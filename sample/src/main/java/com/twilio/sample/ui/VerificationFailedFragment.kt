package com.twilio.sample.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.twilio.sample.model.SampleViewModel
import com.twilio.verify_sna.R
import com.twilio.verify_sna.databinding.FragmentVerificationFailedBinding

class VerificationFailedFragment : Fragment() {

  private lateinit var binding: FragmentVerificationFailedBinding

  private val viewModel: SampleViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentVerificationFailedBinding.inflate(inflater,container,false)
    return binding.root
  }
}
