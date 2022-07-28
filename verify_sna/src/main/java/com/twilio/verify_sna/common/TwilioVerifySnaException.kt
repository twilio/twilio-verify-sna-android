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
  message: String,
  cause: Exception?
) : Exception(message, cause) {
  object InvalidSnaUrlException : TwilioVerifySnaException(
    message = "Invalid SNA URL",
    cause = null
  )

  object CellularNetworkNotAvailable : TwilioVerifySnaException(
    message = "Cellular network not available.",
    cause = null
  )

  data class NetworkRequestException(private val exception: Exception) : TwilioVerifySnaException(
    message = "Network request exception: ${exception.message}.", cause = exception
  )

  data class UnexpectedException(
    private val exception: Exception
  ) : TwilioVerifySnaException(
    message = "Unexpected error happened: ${exception.message}.",
    cause = exception
  )
}
