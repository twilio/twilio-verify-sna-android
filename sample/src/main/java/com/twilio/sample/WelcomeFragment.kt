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

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.twilio.verify_sna.sample.R
import com.twilio.verify_sna.sample.databinding.FragmentWelcomeBinding
import java.lang.reflect.Method

private const val PHONE_NUMBER_KEY = "phoneNumber"
private const val BACKEND_URL_KEY = "backendUrl"

class WelcomeFragment : Fragment() {

  private lateinit var binding: FragmentWelcomeBinding

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentWelcomeBinding.inflate(inflater, container, false)
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
    if (!hasCellularCoverage()) {
      showErrorMessage(R.string.cellular_network_required)
      return
    }
    val action = WelcomeFragmentDirections.actionWelcomeFragmentToVerifyingFragment(
      "$countryCode$phoneNumber", backendUrl
    )
    findNavController().navigate(action)
  }

  private fun showErrorMessage(messageRes: Int) {
    Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
  }

  /**
   * Verifies cellular network is on
   */
  private fun hasCellularCoverage(): Boolean {
    val connectivityManager = requireContext().getSystemService(
      Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager

    return isMobileDataEnabled(connectivityManager)
  }

  /**
   * Android Framework doesn't count with a pre-build way of getting mobile network status,
   * when Wi-Fi is active. Reflection fits well.
   * Taken from https://stackoverflow.com/a/8243305
   */
  private fun isMobileDataEnabled(cm: ConnectivityManager): Boolean {
    val telephonyManager = requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY) {
      return false
    }

    return try {
      val c = Class.forName(cm.javaClass.name)
      val m: Method = c.getDeclaredMethod("getMobileDataEnabled")
      m.isAccessible = true
      m.invoke(cm) as Boolean
    } catch (exception: Exception) {
      exception.printStackTrace()
      false
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
}
