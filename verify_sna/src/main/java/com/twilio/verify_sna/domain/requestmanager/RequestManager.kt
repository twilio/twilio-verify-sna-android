/*
 * Copyright (c) 2022 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.verify_sna.domain.requestmanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.twilio.verify_sna.common.TwilioVerifySnaException
import com.twilio.verify_sna.networking.NetworkRequestProvider
import com.twilio.verify_sna.networking.NetworkRequestResult
import java.lang.reflect.Method
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface RequestManager {

  suspend fun processUrl(url: String): NetworkRequestResult
}

class ConcreteRequestManager(
  private val context: Context,
  private val networkRequestProvider: NetworkRequestProvider
) : RequestManager {

  override suspend fun processUrl(url: String): NetworkRequestResult {
    return suspendCoroutine { continuation ->
      val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
      ) as ConnectivityManager

      if (isMobileDataEnabled(connectivityManager)) {
        establishCellularConnection(connectivityManager, url, continuation)
      } else {
        continuation.resumeWithException(
          TwilioVerifySnaException.CellularNetworkNotAvailable
        )
      }
    }
  }

  /**
   * Android Framework doesn't count with a pre-build way of getting mobile network status,
   * when Wi-Fi is active. Reflection fits well.
   * Taken from https://stackoverflow.com/a/8243305
   */
  private fun isMobileDataEnabled(cm: ConnectivityManager): Boolean {
    return try {
      val c = Class.forName(cm.javaClass.name)
      val m: Method = c.getDeclaredMethod("getMobileDataEnabled")
      m.isAccessible = true
      m.invoke(cm) as Boolean
    } catch (exception: Exception) {
      exception.printStackTrace()
      false
    }
  }

  private fun establishCellularConnection(
    connectivityManager: ConnectivityManager,
    url: String,
    continuation: Continuation<NetworkRequestResult>
  ) {
    val networkRequestBuilder = NetworkRequest.Builder()
      .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)

    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    val networkRequest = networkRequestBuilder.build()
    val networkCallback = object : NetworkCallback() {
      override fun onAvailable(network: Network) {
        try {
          val networkRequestResult = networkRequestProvider.performRequest(url, network)
          continuation.resume(networkRequestResult)
        } catch (networkRequestException: TwilioVerifySnaException.NetworkRequestException) {
          continuation.resumeWithException(
            networkRequestException
          )
        } finally {
          connectivityManager.unregisterNetworkCallback(this)
        }
      }

      override fun onUnavailable() {
        println("***onUnavailable***")
      }
    }
    connectivityManager.requestNetwork(
      networkRequest,
      networkCallback
    )
  }
}
