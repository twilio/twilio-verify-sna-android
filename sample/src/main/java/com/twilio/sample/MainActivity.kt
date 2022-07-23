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

package com.twilio.sample

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.twilio.sample.databinding.ActivityMainBinding
import com.twilio.verify_sna.ProcessUrlResult
import com.twilio.verify_sna.TwilioVerifySna

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    viewModel.getVerificationResult().observe(this) { viewState ->
      updateUI(viewState)
    }

    viewModel.setTwilioVerifySna(TwilioVerifySna.Builder(this).build())

    binding.button.setOnClickListener {
      invokeVerification()
    }
  }

  private fun invokeVerification() {
    viewModel.verify("https://google.com")
  }

  private fun updateUI(viewState: ViewState) {
    when (viewState) {
      is ViewState.Loading -> showLoading()
      is ViewState.VerificationSuccess -> showVerificationResult(viewState.verificationResult)
      is ViewState.VerificationFail -> showVerificationFail(viewState.message)
      else -> showIdle()
    }
  }

  private fun showLoading() {
    binding.progressBar.visibility = View.VISIBLE
    binding.textView.visibility = View.GONE
  }

  private fun showVerificationResult(verificationResult: ProcessUrlResult) {
    binding.progressBar.visibility = View.GONE
    binding.textView.run {
      visibility = View.VISIBLE
      text = when (verificationResult) {
        is ProcessUrlResult.Success -> "Code: ${verificationResult.networkRequestResult.status}, content: ${verificationResult.networkRequestResult.message}"
        is ProcessUrlResult.Fail -> "Error: ${verificationResult.twilioVerifySnaException.message}"
      }
    }
  }

  private fun showVerificationFail(message: String) {
    binding.progressBar.visibility = View.GONE
    binding.textView.run {
      visibility = View.VISIBLE
      text = message
    }
  }

  private fun showIdle() {
    binding.progressBar.visibility = View.GONE
    binding.textView.visibility = View.GONE
  }
}
