package com.twilio.verify_sna.networking

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.twilio.verify_sna.common.TwilioVerifySnaException
import java.util.concurrent.atomic.AtomicBoolean
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
    // Network callbacks such as onCapabilitiesChanged can fire multiple times before the
    // callback is unregistered. This guard guarantees the continuation is resumed exactly once,
    // preventing "IllegalStateException: Already resumed" crashes.
    val hasResumed = AtomicBoolean(false)
    return object : NetworkCallback() {
      override fun onAvailable(network: Network) {
        super.onAvailable(network)
        if (VERSION.SDK_INT < VERSION_CODES.M) {
          performRequest(url, network, continuation, connectivityManager, this, hasResumed)
        }
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
      ) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
          if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            performRequest(url, network, continuation, connectivityManager, this, hasResumed)
          }
          // If the network is not validated yet we simply wait for a subsequent
          // onCapabilitiesChanged. A network that never validates is backstopped by the
          // timeout in RequestManager, which cancels the request and unregisters this callback.
        }
      }
    }
  }

  private fun performRequest(
    url: String,
    network: Network,
    continuation: Continuation<NetworkRequestResult>,
    connectivityManager: ConnectivityManager,
    networkCallback: NetworkCallback,
    hasResumed: AtomicBoolean
  ) {
    // Only the first invocation is allowed to resume the continuation and unregister the callback.
    if (!hasResumed.compareAndSet(false, true)) {
      return
    }
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
