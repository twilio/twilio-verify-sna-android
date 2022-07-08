package com.twilio.verifysna

import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.twilio.verifysna.domain.RequestManager
import com.twilio.verifysna.domain.VerificationResult
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
