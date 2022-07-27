package com.twilio.login_experiment

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DeeplinkResultActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_deeplink_result)

    val resultText = findViewById<TextView>(R.id.resultText)
    resultText.text = "code: ${intent.data?.getQueryParameter("code")}"
  }
}
