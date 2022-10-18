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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.twilio.sample.data.SampleRepository
import com.twilio.verify_sna.ProcessUrlResult
import com.twilio.verify_sna.TwilioVerifySna
import com.twilio.verify_sna.sample.databinding.FragmentVerifyingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerifyingFragment : Fragment() {

  private lateinit var binding: FragmentVerifyingBinding

  private val args: VerifyingFragmentArgs by navArgs()

  // Create TwilioVerifySna instance using builder
  private val twilioVerifySna: TwilioVerifySna by lazy {
    TwilioVerifySna.Builder(requireContext()).build()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentVerifyingBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.backButton.setOnClickListener {
      findNavController().popBackStack()
    }
    // Start verifying the user after the view setup is done
    invokeVerifySna()
  }

  private fun invokeVerifySna() {
    lifecycleScope.launch {
      // Switching to a background thread
      val snaUrl = withContext(Dispatchers.IO) {
        // Get SNA URL from Backend URL sending the phone number
        try {
          SampleRepository.getSnaUrl(
            args.backendUrl, "+1${args.phoneNumber}"
          )
        } catch (e: Exception) {
          e.printStackTrace()
          null
        }
      }
      if (snaUrl.isNullOrEmpty()) {
        onFail()
        return@launch
      }
      // Switching to a background thread
      val result = withContext(Dispatchers.IO) {
        // Consume SNA URL using the SDK
        twilioVerifySna.processUrl(snaUrl)
      }
      // Validate the result
      if (result is ProcessUrlResult.Success) {
        onSuccess()
      } else {
        // The validation gets a result equals to ProcessUrlResult.Fail
        onFail()
      }
    }
  }

  /**
   * Redirect to successful validation screen
   */
  private fun onSuccess() {
    val action =
      VerifyingFragmentDirections.actionVerifyingFragmentToVerificationSuccessfulFragment()
    findNavController().navigate(action)
  }

  /**
   * Redirect to failed validation screen
   */
  private fun onFail() {
    val action = VerifyingFragmentDirections.actionVerifyingFragmentToVerificationFailedFragment()
    findNavController().navigate(action)
  }
}
