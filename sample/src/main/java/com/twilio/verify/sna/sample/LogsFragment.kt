/*
 * Copyright (c) 2026 Twilio Inc.
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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.twilio.verify.sna.sample.databinding.FragmentLogsBinding
import com.twilio.verify_sna.common.Logger

/**
 * Shows the logs of the latest verification, written by the SDK and by this sample.
 */
class LogsFragment : Fragment() {

  private lateinit var binding: FragmentLogsBinding

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentLogsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val logs = Logger.getText()
    binding.logsText.text = logs.ifEmpty { getString(R.string.no_logs) }
    binding.copyButton.isEnabled = logs.isNotEmpty()
    binding.copyButton.setOnClickListener { copyToClipboard(logs) }
    binding.backButton.setOnClickListener { findNavController().navigateUp() }
  }

  private fun copyToClipboard(logs: String) {
    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.logs), logs))
    Toast.makeText(requireContext(), R.string.logs_copied, Toast.LENGTH_SHORT).show()
  }
}
