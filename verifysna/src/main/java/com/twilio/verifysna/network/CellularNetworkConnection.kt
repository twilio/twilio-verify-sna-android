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

package com.twilio.verifysna.network

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import java.net.HttpURLConnection
import java.net.URL

interface CellularNetworkConnection {

  fun performRequest(urlText: String)
}

class ConcreteCellularNetworkConnection(
  private val context: Context
) : CellularNetworkConnection {

  override fun performRequest(urlText: String) {
    val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkRequest = NetworkRequest.Builder()
      .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()
    connectivityManager.requestNetwork(networkRequest, object: ConnectivityManager.NetworkCallback(){
      override fun onAvailable(network: Network) {
        //super.onAvailable(network)
       connect(urlText)
      }
    })

  }

  private fun connect(urlText: String) {
    var httpURLConnection: HttpURLConnection?
    val url = URL(urlText)
    httpURLConnection = url.openConnection() as HttpURLConnection

    val code = httpURLConnection.responseCode
    if (code != 200) {
      throw Exception("Invalid response code: $code")
    }
    /*finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
    }*/
  }
}
