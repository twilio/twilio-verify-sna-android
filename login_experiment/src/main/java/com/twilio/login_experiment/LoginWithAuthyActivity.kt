package com.twilio.login_experiment

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.FragmentActivity
import com.twilio.login_experiment.R.string
import com.twilio.verify_sna.TwilioVerifySna
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginWithAuthyActivity : FragmentActivity(), CountryOnClickListener {

  private lateinit var service: Service
  private lateinit var twilioVerifySna: TwilioVerifySna
  private lateinit var button: Button
  private lateinit var progressBar: ProgressBar
  private lateinit var countryCodeField: EditText

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login_with_authy)

    setupCountryCodeView()

    val retrofit = Retrofit.Builder()
      .baseUrl(getString(R.string.request_evurl_url))
      .addConverterFactory(GsonConverterFactory.create())
      .build()
    service = retrofit.create(Service::class.java)

    twilioVerifySna = TwilioVerifySna.Builder(this).build()

    button = findViewById(R.id.phoneNumberLoginButton)
    progressBar = findViewById(R.id.progressBar)

    button.setOnClickListener {
      performLogin()
    }
  }

  private fun setupCountryCodeView() {
    countryCodeField = findViewById(R.id.countryCodeSpinner)

    countryCodeField.setOnClickListener {
      CountrySelectorDialogFragment().show(supportFragmentManager, "countries")
    }
  }

  private fun performLogin() {
    CoroutineScope(Job()).launch(Dispatchers.Main) {
      try {
        startLoading()
        val countryCode = countryCodeField.text.toString()
        val phoneNumber = findViewById<EditText>(R.id.phoneNumberField).text.toString()

        val requestSnaResponse = withContext(Dispatchers.IO) {
          service.requestSnaUrl(countryCode, phoneNumber, getString(string.client_id))
        }

        withContext(Dispatchers.IO) {
          twilioVerifySna.processUrl(requestSnaResponse.snaUrl)
        }

        openAuthorizeWebPage(
          clientId = getString(R.string.client_id),
          countryCode = countryCode,
          phoneNumber = phoneNumber,
          associationKey = requestSnaResponse.correlationId,
          redirectUrl = "loginexperiment://loginexperiment.twilio.com"
        )
      } catch (exception: Exception) {
        Toast.makeText(
          this@LoginWithAuthyActivity,
          "Error: ${exception.message}",
          Toast.LENGTH_LONG
        ).show()

      } finally {
        stopLoading()
      }
    }
  }

  private fun startLoading() {
    progressBar.visibility = View.VISIBLE
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

  override fun onClick(code: String) {
    countryCodeField.setText(code)
  }
}