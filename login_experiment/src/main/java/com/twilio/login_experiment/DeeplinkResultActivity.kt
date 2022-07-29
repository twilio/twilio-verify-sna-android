package com.twilio.login_experiment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DeeplinkResultActivity : AppCompatActivity() {

  private lateinit var service: Service
  private lateinit var resultText: TextView
  private var code = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_deeplink_result)

    val retrofit = Retrofit.Builder()
      .baseUrl(getString(R.string.request_evurl_url))
      .addConverterFactory(GsonConverterFactory.create())
      .build()
    service = retrofit.create(Service::class.java)

    resultText = findViewById(R.id.resultText)
    code = intent.data?.getQueryParameter("code").toString()
    if (code.isNotEmpty()) {
      resultText.text = "code: ${intent.data?.getQueryParameter("code")}"
      invokeGetAccessToken()
    }
  }

  private fun showSuccess() {
    findViewById<TextView>(R.id.title).visibility = View.VISIBLE
  }

  private fun invokeGetAccessToken() {
    CoroutineScope(Job()).launch(Dispatchers.Main) {

      try {
        val accessTokenResponse = withContext(Dispatchers.IO) {
          service.requestAccessToken(
            clientId = getString(R.string.client_id),
            clientSecret = getString(R.string.application_seed_secret),
            code = code
          )
        }
        resultText.text =
          resultText.text.toString() + "\nAccess token: ${accessTokenResponse.accessToken}"
        showSuccess()
      } catch (exception: Exception) {
        resultText.text =
          resultText.text.toString() + "\nError getting access token: ${exception.message}"
      }
    }
  }
}
