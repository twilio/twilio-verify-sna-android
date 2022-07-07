package com.twilio.verifysna.domain

import com.twilio.verifysna.TwilioVerifySna
import com.twilio.verifysna.VerificationResult

class ConcreteTwilioVerifySna(
  private val repository: Twilio
): TwilioVerifySna {

  override fun processUrl(url: String): VerificationResult {
    TODO("Not yet implemented")
  }
}
