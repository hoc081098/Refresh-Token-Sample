package com.hoc081098.refreshtokensample.data.remote

import com.hoc081098.refreshtokensample.data.remote.response.LoginResponse
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.POST

interface ApiService {
  @POST("login")
  fun login(): LoginResponse

  companion object Factory {
    operator fun invoke(retrofit: Retrofit): ApiService = retrofit.create()
  }
}