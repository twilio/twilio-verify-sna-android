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

import com.twilio.verify_sna.common.TwilioVerifySnaException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

interface NetworkRequestProvider {

  fun performRequest(urlText: String): NetworkRequestResult
}

class ConcreteNetworkRequestProvider : NetworkRequestProvider {

  override fun performRequest(urlText: String): NetworkRequestResult {
    try {
      val httpUrlConnection: HttpURLConnection?
      val url = URL(urlText)
      httpUrlConnection = url.openConnection() as HttpURLConnection
      val status = httpUrlConnection.responseCode
      val message = obtainResponseMessage(httpUrlConnection)
      return NetworkRequestResult(status, message).also {
        httpUrlConnection.disconnect()
      }
    } catch (exception: Exception) {
      throw TwilioVerifySnaException.NetworkRequestException(exception)
    }
  }

  /**
   * Get String conversion of response, used for debug only.
   * TODO: deserialize the response with an object
   */
  private fun obtainResponseMessage(httpURLConnection: HttpURLConnection): String {
    val bufferedReader = BufferedReader(InputStreamReader(httpURLConnection.inputStream))
    var message = ""
    var currentLine: String?
    while (bufferedReader.readLine().also { currentLine = it } != null) {
      message += currentLine
    }
    return message
  }
}
