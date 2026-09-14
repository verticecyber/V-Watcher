package com.example.reasoning

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Metadata descriptor for the on-device Gemma artifact.
 */
data class GemmaModelMetadata(
  val modelName: String = "gemma-2b-it-cpu-int4.bin",
  val modelVariant: String = "Gemma 2B IT (Instruction Tuned)",
  val modelFormat: String = "MediaPipe LLM Task Binary / FlatBuffer",
  val modelVersion: String = "2.0-2B-IT",
  val quantization: String = "INT4",
  val estimatedSizeBytes: Long = 1_432_000_000L,
  val expectedSha256: String? = null,
  val origin: String = "Google MediaPipe LLM / Hugging Face Google Gemma Hub",
  val runtime: String = "MediaPipe Tasks GenAI (libllm_inference_engine_jni)",
  val targetAbi: String = "arm64-v8a"
)

sealed class ModelDownloadState {
  data object Idle : ModelDownloadState()
  data class Downloading(
    val progressFraction: Float,
    val bytesRead: Long,
    val totalBytes: Long
  ) : ModelDownloadState()
  data object Verifying : ModelDownloadState()
  data object Ready : ModelDownloadState()
  data class Failed(val error: String) : ModelDownloadState()
}

/**
 * Manages the physical lifecycle of the Gemma weights artifact on the Android device:
 * - App-private storage placement in context.filesDir/models/
 * - Checksum and byte integrity verification
 * - Resilient, cancellable, non-blocking background streaming download
 * - Atomic rename from .download temporary file
 */
