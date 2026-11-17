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

package com.twilio.verify_sna.networking

import android.net.Network
import com.twilio.verify_sna.common.Logger
import com.twilio.verify_sna.common.TwilioVerifySnaException
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

interface NetworkRequestProvider {

  fun performRequest(urlText: String, network: Network): NetworkRequestResult
}

class ConcreteNetworkRequestProvider : NetworkRequestProvider {
  override fun performRequest(
    urlText: String,
    network: Network
  ): NetworkRequestResult {
    val okHttpClient = OkHttpClient.Builder()
      .socketFactory(network.socketFactory)
      .protocols(
        listOf(
          Protocol.HTTP_1_1
        )
      )
      .build()
    val request = Request.Builder().url(urlText).build()
    Logger.log("Start cellular connection")
    Logger.log("Request:\n${request.method} ${request.url}")
    return try {
      val response = okHttpClient.newCall(request).execute()
      logRedirects(response)
      if (response.isSuccessful) {
        val status = response.code
        val message = response.body?.string()
        Logger.log("Response:\n$status\n$message")
        NetworkRequestResult(status, message)
      } else {
        Logger.log("Response:\n${response.code} ${response.message}")
        throw TwilioVerifySnaException.NetworkRequestException(
          Exception("SNA_URL wasn't successful")
        )
      }
    } catch (e: IOException) {
      Logger.log("Request failed: $e")
      throw TwilioVerifySnaException.NetworkRequestException(e)
    }
  }

  private fun logRedirects(response: Response) {
    val redirects = generateSequence(response.priorResponse) { it.priorResponse }
      .toList()
      .asReversed()
    redirects.forEach { prior ->
      Logger.log("HTTP redirect (${prior.code}) to: ${prior.header("Location")}")
    }
  }
}
