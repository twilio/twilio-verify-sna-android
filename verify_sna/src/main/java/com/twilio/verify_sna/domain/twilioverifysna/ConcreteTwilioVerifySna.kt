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

package com.twilio.verify_sna.domain.twilioverifysna

import android.os.Looper
import android.util.Patterns
import com.twilio.verify_sna.ProcessUrlResult
import com.twilio.verify_sna.TwilioVerifySna
import com.twilio.verify_sna.common.TwilioVerifySnaException
import com.twilio.verify_sna.domain.requestmanager.RequestManager

/**
 * Constant used for validating the SNA URL results, this may change if the provider changes the implementation.
 */
private const val SUCCESS_PATH = "ErrorCode=0&ErrorDescription=Success"

class ConcreteTwilioVerifySna(
  private val requestManager: RequestManager
) : TwilioVerifySna {

  override suspend fun processUrl(snaUrl: String): ProcessUrlResult {
    return try {
      if (!Patterns.WEB_URL.matcher(snaUrl).matches()) {
        throw TwilioVerifySnaException.InvalidUrlException
      }
      if (Looper.myLooper() == Looper.getMainLooper()) {
        throw TwilioVerifySnaException.RunInMainThreadException
      }
      val snaResponse = requestManager.processUrl(snaUrl)
      if (isMessageValid(snaResponse.message)) {
        return ProcessUrlResult.Success(snaResponse)
      }
      throw TwilioVerifySnaException.NoResultFromUrl(snaResponse.message.toString())
    } catch (twilioVerifySnaException: TwilioVerifySnaException) {
      ProcessUrlResult.Fail(twilioVerifySnaException)
    } catch (exception: Exception) {
      ProcessUrlResult.Fail(TwilioVerifySnaException.UnexpectedException(exception))
    }
  }

  private fun isMessageValid(message: String?): Boolean {
    message ?: return false
    return message.contains(SUCCESS_PATH)
  }
}
