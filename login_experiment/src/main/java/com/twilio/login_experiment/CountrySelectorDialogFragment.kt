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
package com.twilio.login_experiment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView

class CountrySelectorDialogFragment :
  DialogFragment(R.layout.dialog_country_selector),
  CountryOnClickListener {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val countries = listOf(
      Country("United States", "1"),
      Country("Colombia", "57")
    )
    val countryAdapter = CountryAdapter(countries, this)

    view.findViewById<RecyclerView>(R.id.countryList).run {
      adapter = countryAdapter
    }
  }

  override fun onClick(code: String) {
    (context as CountryOnClickListener).onClick(code)
    dismiss()
  }
}
