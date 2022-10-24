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

  /**
   * The function with perform the following tasks in order:
   * 1. Get the SNA URL using the backend URL provided.
   * 2. Invoke the SNA SDK to ask for verifying the phone number provided.
   * 3. Check with the backend URL provided if verification was successful.
   */
  private fun invokeVerifySna() {
    lifecycleScope.launch {
      val snaUrl = getSnaUrl(args.backendUrl, args.phoneNumber)
      if (snaUrl.isNullOrEmpty()) {
        onFail()
        return@launch
      }
      val result = invokeSnaSdk(snaUrl)
      // Validate the result
      if (result !is ProcessUrlResult.Success) {
        // The validation gets a result equals to ProcessUrlResult.Fail
        onFail()
        return@launch
      }
      val verificationResult = checkVerification()
      if (verificationResult) {
        // The phone number was verified
        onSuccess()
      } else {
        onFail()
      }
    }
  }

  private suspend fun getSnaUrl(backendUrl: String, phoneNumber: String): String? {
    // Switching to a background thread
    val snaUrl = withContext(Dispatchers.IO) {
      try {
        // Get SNA URL from Backend URL sending the phone number
        SampleRepository.getSnaUrl(
          backendUrl, phoneNumber
        )
      } catch (e: Exception) {
        // An exception was thrown getting the SNA URL
        null
      }
    }
    return snaUrl
  }

  private suspend fun invokeSnaSdk(snaUrl: String): ProcessUrlResult {
    // Switching to a background thread
    val result = withContext(Dispatchers.IO) {
      // Consume SNA URL using the SDK
      twilioVerifySna.processUrl(snaUrl)
    }
    return result
  }

  private suspend fun checkVerification(): Boolean {
    // Switching to a background thread
    val verificationResult = withContext(Dispatchers.IO) {
      try {
        SampleRepository.checkVerification(
          args.backendUrl, args.phoneNumber
        )
      } catch (e: Exception) {
        // An exception was thrown checking the verification status}
        false
      }
    }
    return verificationResult
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
