/*
 * Copyright (c) 2022 Twilio Inc.
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

package com.twilio.sample

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.twilio.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    binding.button.setOnClickListener {
      val url = binding.editText.text.toString()
      if (url.isNotEmpty()) {
        startCustomTab(url)
      }
    }
    setContentView(binding.root)
  }

  private fun startCustomTab(url: String) {
    val customTabsIntent = CustomTabsIntent.Builder()
      .apply {
        title = "Example in-app browser"
        setDefaultColorSchemeParams(
          CustomTabColorSchemeParams.Builder()
            .setToolbarColor(
              ContextCompat.getColor(this@MainActivity, R.color.red)
            )
            .build()
        )
      }
      .build()

    customTabsIntent.intent.data = Uri.parse(url)
    startActivity(customTabsIntent.intent)
  }
}
