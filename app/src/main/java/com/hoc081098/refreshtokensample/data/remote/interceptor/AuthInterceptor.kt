package com.hoc081098.refreshtokensample.data.remote.interceptor

import com.hoc081098.refreshtokensample.data.local.UserLocalSource
import com.hoc081098.refreshtokensample.data.remote.ApiService
import com.hoc081098.refreshtokensample.data.remote.ApiService.Factory.CUSTOM_HEADER
import com.hoc081098.refreshtokensample.data.remote.ApiService.Factory.NO_AUTH
import com.hoc081098.refreshtokensample.data.remote.body.RefreshTokenBody
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject
import javax.inject.Provider

class AuthInterceptor @Inject constructor(
  private val userLocalSource: UserLocalSource,
  private val apiService: Provider<ApiService>,
) : Interceptor {
  private val mutex = Mutex()

  override fun intercept(chain: Interceptor.Chain): Response {
    val req = chain.request()

    if (NO_AUTH in req.headers.values(CUSTOM_HEADER)) {
      return chain.proceedWithToken(req, null)
    }

    val token = runBlocking { userLocalSource.user().first() }?.token.also { Timber.d("Token=$it") }
    val res = chain.proceedWithToken(req, token)

    if (res.code != HTTP_UNAUTHORIZED) {
      return res
    }

    return runBlocking {
      Timber.d("Start $req")

      mutex.withLock {
        val user = userLocalSource.user().first()
        when {
          user == null -> res
          user.token != token -> chain.proceedWithToken(req, user.token)
          else -> {
            val refreshTokenRes =
              apiService.get().refreshToken(RefreshTokenBody(user.refreshToken))
            val updatedToken = refreshTokenRes.body()?.token

            if (refreshTokenRes.code() == HTTP_OK && updatedToken != null) {
              userLocalSource.save(
                user.toBuilder()
                  .setToken(updatedToken)
                  .build()
              )
              chain.proceedWithToken(req, updatedToken)
            } else {
              res
            }
          }
        }
      }.also { Timber.d("Done $req") }
    }
  }

  private fun Interceptor.Chain.proceedWithToken(req: Request, token: String?): Response =
    req.newBuilder()
      .apply {
        if (token !== null) {
          addHeader("Authorization", "Bearer $token")
        }
      }
      .removeHeader(CUSTOM_HEADER)
      .build()
      .let(::proceed)
}
