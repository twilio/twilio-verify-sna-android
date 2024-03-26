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
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Handler
import android.os.Looper
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

  var logger: (String) -> Unit
}

class ConcreteRequestManager(
  private val context: Context,
  private val networkRequestProvider: NetworkRequestProvider
) : RequestManager {

  override var logger: (String) -> Unit = { }

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

  private fun establishCellularConnection(
    connectivityManager: ConnectivityManager,
    url: String,
    continuation: Continuation<NetworkRequestResult>
  ) {
    val networkRequestBuilder = NetworkRequest.Builder()
      .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)

    val networkRequest = networkRequestBuilder.build()
    val networkCallback = object : NetworkCallback() {
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
        logger("Network onCapabilitiesChanged")
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
          if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            performRequest(url, network, continuation, connectivityManager, this)
          } else {
            logger("No NetworkCapabilities.NET_CAPABILITY_VALIDATED found")
            connectivityManager.unregisterNetworkCallback(this)
            continuation.resumeWithException(
              TwilioVerifySnaException.NetworkRequestException(
                Exception("No NetworkCapabilities.NET_CAPABILITY_VALIDATED found"),
              )
            )
          }
        }
      }

      override fun onUnavailable() {
        super.onUnavailable()
        logger("Network onUnavailable")
      }

      override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
        logger("Network onLosing")
      }

      override fun onLost(network: Network) {
        super.onLost(network)
        logger("Network onLost")
      }

      override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties)
        logger("Network onLinkPropertiesChanged")
        logger("with network: " + network)
        logger("with linkProperties: " + linkProperties)
      }

      override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
        super.onBlockedStatusChanged(network, blocked)
        logger("Network onBlockedStatusChanged")
      }
    }

    try {
      connectivityManager.requestNetwork(
        networkRequest,
        networkCallback
      )
    } catch (e: Exception) {
      logger("Error requesting network in first try: " + e.message)
      Handler(Looper.getMainLooper()).postDelayed({
        connectivityManager.requestNetwork(
          networkRequest,
          networkCallback
        )
      }, 500)
    }
  }
}
