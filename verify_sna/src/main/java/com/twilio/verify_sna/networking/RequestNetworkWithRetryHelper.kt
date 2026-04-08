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
        connectivityManager.requestNetwork(
          networkRequest,
          networkCallback
        )
      }, 500)
    }
  }
}
