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

package com.twilio.verify_sna

import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.twilio.verify_sna.domain.RequestManager
import com.twilio.verify_sna.domain.VerificationResult
import org.junit.Assert.assertEquals
import org.junit.Test

class TwilioVerifySnaTest {

  private val context: Context = mock()
  private val requestManager: RequestManager = mock()

  @Test
  fun `Process an url invokes request manager`() {
    val url = "http://www.test.com"
    val twilioVerifySna = TwilioVerifySna.Builder(
      context, requestManager
    ).build()

    val result = twilioVerifySna.processUrl(url)

    assertEquals(VerificationResult.Success, result)

    verify(
      requestManager, times(1)
    ).processUrl(url)
  }
}
