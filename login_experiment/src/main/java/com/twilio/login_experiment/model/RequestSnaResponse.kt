package com.twilio.login_experiment.model

import com.google.gson.annotations.SerializedName

data class RequestSnaResponse(
  @SerializedName("evurl")
  val snaUrl: String,
  val correlationId: String
)
