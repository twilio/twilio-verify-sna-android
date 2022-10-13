package com.twilio.sample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.twilio.sample.model.SampleViewModel
import com.twilio.sample.model.UIState
import com.twilio.verify_sna.databinding.FragmentWelcomeBinding
import kotlinx.coroutines.launch

class WelcomeFragment : Fragment() {

  private lateinit var binding: FragmentWelcomeBinding

  private val viewModel: SampleViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentWelcomeBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    observeUiState()
    binding.verifySnaUrlButton.setOnClickListener {
      val countryCode = binding.countryCodeSpinner.text.toString()
      val phoneNumber = binding.phoneNumberField.text.toString()
      val backendUrl = binding.backendUrlField.text.toString()
      viewModel.submit(countryCode, phoneNumber, backendUrl)
    }
  }

  private fun observeUiState() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { uiState ->
          when (uiState) {
            is UIState.GenericError -> showError(uiState.message)
            is UIState.ToggleLoader -> navigateToLoader()
          }
        }
      }
    }
  }

  private fun showError(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
  }

  private fun navigateToLoader() {
    val action =
      WelcomeFragmentDirections.actionWelcomeFragmentToVerifyingFragment()
    view?.findNavController()?.navigate(action)
  }
}
