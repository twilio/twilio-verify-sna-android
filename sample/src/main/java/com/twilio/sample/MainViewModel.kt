package com.twilio.sample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twilio.verify_sna.ProcessUrlResult
import com.twilio.verify_sna.TwilioVerifySna
import com.twilio.verify_sna.common.CellularNetworkNotAvailable
import com.twilio.verify_sna.common.NetworkRequestException
import com.twilio.verify_sna.common.TwilioVerifySnaException
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
      try {
        val result = verify.processUrl(snaUrl)
        verificationResult.value = when (result) {
          is ProcessUrlResult.Success -> ViewState.VerificationSuccess(result)
          is ProcessUrlResult.Fail -> ViewState.VerificationFail(
            processVerificationFail(result.twilioVerifySnaException)
          )
        }
      } catch (exception: TwilioVerifySnaException) {
        verificationResult.value = ViewState.VerificationFail("error")
      }
    }
  }

  private fun processVerificationFail(twilioVerifySnaException: TwilioVerifySnaException): String {
    return when (twilioVerifySnaException) {
      is CellularNetworkNotAvailable -> "Cellular Network Not Available"
      is NetworkRequestException -> "Network Request Exception: ${twilioVerifySnaException.cause}"
      else -> "Unexpected Error"
    }
  }
}