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
import com.twilio.verify_sna.TwilioVerifySna
import com.twilio.verify_sna.domain.VerificationResult

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
    viewModel.verify("https://f656-2800-e2-1a00-68c-94ea-5f64-c2c1-a188.ngrok.io")
  }

  private fun updateUI(viewState: ViewState) {
    when (viewState) {
      is ViewState.Loading -> showLoading()
      is ViewState.VerificationDone -> showVerificationResult(viewState.verificationResult)
      else -> showIdle()
    }
  }

  private fun showLoading() {
    binding.progressBar.visibility = View.VISIBLE
    binding.textView.visibility = View.GONE
  }

  private fun showVerificationResult(verificationResult: VerificationResult) {
    binding.progressBar.visibility = View.GONE
    binding.textView.run {
      visibility = View.VISIBLE
      text = when (verificationResult) {
        is VerificationResult.Success -> "Code: ${verificationResult.snaResponse.code}, message: ${verificationResult.snaResponse.message}"
        is VerificationResult.Fail -> "Error: ${verificationResult.verifySnaException.message}"
      }
    }
  }

  private fun showIdle() {
    binding.progressBar.visibility = View.GONE
    binding.textView.visibility = View.GONE
  }
}
