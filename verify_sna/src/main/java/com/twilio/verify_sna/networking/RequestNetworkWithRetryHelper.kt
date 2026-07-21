package com.twilio.verify_sna.networking

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper

interface RequestNetworkWithRetryHelper {
  operator fun invoke(
    connectivityManager: ConnectivityManager,
    networkRequest: NetworkRequest,
    networkCallback: NetworkCallback
  )
}

class RequestNetworkWithRetryHelperImpl : RequestNetworkWithRetryHelper {

  override operator fun invoke(
    connectivityManager: ConnectivityManager,
    networkRequest: NetworkRequest,
    networkCallback: NetworkCallback
  ) {
    try {
      connectivityManager.requestNetwork(
        networkRequest,
        networkCallback
      )
    } catch (e: Exception) {
      Handler(Looper.getMainLooper()).postDelayed({
        // Guard the retry so a second failure doesn't crash the main thread. If the retry also
        // fails the continuation won't resume here, but the timeout in RequestManager backstops
        // that case by cancelling the request and unregistering the callback.
        try {
          connectivityManager.requestNetwork(
            networkRequest,
            networkCallback
          )
        } catch (retryException: Exception) {
          retryException.printStackTrace()
        }
      }, 500)
    }
  }
}
