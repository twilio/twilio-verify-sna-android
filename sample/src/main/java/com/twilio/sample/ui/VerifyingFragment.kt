package com.twilio.sample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.twilio.sample.model.SampleViewModel
import com.twilio.verify_sna.databinding.FragmentVerifyingBinding

class VerifyingFragment : Fragment() {

  private lateinit var binding: FragmentVerifyingBinding

  private val viewModel: SampleViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentVerifyingBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.backButton.setOnClickListener {
      findNavController().popBackStack()
    }
    viewModel.verify()
  }
}
