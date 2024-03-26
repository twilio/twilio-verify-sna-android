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

package com.twilio.verify_sna

import android.content.Context
import com.twilio.verify_sna.domain.requestmanager.ConcreteRequestManager
import com.twilio.verify_sna.domain.requestmanager.RequestManager
import com.twilio.verify_sna.domain.twilioverifysna.ConcreteTwilioVerifySna
import com.twilio.verify_sna.networking.ConcreteNetworkRequestProvider

/**
 * Describes the available operations to process  SNA verification
 */
interface TwilioVerifySna {

  /**
   * Consume the required SNA URL using cellular network
   * @param snaUrl Silent phone number authentication URL
   */
  suspend fun processUrl(snaUrl: String): ProcessUrlResult

  class Builder(
    context: Context
  ) {

    private var requestManager: RequestManager = ConcreteRequestManager(
      context, ConcreteNetworkRequestProvider()
    )

    internal fun requestManager(
      requestManager: RequestManager
    ) = apply { this.requestManager = requestManager }

    fun logger(logger: (String) -> Unit) = apply {
      requestManager.logger = logger
    }

    /**
     * Builds an instance of TwilioVerifySna
     */
    fun build(): TwilioVerifySna {
      return ConcreteTwilioVerifySna(requestManager)
    }
  }
}
