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
import com.twilio.verify_sna.common.CellularNetworkNotAvailable
import com.twilio.verify_sna.common.NetworkRequestException
import com.twilio.verify_sna.networking.NetworkRequestProvider
import com.twilio.verify_sna.networking.NetworkRequestResult
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
      val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
      connectivityManager.requestNetwork(
        networkRequest,
        object : NetworkCallback() {
          override fun onAvailable(network: Network) {
            try {
              val networkRequestResult = networkRequestProvider.performRequest(url)
              continuation.resume(networkRequestResult)
            } catch (networkRequestException: NetworkRequestException) {
              continuation.resumeWithException(
                networkRequestException
              )
            }
          }

          override fun onUnavailable() {
            // super.onUnavailable()
            continuation.resumeWithException(
              CellularNetworkNotAvailable()
            )
          }
        }
      )
    }
  }
}