package com.hoc081098.refreshtokensample.data.remote.interceptor

import com.hoc081098.refreshtokensample.data.local.UserLocalSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

class AuthInterceptor(
  private val userLocalSource: UserLocalSource,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val req = chain.request()
    val newReq = when {
      "NoAuth" in req.headers.values(customHeader) -> req.newBuilder()
        .removeHeader(customHeader)
        .build()
      else -> {
        val token = runBlocking { userLocalSource.user().first() }?.token
        req.newBuilder()
          .addHeader("Authorization", "Bearer $token")
          .removeHeader(customHeader)
          .build()
      }
    }

    val res = chain.proceed(newReq)
    if (res.code != HTTP_UNAUTHORIZED) {
      return res
    }

    return res
  }

  companion object {
    private const val customHeader = "@"
  }
}