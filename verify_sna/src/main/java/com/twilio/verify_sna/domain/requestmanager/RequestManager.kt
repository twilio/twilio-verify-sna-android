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
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.twilio.verify_sna.common.TwilioVerifySnaException
import com.twilio.verify_sna.networking.IsMobileDataEnabledHelper
import com.twilio.verify_sna.networking.NetworkRequestResult
import com.twilio.verify_sna.networking.RequestNetworkWithRetryHelper
import com.twilio.verify_sna.networking.VerifySnaNetworkCallbackProvider
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface RequestManager {

  suspend fun processUrl(url: String): NetworkRequestResult
}

class ConcreteRequestManager(
  private val context: Context,
  private val isMobileDataEnabledHelper: IsMobileDataEnabledHelper,
  private val verifySnaNetworkCallbackProvider: VerifySnaNetworkCallbackProvider,
  private val requestNetworkWithRetryHelper: RequestNetworkWithRetryHelper
) : RequestManager {

  override suspend fun processUrl(url: String): NetworkRequestResult {
    return suspendCoroutine { continuation ->
      val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
      ) as ConnectivityManager
      if (isMobileDataEnabledHelper(connectivityManager)) {
        establishCellularConnection(connectivityManager, url, continuation)
      } else {
        continuation.resumeWithException(
          TwilioVerifySnaException.CellularNetworkNotAvailable
        )
      }
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
    val networkCallback = verifySnaNetworkCallbackProvider.provide(
      url,
      continuation,
      connectivityManager
    )

    requestNetworkWithRetryHelper(
      connectivityManager,
      networkRequest,
      networkCallback
    )
  }
}
