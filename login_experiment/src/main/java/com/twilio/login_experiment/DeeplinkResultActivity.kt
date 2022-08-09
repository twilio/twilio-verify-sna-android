package com.twilio.login_experiment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
  private var code = ""
  private lateinit var title: TextView
  private lateinit var myBalance: Button
  private lateinit var settings: Button
  private lateinit var transfer: Button
  private lateinit var products: Button
  private lateinit var progressBar: ProgressBar
  private lateinit var logout: ImageView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_deeplink_result)

    title = findViewById(R.id.owlBankTitle)
    myBalance = findViewById(R.id.myBalance)
    settings = findViewById(R.id.settings)
    transfer = findViewById(R.id.transfer)
    products = findViewById(R.id.products)
    progressBar = findViewById(R.id.progressBar)

    val retrofit = Retrofit.Builder()
      .baseUrl(getString(R.string.request_evurl_url))
      .addConverterFactory(GsonConverterFactory.create())
      .build()
    service = retrofit.create(Service::class.java)

    code = intent.data?.getQueryParameter("code").toString()
    if (code.isNotEmpty()) {
      invokeGetAccessToken()
    }

    logout = findViewById(R.id.logoutIcon)
    logout.setOnClickListener {
      performLogout()
    }
  }

  private fun stopLoading() {
    title.visibility = View.VISIBLE
    myBalance.visibility = View.VISIBLE
    settings.visibility = View.VISIBLE
    transfer.visibility = View.VISIBLE
    products.visibility = View.VISIBLE
    progressBar.visibility = View.GONE
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
        Toast.makeText(
          this@DeeplinkResultActivity,
          "Success! Access token: ${accessTokenResponse.accessToken}",
          Toast.LENGTH_LONG,
        ).show()
      } catch (exception: Exception) {
        Toast.makeText(
          this@DeeplinkResultActivity,
          "Error getting access token: ${exception.message}",
          Toast.LENGTH_LONG,
        ).show()
      } finally {
        stopLoading()
      }
    }
  }

  private fun performLogout() {
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
    finish()
  }
}
