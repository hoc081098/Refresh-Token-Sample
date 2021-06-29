package com.hoc081098.refreshtokensample.data.remote.interceptor

import com.hoc081098.refreshtokensample.data.remote.response.LoginResponse
import com.hoc081098.refreshtokensample.data.remote.response.RefreshTokenResponse
import com.squareup.moshi.JsonAdapter
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.HttpURLConnection
import java.util.UUID
import javax.inject.Inject

class FakeApiInterceptor @Inject constructor(
  private val loginResponseAdapter: JsonAdapter<LoginResponse>,
  private val refreshTokenResponseAdapter: JsonAdapter<RefreshTokenResponse>,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val req = chain.request()

    return when (val path = req.url.encodedPath) {
      "/login" -> {
        val jsonString = loginResponseAdapter.toJson(
          LoginResponse(
            token = UUID.randomUUID().toString(),
            refreshToken = "refresh_token",
            id = "1",
            username = "hoc081098"
          ),
        )

        chain.proceed(req)
          .newBuilder()
          .code(HttpURLConnection.HTTP_OK)
          .protocol(Protocol.HTTP_2)
          .message("")
          .body(jsonString.toResponseBody("application/json".toMediaType()))
          .build()
      }
      "/check-auth" -> {
        Thread.sleep(300)

        chain.proceed(req)
          .newBuilder()
          .code(HttpURLConnection.HTTP_UNAUTHORIZED)
          .protocol(Protocol.HTTP_2)
          .message("")
          .body("{}".toResponseBody("application/json".toMediaType()))
          .build()
      }
      "/refresh-token" -> {
        Thread.sleep(300)

        chain.proceed(req)
          .newBuilder()
          .code(HttpURLConnection.HTTP_OK)
          .protocol(Protocol.HTTP_2)
          .message("")
          .body(
            refreshTokenResponseAdapter
              .toJson(RefreshTokenResponse(token = UUID.randomUUID().toString()))
              .toResponseBody("application/json".toMediaType())
          )
          .build()
      }
      else -> error("Unknown path: $path")
    }
  }
}
