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
import androidx.appcompat.app.AppCompatActivity
import com.twilio.sample.databinding.ActivityMainBinding
import com.twilio.verify_sna.ProcessUrlResult
import com.twilio.verify_sna.TwilioVerifySna
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  // Create TwilioVerifySna instance using builder
  private val twilioVerifySna: TwilioVerifySna by lazy {
    TwilioVerifySna.Builder(this).build()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.verifySnaUrlButton.setOnClickListener {
      val snaUrl = binding.snaUrlField.text.toString()
      invokeVerification(snaUrl)
    }
  }

  private fun invokeVerification(snaUrl: String) {
    // Start loading animation
    startLoading()
    CoroutineScope(Job()).launch(Dispatchers.Main) {
      // Consume SNA URL verification in background thread
      val processUrlResult = withContext(Dispatchers.IO) {
        twilioVerifySna.processUrl(snaUrl)
      }

      binding.result.text = when (processUrlResult) {
        is ProcessUrlResult.Success -> "Success"
        is ProcessUrlResult.Fail -> {
          "Fail: ${processUrlResult.twilioVerifySnaException.message}"
        }
      }
      // Stop loading animation
      stopLoading()
    }
  }

  private fun startLoading() {
    binding.run {
      verifySnaUrlButton.visibility = View.GONE
      progressBar.visibility = View.VISIBLE
      result.visibility = View.GONE
    }
  }

  private fun stopLoading() {
    binding.run {
      verifySnaUrlButton.visibility = View.VISIBLE
      progressBar.visibility = View.GONE
      result.visibility = View.VISIBLE
    }
  }
}
