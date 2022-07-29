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

import com.twilio.login_experiment.model.AccessTokenResponse
import com.twilio.login_experiment.model.RequestSnaResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface Service {

  @FormUrlEncoded
  @POST("/sna/create_evurl")
  suspend fun requestSnaUrl(
    @Field("countryCode") countryCode: String,
    @Field("phoneNumber") phoneNumber: String,
    @Field("clientId") clientId: String
  ): RequestSnaResponse

  @FormUrlEncoded
  @POST("/oauth/token")
  suspend fun requestAccessToken(
    @Field("clientId") clientId: String,
    @Field("clientSecret") clientSecret: String,
    @Field("code") code: String
  ): AccessTokenResponse
}
