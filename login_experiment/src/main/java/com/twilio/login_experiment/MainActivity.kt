package com.twilio.login_experiment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    findViewById<Button>(R.id.loginWithAuthy).setOnClickListener {
      val intent = Intent(this, LoginWithAuthyActivity::class.java)
      startActivity(intent)
    }

    findViewById<Button>(R.id.loginButton).setOnClickListener {
      showNotAvailableMessage()
    }

    findViewById<Button>(R.id.loginWithGoogle).setOnClickListener {
      showNotAvailableMessage()
    }
  }

  private fun showNotAvailableMessage() {
    Toast.makeText(this, "Not available yet", Toast.LENGTH_SHORT).show()
  }
}
