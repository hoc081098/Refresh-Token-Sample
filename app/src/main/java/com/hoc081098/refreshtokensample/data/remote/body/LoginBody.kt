package com.hoc081098.refreshtokensample.data.remote.body

import com.squareup.moshi.Json

data class LoginBody(
  @Json(name = "username") val username: String,
  @Json(name = "password") val password: String
)
