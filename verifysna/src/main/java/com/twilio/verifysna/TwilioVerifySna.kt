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

package com.twilio.verifysna

import android.content.Context
import com.twilio.verifysna.domain.ConcreteRequestManager
import com.twilio.verifysna.domain.RequestManager
import com.twilio.verifysna.domain.VerificationResult
import com.twilio.verifysna.network.ConcreteCellularNetworkConnection

class TwilioVerifySna private constructor(
  private val context: Context,
  private val requestManager: RequestManager
) {

  fun processUrl(url: String): VerificationResult {
    requestManager.processUrl(url)
    return VerificationResult.Success
  }

  data class Builder(
    private val context: Context,
    private var requestManager: RequestManager = ConcreteRequestManager(
      ConcreteCellularNetworkConnection(context)
    )
  ) {

    fun requestManager(
      requestManager: RequestManager
    ) = apply { this.requestManager = requestManager }

    fun build(): TwilioVerifySna {
      return TwilioVerifySna(context, requestManager)
    }
  }
}
