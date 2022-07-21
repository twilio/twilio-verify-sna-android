package com.twilio.sample

import com.twilio.verify_sna.domain.VerificationResult

sealed interface ViewState {
  object Idle : ViewState

  object Loading : ViewState

  data class VerificationDone(
    val verificationResult: VerificationResult
  ) : ViewState
}
