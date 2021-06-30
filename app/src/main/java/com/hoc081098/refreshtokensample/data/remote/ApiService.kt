package com.hoc081098.refreshtokensample.data.remote

import com.hoc081098.refreshtokensample.data.remote.body.LoginBody
import com.hoc081098.refreshtokensample.data.remote.body.RefreshTokenBody
import com.hoc081098.refreshtokensample.data.remote.response.LoginResponse
import com.hoc081098.refreshtokensample.data.remote.response.RefreshTokenResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
  @Headers("$CUSTOM_HEADER: $NO_AUTH")
  @POST("login")
  suspend fun login(@Body() loginBody: LoginBody): LoginResponse

  @Headers("$CUSTOM_HEADER: $NO_AUTH")
  @POST("refresh-token")
  suspend fun refreshToken(@Body() refreshToken: RefreshTokenBody): Response<RefreshTokenResponse>

  @GET("check-auth")
  suspend fun checkAuth()

  @GET("demo")
  suspend fun demo(@Query("count") count: Int): String

  companion object Factory {
    operator fun invoke(retrofit: Retrofit): ApiService = retrofit.create()

    const val CUSTOM_HEADER = "@"
    const val NO_AUTH = "NoAuth"
  }
}
