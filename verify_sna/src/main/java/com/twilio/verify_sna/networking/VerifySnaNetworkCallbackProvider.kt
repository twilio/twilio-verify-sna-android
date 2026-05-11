package com.twilio.verify_sna.networking

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.twilio.verify_sna.common.TwilioVerifySnaException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface VerifySnaNetworkCallbackProvider {

  fun provide(
    url:String,
    continuation: Continuation<NetworkRequestResult>,
    connectivityManager: ConnectivityManager
  ): NetworkCallback
}

class VerifySnaNetworkCallbackProviderImpl(
  private val networkRequestProvider: NetworkRequestProvider,
) : VerifySnaNetworkCallbackProvider {

  override fun provide(
    url:String,
    continuation: Continuation<NetworkRequestResult>,
    connectivityManager: ConnectivityManager
  ): NetworkCallback {
    return object : NetworkCallback() {
      override fun onAvailable(network: Network) {
        super.onAvailable(network)
        if (VERSION.SDK_INT < VERSION_CODES.M) {
          performRequest(url, network, continuation, connectivityManager, this)
        }
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
      ) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
          if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            performRequest(url, network, continuation, connectivityManager, this)
          } else {
            checkNetworkConnectivity(network)
          }
        }
      }

      private fun checkNetworkConnectivity(network: Network) {
        try {
          network.getByName("google.com").toString().isNotEmpty()
        } catch (e: Exception) {
          connectivityManager.unregisterNetworkCallback(this)
          continuation.resumeWithException(
            TwilioVerifySnaException.NetworkRequestException(
              Exception("Network is not capable of connecting to internet"),
            )
          )
        }
      }
    }
  }

  private fun performRequest(
    url: String,
    network: Network,
    continuation: Continuation<NetworkRequestResult>,
    connectivityManager: ConnectivityManager,
    networkCallback: NetworkCallback
  ) {
    try {
      val networkRequestResult = networkRequestProvider.performRequest(url, network)
      continuation.resume(networkRequestResult)
    } catch (networkRequestException: TwilioVerifySnaException.NetworkRequestException) {
      continuation.resumeWithException(
        networkRequestException
      )
    } catch (e: Exception) {
      continuation.resumeWithException(
        TwilioVerifySnaException.NetworkRequestException(e)
      )
    } finally {
      connectivityManager.unregisterNetworkCallback(networkCallback)
    }
  }
}
