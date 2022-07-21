package com.twilio.sample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twilio.verify_sna.TwilioVerifySna
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

  private var twilioVerifySna: TwilioVerifySna? = null

  private val verificationResult = MutableLiveData<ViewState>(ViewState.Idle)

  fun getVerificationResult(): LiveData<ViewState> {
    return verificationResult
  }

  fun setTwilioVerifySna(twilioVerifySna: TwilioVerifySna) {
    this.twilioVerifySna = twilioVerifySna
  }

  fun verify(snaUrl: String) {
    val verify = twilioVerifySna ?: return
    processVerification(verify, snaUrl)
  }

  private fun processVerification(verify: TwilioVerifySna, snaUrl: String) {
    viewModelScope.launch {
      verificationResult.value = ViewState.Loading
      val result = verify.processUrl(snaUrl)
      verificationResult.value = ViewState.VerificationDone(result)
    }
  }
}