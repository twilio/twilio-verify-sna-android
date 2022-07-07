package com.twilio.verifysna

interface TwilioVerifySna {

  fun processUrl(url: String): VerificationResult
}