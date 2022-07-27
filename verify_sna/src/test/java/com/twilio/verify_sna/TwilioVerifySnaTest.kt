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
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.verify_sna.common.InvalidSnaUrlException
import com.twilio.verify_sna.common.TwilioVerifySnaException
import com.twilio.verify_sna.common.UnexpectedException
import com.twilio.verify_sna.domain.requestmanager.RequestManager
import com.twilio.verify_sna.networking.NetworkRequestResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TwilioVerifySnaTest {

  private val context: Context = mock()
  private val requestManager: RequestManager = mock()
  private lateinit var twilioVerifySna: TwilioVerifySna

  @Before
  fun setUp() {
    twilioVerifySna = TwilioVerifySna.Builder(context)
      .requestManager(requestManager)
      .build()
  }

  @Test
  fun `Process invalid Url`() {
    runBlocking {
      val url = "invalidurl"

      val processUrlResult = twilioVerifySna.processUrl(url)

      val actualResult = processUrlResult as? ProcessUrlResult.Fail
      Assert.assertNotNull(actualResult)
      Assert.assertTrue(
        actualResult?.twilioVerifySnaException is InvalidSnaUrlException
      )
    }
  }

  @Test
  fun `Process an Url returns Success result`() {
    runBlocking {
      val networkRequestResult: NetworkRequestResult = mock()
      val url = "https://www.test.com"

      whenever(
        requestManager.processUrl(url)
      ).thenReturn(
        networkRequestResult
      )

      val result = twilioVerifySna.processUrl(url)

      assertThat(result).isEqualTo(ProcessUrlResult.Success(networkRequestResult))
    }
  }

  @Test
  fun `Process an Url throws a verify Sna exception`() {
    runBlocking {
      val twilioVerifySnaException: TwilioVerifySnaException = mock()
      val url = "https://www.test.com"

      whenever(
        requestManager.processUrl(url)
      ).then {
        throw twilioVerifySnaException
      }

      val processUrlResult = twilioVerifySna.processUrl(url)

      val actualResult = processUrlResult as? ProcessUrlResult.Fail
      Assert.assertNotNull(actualResult)
      assertThat(
        actualResult?.twilioVerifySnaException
      ).isEqualTo(twilioVerifySnaException)
    }
  }

  @Test
  fun `Process an Url throws an unexpected exception`() {
    runBlocking {
      val exception: Exception = mock()
      val url = "https://www.test.com"

      whenever(
        requestManager.processUrl(url)
      ).then {
        throw exception
      }

      val processUrlResult = twilioVerifySna.processUrl(url)

      val actualResult = processUrlResult as? ProcessUrlResult.Fail
      Assert.assertNotNull(actualResult)
      Assert.assertTrue(
        actualResult?.twilioVerifySnaException is UnexpectedException
      )
    }
  }
}
