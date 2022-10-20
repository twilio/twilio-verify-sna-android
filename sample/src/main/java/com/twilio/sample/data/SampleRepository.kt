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

  private val sampleApi: SampleApi

  init {
    // This repo uses retrofit library
    val retrofit = Retrofit.Builder()
      .baseUrl("http://foo.bar") // the URL is override by @Url param
      .addConverterFactory(GsonConverterFactory.create())
      .build()
    sampleApi = retrofit.create(SampleApi::class.java)
  }

  /**
   * Obtain SNA URL from backendUrl and sending phoneNumber.
   */
  suspend fun getSnaUrl(backendUrl: String, phoneNumber: String): String? {
    return sampleApi.getSnaUrl(backendUrl + START_VERIFICATION, phoneNumber).snaUrl
  }

  /**
   * Obtain SNA URL from backendUrl and sending phoneNumber.
   */
  suspend fun checkVerification(backendUrl: String, phoneNumber: String): Boolean {
    return sampleApi.checkVerification(backendUrl + CHECK_VERIFICATION, phoneNumber).success
  }
}

/**
 * Custom backend endpoints:
 * If you create your own backend using a Twilio template, this endpoints will be used
 * for your validations, otherwise you should use your own backend implementation
 */
const val START_VERIFICATION = "/verify-start"
const val CHECK_VERIFICATION = "/verify-check"

interface SampleApi {
  @POST
  @FormUrlEncoded
  suspend fun getSnaUrl(
    @Url backendUrl: String,
    @Field("phoneNumber") phoneNumber: String
  ): SnaUrlResponse

  @POST
  @FormUrlEncoded
  suspend fun checkVerification(
    @Url backendUrl: String,
    @Field("phoneNumber") phoneNumber: String
  ): VerificationResult
}

/**
 * Model that represents the Backend URL response
 */
data class SnaUrlResponse(val snaUrl: String?)

/**
 * Model that represents the backend response when checking a verification
 */
data class VerificationResult(val success: Boolean)
