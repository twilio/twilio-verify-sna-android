package com.twilio.verify_sna.networking

data class NetworkRequestResult(
  /**
   * Http response status
   */
  val status: Int,
  /**
   * Property used for debug only
   */
  val message: String?
)