class GemmaModelManager(
  private val context: Context,
  val metadata: GemmaModelMetadata = GemmaModelMetadata()
) {
  val modelsDir: File = File(context.filesDir, "models")
  val targetModelFile: File = File(modelsDir, metadata.modelName)
  private val tempDownloadFile: File = File(modelsDir, "${metadata.modelName}.download")

  private val _downloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.Idle)
  val downloadState: StateFlow<ModelDownloadState> = _downloadState.asStateFlow()

  private var activeDownloadJob: Job? = null

  init {
    if (!modelsDir.exists()) {
      modelsDir.mkdirs()
    }
  }

  fun isArtifactAvailable(): Boolean {
    val altFile = File(context.filesDir, metadata.modelName)
    val file = when {
      targetModelFile.exists() && targetModelFile.length() > 0 -> targetModelFile
      altFile.exists() && altFile.length() > 0 -> altFile
      else -> null
    }
    return file != null && verifyIntegrity(file)
  }

  fun getResolvedModelFile(): File? {
    val altFile = File(context.filesDir, metadata.modelName)
    return when {
      targetModelFile.exists() && targetModelFile.length() > 0 -> targetModelFile
      altFile.exists() && altFile.length() > 0 -> altFile
      else -> null
    }
  }

  fun verifyIntegrity(file: File): Boolean {
    if (!file.exists() || !file.canRead()) return false
    val length = file.length()
    // A valid Gemma weight artifact is non-empty; for test mocks or lightweight benchmarks >= 1KB
    if (length < 1024) return false

    // Optional SHA-256 verification if specified in metadata
    val expectedHash = metadata.expectedSha256
    if (!expectedHash.isNullOrBlank()) {
      val actualHash = computeSha256(file)
      if (!actualHash.equals(expectedHash, ignoreCase = true)) {
        return false
      }
    }
    return true
  }

  fun computeSha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { inputStream ->
      val buffer = ByteArray(65536)
      var bytesRead: Int
      while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        digest.update(buffer, 0, bytesRead)
      }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
  }

  /**
   * Installs an artifact directly from an input stream (e.g. for testing, preloaded asset, or bundle)
   */
  suspend fun installFromStream(inputStream: InputStream, verifyHash: Boolean = true): Boolean = withContext(Dispatchers.IO) {
    _downloadState.value = ModelDownloadState.Downloading(0f, 0, -1)
    try {
      if (!modelsDir.exists()) modelsDir.mkdirs()
      if (tempDownloadFile.exists()) tempDownloadFile.delete()

      tempDownloadFile.outputStream().use { output ->
        val buffer = ByteArray(65536)
        var bytesRead: Int
        var totalRead = 0L
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
          output.write(buffer, 0, bytesRead)
          totalRead += bytesRead
          _downloadState.value = ModelDownloadState.Downloading(
            progressFraction = 0.5f,
            bytesRead = totalRead,
            totalBytes = -1
          )
        }
      }

      _downloadState.value = ModelDownloadState.Verifying
      if (verifyIntegrity(tempDownloadFile)) {
        if (targetModelFile.exists()) targetModelFile.delete()
        val renamed = tempDownloadFile.renameTo(targetModelFile)
        if (renamed) {
          _downloadState.value = ModelDownloadState.Ready
          return@withContext true
        } else {
          _downloadState.value = ModelDownloadState.Failed("Failed to atomically rename model artifact")
          return@withContext false
        }
      } else {
        tempDownloadFile.delete()
        _downloadState.value = ModelDownloadState.Failed("Integrity verification failed for installed artifact")
        return@withContext false
      }
    } catch (e: Exception) {
      if (tempDownloadFile.exists()) tempDownloadFile.delete()
      _downloadState.value = ModelDownloadState.Failed("Install failed: ${e.message}")
      return@withContext false
    }
  }

  /**
   * Downloads model weights from remote HTTP/HTTPS URL into app-private storage.
   */
  suspend fun downloadFromUrl(
    sourceUrl: String,
    onProgress: ((Float, Long, Long) -> Unit)? = null
  ): Boolean = withContext(Dispatchers.IO) {
    _downloadState.value = ModelDownloadState.Downloading(0f, 0L, -1L)
    var connection: HttpURLConnection? = null
    try {
      if (!modelsDir.exists()) modelsDir.mkdirs()
      if (tempDownloadFile.exists()) tempDownloadFile.delete()

      val url = URL(sourceUrl)
      connection = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 15000
        readTimeout = 30000
        requestMethod = "GET"
        setRequestProperty("User-Agent", "VWatcher-Android-GemmaManager/1.0")
      }

      val responseCode = connection.responseCode
      if (responseCode !in 200..299) {
        _downloadState.value = ModelDownloadState.Failed("HTTP $responseCode: ${connection.responseMessage}")
        return@withContext false
      }

      val contentLength = connection.contentLengthLong
      var totalBytesRead = 0L

      connection.inputStream.use { input ->
        FileOutputStream(tempDownloadFile).use { output ->
          val buffer = ByteArray(65536)
          var bytesRead: Int
          while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead
            val fraction = if (contentLength > 0) totalBytesRead.toFloat() / contentLength else 0f
            _downloadState.value = ModelDownloadState.Downloading(fraction, totalBytesRead, contentLength)
            onProgress?.invoke(fraction, totalBytesRead, contentLength)
          }
        }
      }

      _downloadState.value = ModelDownloadState.Verifying
      if (verifyIntegrity(tempDownloadFile)) {
        if (targetModelFile.exists()) targetModelFile.delete()
        val success = tempDownloadFile.renameTo(targetModelFile)
        if (success) {
          _downloadState.value = ModelDownloadState.Ready
          return@withContext true
        } else {
          _downloadState.value = ModelDownloadState.Failed("Failed to move artifact to destination")
          return@withContext false
        }
      } else {
        tempDownloadFile.delete()
        _downloadState.value = ModelDownloadState.Failed("Integrity check failed: Artifact corrupt or size invalid")
        return@withContext false
      }
    } catch (e: CancellationException) {
      if (tempDownloadFile.exists()) tempDownloadFile.delete()
      _downloadState.value = ModelDownloadState.Idle
      throw e
    } catch (e: Exception) {
      if (tempDownloadFile.exists()) tempDownloadFile.delete()
      _downloadState.value = ModelDownloadState.Failed("Download error: ${e.message}")
      return@withContext false
    } finally {
      connection?.disconnect()
    }
  }

  fun cancelActiveDownload() {
    activeDownloadJob?.cancel()
    activeDownloadJob = null
    if (tempDownloadFile.exists()) {
      tempDownloadFile.delete()
    }
    _downloadState.value = ModelDownloadState.Idle
  }

  fun deleteModel(): Boolean {
    val file = getResolvedModelFile()
    val deleted = file?.delete() ?: false
    _downloadState.value = ModelDownloadState.Idle
    return deleted
  }
}
