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
package com.twilio.sample.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twilio.sample.model.UIState.GenericError
import com.twilio.sample.model.UIState.Idle
import com.twilio.sample.model.UIState.ToggleLoader
import com.twilio.verify_sna.TwilioVerifySna
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UIState {
  object Idle : UIState
  data class GenericError(val message: String) : UIState
  object ToggleLoader : UIState
}

@ActivityScoped
class SampleViewModel @Inject constructor(
  private val twilioVerifySna: TwilioVerifySna
) : ViewModel() {

  private var _uiState = MutableStateFlow<UIState>(Idle)
  val uiState: StateFlow<UIState> = _uiState

  fun submit(countryCode: String, phoneNumber: String, backendUrl: String) {
    _uiState.value = Idle
    viewModelScope.launch {
      if (isAnyEmpty(countryCode, phoneNumber, backendUrl)) {
        _uiState.emit(
          GenericError("Missing country code, phone number or backend URL.")
        )
        return@launch
      }
      if (!hasCellularCoverage()) {
        _uiState.emit(
          GenericError(
            "For silent network authentication, you will need a working sim card."
          )
        )
        return@launch
      }
      _uiState.emit(ToggleLoader)
    }
  }

  private fun isAnyEmpty(countryCode: String, phoneNumber: String, backendUrl: String): Boolean {
    return countryCode.isEmpty() || phoneNumber.isEmpty() || backendUrl.isEmpty()
  }

  private fun hasCellularCoverage(): Boolean {
    return true
  }

  fun verify() {
    _uiState.value = Idle
    viewModelScope.launch {
      println(twilioVerifySna.processUrl(""))
    }
  }
}
