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

package com.twilio.verify_sna.common

sealed class TwilioVerifySnaException(
  description: String,
  technicalError: String? = null,
  cause: Exception? = null
) : Exception(
  """
    $description.
    TechnicalError: ${technicalError ?: "Undefined"}.
  """.trimIndent(),
  cause
) {
  object InvalidUrlException : TwilioVerifySnaException(
    description = "Invalid URL, please check the format.",
    technicalError = "Unable to convert the URL string to an URL object."
  )

  object CellularNetworkNotAvailable : TwilioVerifySnaException(
    description = "Cellular network not available.",
    technicalError = "ConnectivityManager established that a cellular network is not available, running on a simulator or a device with no sim card is no supported."
  )

  object NoResultFromUrl : TwilioVerifySnaException(
    description = "Unable to get a valid result from the requested URL.",
    technicalError = "Unable to get a redirection path or a result path from the URL, probably the SNAURL is corrupted (or maybe expired)."
  )

  data class NetworkRequestException(private val exception: Exception) : TwilioVerifySnaException(
    description = "Networking error, cause: ${exception.message}",
    cause = exception
  )

  object RunInMainThreadException : TwilioVerifySnaException(
    description = "Can't run inside main thread."
  )

  data class UnexpectedException(
    private val exception: Exception
  ) : TwilioVerifySnaException(
    description = "Unexpected error happened: ${exception.message}."
  )
}
