package com.twilio.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.twilio.sample.databinding.ActivityDeeplinkResultBinding

class DeeplinkResultActivity : AppCompatActivity() {

  private lateinit var binding: ActivityDeeplinkResultBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDeeplinkResultBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.result.text = "code: ${intent.data?.getQueryParameter("code")}"
  }
}
