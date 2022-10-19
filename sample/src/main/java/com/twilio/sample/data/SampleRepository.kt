package com.twilio.sample.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Static repository to consume the provided URL
 */
object SampleRepository {

  /**
   * This repo uses retrofit library
   */
  private val retrofit = Retrofit.Builder()
    .baseUrl("http://foo.bar") // the URL is override by @Url param
    .addConverterFactory(GsonConverterFactory.create())
    .build()

  private val sampleApi: SampleApi = retrofit.create(SampleApi::class.java)

  /**
   * Obtain SNA URL from backendUrl and sending phoneNumber.
   */
  suspend fun getSnaUrl(backendUrl: String, phoneNumber: String): String? {
    return sampleApi.getSnaUrl(backendUrl, phoneNumber).snaUrl
  }
}

interface SampleApi {
  @POST
  @FormUrlEncoded
  suspend fun getSnaUrl(
    @Url backendUrl: String,
    @Field("phoneNumber") phoneNumber: String
  ): SnaUrlResponse
}

/**
 * Model that represents the Backend URL response
 */
data class SnaUrlResponse(val snaUrl: String?)
