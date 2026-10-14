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
package com.twilio.verify.sna.sample

import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.twilio.verify.sna.sample.databinding.FragmentWelcomeBinding
import com.twilio.verify_sna.networking.IsMobileDataEnabledHelper
import com.twilio.verify_sna.networking.IsMobileDataEnabledHelperImpl

private const val PHONE_NUMBER_KEY = "phoneNumber"
private const val BACKEND_URL_KEY = "backendUrl"

class WelcomeFragment : Fragment() {

  private lateinit var binding: FragmentWelcomeBinding

  private val isMobileDataEnabledHelper: IsMobileDataEnabledHelper by lazy {
    IsMobileDataEnabledHelperImpl(requireContext().applicationContext)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentWelcomeBinding.inflate(inflater, container, false)
    binding.pill.text = getString(
      R.string.test_app_with_versions_pill,
      BuildConfig.VERSION_NAME,
      BuildConfig.VERSION_CODE.toString()
    )
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    readValuesFromPreferences()
    binding.verifySnaUrlButton.setOnClickListener {
      submit()
    }
  }

  private fun readValuesFromPreferences() {
    val preferences = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
    binding.phoneNumberField.setText(preferences.getString(PHONE_NUMBER_KEY, ""))
    binding.backendUrlField.setText(preferences.getString(BACKEND_URL_KEY, ""))
  }

  private fun submit() {
    val countryCode = binding.countryCodeSpinner.text.toString()
    val phoneNumber = binding.phoneNumberField.text.toString()
    val backendUrl = binding.backendUrlField.text.toString()
    if (countryCode.isEmpty() || phoneNumber.isEmpty() || backendUrl.isEmpty()) {
      showErrorMessage(R.string.missing_field_error)
      return
    }
    // save in cache the phone number and backend URL
    saveInPreferences(phoneNumber, backendUrl)
    when (cellularCoverageStatus()) {
      CellularCoverageStatus.SIM_NOT_READY -> {
        showErrorMessage(R.string.sim_not_ready_error)
        return
      }
      CellularCoverageStatus.MOBILE_DATA_DISABLED -> {
        showErrorMessage(R.string.mobile_data_disabled_error)
        return
      }
      CellularCoverageStatus.AVAILABLE -> Unit
    }
    val action = WelcomeFragmentDirections
      .actionWelcomeFragmentToVerifyingFragment(
        "+$countryCode$phoneNumber", backendUrl
      )
    findNavController().navigate(action)
  }

  private fun showErrorMessage(messageRes: Int) {
    Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
  }

  /**
   * Verifies cellular network is on, returning the specific reason when it isn't so the user
   * can be told what to do about it.
   */
  private fun cellularCoverageStatus(): CellularCoverageStatus {
    val telephonyManager =
      requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY) {
      return CellularCoverageStatus.SIM_NOT_READY
    }

    return if (isMobileDataEnabledHelper()) {
      CellularCoverageStatus.AVAILABLE
    } else {
      CellularCoverageStatus.MOBILE_DATA_DISABLED
    }
  }

  private fun saveInPreferences(phoneNumber: String, backendUrl: String) {
    val preferences = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
    with(preferences.edit()) {
      putString(PHONE_NUMBER_KEY, phoneNumber)
      putString(BACKEND_URL_KEY, backendUrl)
      apply()
    }
  }

  private enum class CellularCoverageStatus {
    AVAILABLE,
    SIM_NOT_READY,
    MOBILE_DATA_DISABLED
  }
}
