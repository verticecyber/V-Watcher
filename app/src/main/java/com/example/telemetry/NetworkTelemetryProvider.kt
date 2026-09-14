package com.example.telemetry

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkTelemetryProvider(private val context: Context) {

  fun collect(): TelemetryResult<NetworkTelemetry> {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val activeNetwork = cm?.activeNetwork
    val caps = if (cm != null && activeNetwork != null) cm.getNetworkCapabilities(activeNetwork) else null

    if (cm == null) {
      return TelemetryResult(
        value = NetworkTelemetry(
          transportType = "Unavailable",
          isValidated = false,
          isVpnActive = false,
          downstreamBandwidthKbps = 0,
          upstreamBandwidthKbps = 0,
          advancedPacketInspectionAvailable = false,
          vpnConsentRequiredNote = "Connectivity service unavailable"
        ),
        source = "Android ConnectivityManager",
        availability = TelemetryAvailability.UNAVAILABLE,
        permissionState = "Service unavailable",
        isReal = false,
        diagnosticNotes = "ConnectivityManager system service returned null"
      )
    }

    if (caps == null) {
      return TelemetryResult(
        value = NetworkTelemetry(
          transportType = "Disconnected",
          isValidated = false,
          isVpnActive = false,
          downstreamBandwidthKbps = 0,
          upstreamBandwidthKbps = 0,
          advancedPacketInspectionAvailable = false,
          vpnConsentRequiredNote = "Requires VPN authorization"
        ),
        source = "Android ConnectivityManager",
        availability = TelemetryAvailability.AVAILABLE,
        permissionState = "Granted (ACCESS_NETWORK_STATE)",
        isReal = true,
        diagnosticNotes = "Network interface offline or disconnected"
      )
    }

    val transport = when {
      caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
      caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
      caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
      caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
      caps.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth Tether"
      else -> "Other"
    }

    val isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    val isVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    val downKbps = caps.linkDownstreamBandwidthKbps
    val upKbps = caps.linkUpstreamBandwidthKbps

    return TelemetryResult(
      value = NetworkTelemetry(
        transportType = transport,
        isValidated = isValidated,
        isVpnActive = isVpn,
        downstreamBandwidthKbps = downKbps,
        upstreamBandwidthKbps = upKbps,
        advancedPacketInspectionAvailable = false, // Honest product boundary: requires VpnService
        vpnConsentRequiredNote = "Requires VPN authorization for deep packet inspection"
      ),
      source = "Android ConnectivityManager & NetworkCapabilities",
      availability = TelemetryAvailability.AVAILABLE,
      permissionState = "Granted (ACCESS_NETWORK_STATE)",
      isReal = true,
      diagnosticNotes = "Real link capabilities observed. Advanced packet inspection correctly marked as requiring user VPN authorization."
    )
  }
}
