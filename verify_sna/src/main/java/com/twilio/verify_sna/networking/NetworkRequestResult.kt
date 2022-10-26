package com.twilio.verify_sna.networking

data class NetworkRequestResult(
  /**
   * Http response status
   */
  val status: Int,
  /**
   * Contains the result message of the verification
   */
  val message: String?
)
