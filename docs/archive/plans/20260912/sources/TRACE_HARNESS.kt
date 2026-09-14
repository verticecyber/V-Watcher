package com.example.petscan

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.communication.DefaultCommunicationChannel
import com.example.communication.ReasoningRequest
import com.example.immune.AgentLifecycleState
import com.example.immune.AgentRole
import com.example.immune.BiomimeticImmuneSystem
import com.example.immune.ImmuneMessage
import com.example.immune.ImmunePayload
import com.example.memory.LocalImmuneMemoryRepository
import com.example.reasoning.DeterministicBackend
import com.example.reasoning.GeminiNanoBackend
import com.example.reasoning.GemmaBackend
import com.example.reasoning.ReasoningRouter
import com.example.reasoning.ResourceGuardrails
import com.example.sentinel.AndroidSentinel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

/** TEMPORARY PET-scan trace probe (L2 Robolectric). Prints chain evidence to stdout. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PetScanTraceTest {

  private lateinit var context: Context
  private lateinit var router: ReasoningRouter
  private lateinit var channel: DefaultCommunicationChannel

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    router = ReasoningRouter(GeminiNanoBackend(context), GemmaBackend(context), DeterministicBackend(), ResourceGuardrails())
    channel = DefaultCommunicationChannel(router)
  }

  @Test
  fun traceNominalChain() = runBlocking {
    val sentinel = AndroidSentinel(context)
    val t0 = System.currentTimeMillis()
    val obs = sentinel.observeNow()
    val tObserve = System.currentTimeMillis() - t0
    val system = BiomimeticImmuneSystem(context, channel, LocalImmuneMemoryRepository(context))
    val snap = system.processObservation(obs)
    println("PETSCAN nominal observationId=${obs.observationId} observeMs=$tObserve " +
      "macroState=${snap.homeostasis.macroState} confidence=${snap.homeostasis.confidenceScore} " +
      "busTotal=${system.bus.stats.value.totalMessagesDispatched} " +
      "busRate=${system.bus.stats.value.currentDispatchesPerSecond} " +
      "storm=${system.bus.stats.value.activeStormDetected} " +
      "dropped=${system.bus.stats.value.droppedMessagesCount} " +
      "reasoningCalls=${channel.state.value.totalReasoningRequests} " +
      "telemetryDispatched=${channel.state.value.totalTelemetryDispatched}")
  }

  @Test
  fun traceRouterDirect() = runBlocking {
    val sentinel = AndroidSentinel(context)
    val obs = sentinel.observeNow()
    val req = ReasoningRequest(
      requestId = "req_pet_${UUID.randomUUID().toString().take(6)}",
      correlationId = "corr_pet",
      observation = obs,
      candidateAnomaly = "socket burst",
      preferredBackend = null
    )
    val t0 = System.currentTimeMillis()
    val resp = router.routeAndExecute(req)
    val wall = System.currentTimeMillis() - t0
    println("PETSCAN router selected=${resp.selectedBackend} actual=${resp.actualBackendUsed} " +
      "mode=${resp.executionMode} status=${resp.executionStatus} fallback=${resp.fallbackReason} " +
      "latencyMs=${resp.latencyMs} wallMs=$wall classification=${resp.assessment.classification} " +
      "confidence=${resp.assessment.confidence}")
  }

  @Test
  fun traceStormLoad() = runBlocking {
    val system = BiomimeticImmuneSystem(context, channel, LocalImmuneMemoryRepository(context))
    val t0 = System.currentTimeMillis()
    repeat(40) { i ->
      system.bus.dispatch(
        ImmuneMessage(
          messageId = "msg_pet_$i", correlationId = "corr_pet", incidentId = null,
          observationId = "obs_pet", emitterRole = AgentRole.PRR, emitterId = "pet",
          targetRole = null, freshnessMs = 0L, isStale = false,
          evidenceRefs = emptyList(), requestedAction = null,
          senderState = AgentLifecycleState.ACTIVE,
          payload = ImmunePayload.TelemetryArrival(
            (AndroidSentinel(context)).observeNow()
          )
        )
      )
    }
    val wall = System.currentTimeMillis() - t0
    val s = system.bus.stats.value
    println("PETSCAN storm dispatched=40 wallMs=$wall total=${s.totalMessagesDispatched} " +
      "rate=${s.currentDispatchesPerSecond} storm=${s.activeStormDetected} dropped=${s.droppedMessagesCount}")
  }
}
