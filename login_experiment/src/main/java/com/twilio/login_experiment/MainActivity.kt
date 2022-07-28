package com.twilio.login_experiment

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.twilio.verify_sna.TwilioVerifySna
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

  private lateinit var service: Service
  private lateinit var twilioVerifySna: TwilioVerifySna
  private lateinit var button: Button
  private lateinit var progressBar: ProgressBar
  private lateinit var outputText: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val retrofit = Retrofit.Builder()
      .baseUrl(getString(R.string.request_evurl_url))
      .addConverterFactory(GsonConverterFactory.create())
      .build()
    service = retrofit.create(Service::class.java)

    twilioVerifySna = TwilioVerifySna.Builder(this).build()

    button = findViewById(R.id.loginWithAuthy)
    progressBar = findViewById(R.id.progressBar)
    outputText = findViewById(R.id.outputText)

    button.setOnClickListener {
      performLogin()
    }
  }

  private fun performLogin() {
    CoroutineScope(Job()).launch(Dispatchers.Main) {
      startLoading()
      val countryCode = findViewById<EditText>(R.id.countryCodeField).text.toString()
      val phoneNumber = findViewById<EditText>(R.id.phoneNumberField).text.toString()
      outputText.text = "Consuming /sna/create_evurl"
      val requestSnaResponse = withContext(Dispatchers.IO) {
        service.requestSnaUrl(countryCode, phoneNumber, getString(R.string.client_id))
      }
      outputText.text = "Consuming Sna Url"
      withContext(Dispatchers.IO) {
        twilioVerifySna.processUrl(requestSnaResponse.snaUrl)
      }
      outputText.text = "Sna Url response successful"
      openAuthorizeWebPage(
        clientId = getString(R.string.client_id),
        countryCode = getString(R.string.silent_auth_country_code),
        phoneNumber = getString(R.string.silent_auth_phone_number),
        associationKey = requestSnaResponse.correlationId,
        redirectUrl = "loginexperiment://loginexperiment.twilio.com"
      )
      stopLoading()
    }
  }

  private fun startLoading() {
    button.isEnabled = false
    progressBar.visibility = View.VISIBLE
    outputText.text = ""
  }

  private fun openAuthorizeWebPage(
    clientId: String,
    countryCode: String,
    phoneNumber: String,
    associationKey: String,
    redirectUrl: String
  ) {
    val url =
      "${getString(R.string.silent_auth_url)}?client_id=$clientId&country_code=$countryCode&phone_number=$phoneNumber&association_key=$associationKey&redirect_url=$redirectUrl"
    startCustomTab(url)
  }

  private fun stopLoading() {
    button.isEnabled = true
    progressBar.visibility = View.INVISIBLE
  }

  private fun startCustomTab(url: String) {
    val customTabsIntent = CustomTabsIntent.Builder()
      .apply {
        title = "Silent-auth Authorize webpage"
      }
      .build()

    customTabsIntent.intent.data = Uri.parse(url)
    startActivity(customTabsIntent.intent)
  }
}
