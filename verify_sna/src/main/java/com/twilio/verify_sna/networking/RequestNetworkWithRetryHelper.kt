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
        // Retry after 500ms is the requestNetwork call fails for any reason.
        try {
          connectivityManager.requestNetwork(
            networkRequest,
            networkCallback
          )
        } catch (retryException: Exception) {
        }
      }, 500)
    }
  }
}
