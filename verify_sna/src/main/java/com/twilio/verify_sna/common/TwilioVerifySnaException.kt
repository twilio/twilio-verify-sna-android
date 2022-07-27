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

open class TwilioVerifySnaException(
  message: String,
  cause: Exception?,
) : Exception(message, cause)

class InvalidSnaUrlException: TwilioVerifySnaException(
  message = "Invalid SNA URL",
  cause = null
)

class CellularNetworkNotAvailable : TwilioVerifySnaException(
  message = "Cellular network not available.",
  cause = null
)

class NetworkRequestException(cause: Exception) : TwilioVerifySnaException(
  message = "Network request exception: ${cause.message}.",
  cause = cause
)

class UnexpectedException(
  cause: Exception
) : TwilioVerifySnaException(
  message = "Unexpected error happened: ${cause.message}.",
  cause = cause
)
