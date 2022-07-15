package com.twilio.sample

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.twilio.sample.databinding.ActivityWebviewBinding

class WebViewActivity : AppCompatActivity() {

  lateinit var binding: ActivityWebviewBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityWebviewBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val webView = binding.webView
    webView.webViewClient = object : WebViewClient() {
      override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
        view?.loadUrl(url)
        return true
      }
    }

    webView.settings.run {
      loadsImagesAutomatically = true
      javaScriptEnabled = true
    }
    webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    webView.loadUrl("https://4adbdbd49a83.ngrok.io/dummy")
  }
}
