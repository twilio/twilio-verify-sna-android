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

package com.twilio.verify_sna.network

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface CellularNetworkConnection {

  suspend fun performRequest(urlText: String): SnaResponse
}

class ConcreteCellularNetworkConnection(
  private val context: Context
) : CellularNetworkConnection {

  override suspend fun performRequest(urlText: String): SnaResponse {
    return suspendCoroutine { continuation ->
      val connectivityManager = context.getSystemService(
        CONNECTIVITY_SERVICE
      ) as ConnectivityManager
      val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
      connectivityManager.requestNetwork(
        networkRequest,
        object : ConnectivityManager.NetworkCallback() {
          override fun onAvailable(network: Network) {
            val snaUrlResponse = connect(urlText)
            continuation.resume(snaUrlResponse)
          }
        }
      )
    }
  }

  private fun connect(urlText: String): SnaResponse {
    val httpURLConnection: HttpURLConnection?
    val url = URL(urlText)
    httpURLConnection = url.openConnection() as HttpURLConnection

    val code = httpURLConnection.responseCode
    val br = BufferedReader(InputStreamReader(httpURLConnection.inputStream))
    var message = ""
    var strCurrentLine: String?
    while (br.readLine().also { strCurrentLine = it } != null) {
      message += strCurrentLine
    }
    return SnaResponse(code, message)
  }
}
