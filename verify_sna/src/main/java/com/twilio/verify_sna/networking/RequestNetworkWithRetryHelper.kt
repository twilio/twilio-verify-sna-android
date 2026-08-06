package com.twilio.verify_sna.networking

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper

interface RequestNetworkWithRetryHelper {
  /**
   * Registers [networkCallback] for [networkRequest], retrying once if the registration fails.
   *
   * @param onFailure invoked with the retry's exception when both attempts fail. The callback
   * never registers in that case, so callers must report the failure themselves or they will
   * wait for their own timeout with no indication of what went wrong.
   */
  operator fun invoke(
    connectivityManager: ConnectivityManager,
    networkRequest: NetworkRequest,
    networkCallback: NetworkCallback,
    onFailure: (Exception) -> Unit
  )
}

class RequestNetworkWithRetryHelperImpl : RequestNetworkWithRetryHelper {

  override operator fun invoke(
    connectivityManager: ConnectivityManager,
    networkRequest: NetworkRequest,
    networkCallback: NetworkCallback,
    onFailure: (Exception) -> Unit
  ) {
    try {
      connectivityManager.requestNetwork(
        networkRequest,
        networkCallback
      )
    } catch (e: Exception) {
      Handler(Looper.getMainLooper()).postDelayed({
        // Retry after 500ms is the requestNetwork call fails for any reason.
        try {
          connectivityManager.requestNetwork(
            networkRequest,
            networkCallback
          )
        } catch (retryException: Exception) {
          onFailure(retryException)
        }
      }, 500)
    }
  }
}
