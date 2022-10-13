package com.twilio.sample.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.twilio.verify_sna.R
import com.twilio.verify_sna.databinding.FragmentVerificationSuccessfulBinding

class VerificationSuccessfulFragment : Fragment() {

  private lateinit var binding: FragmentVerificationSuccessfulBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentVerificationSuccessfulBinding.inflate(inflater,container,false)
    return binding.root
  }
}
