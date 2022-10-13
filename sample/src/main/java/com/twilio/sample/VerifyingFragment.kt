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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.twilio.sample.databinding.FragmentVerifyingBinding

class VerifyingFragment : Fragment() {

  private lateinit var binding: FragmentVerifyingBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    binding = FragmentVerifyingBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.backButton.setOnClickListener {
      findNavController().popBackStack()
    }

    // todo: call verify sna here
    binding.success.setOnClickListener {
      val action =
        VerifyingFragmentDirections.actionVerifyingFragmentToVerificationSuccessfulFragment()
      view.findNavController().navigate(action)
    }
    binding.fail.setOnClickListener {
      val action = VerifyingFragmentDirections.actionVerifyingFragmentToVerificationFailedFragment()
      view.findNavController().navigate(action)
    }
  }
}
