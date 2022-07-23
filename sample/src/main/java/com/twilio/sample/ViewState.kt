package com.twilio.sample

import com.twilio.verify_sna.ProcessUrlResult

sealed interface ViewState {
  object Idle : ViewState

  object Loading : ViewState

  data class VerificationSuccess(
    val verificationResult: ProcessUrlResult
  ) : ViewState

  data class VerificationFail(
    val message: String
  ) : ViewState
}
