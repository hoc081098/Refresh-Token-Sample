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
    val req = chain.request().also { Timber.d("[1] $it") }

    if (NO_AUTH in req.headers.values(CUSTOM_HEADER)) {
      return chain.proceedWithToken(req, null)
    }

    val token =
      runBlocking { userLocalSource.user().first() }?.token.also { Timber.d("[2] $req $it") }
    val res = chain.proceedWithToken(req, token)

    if (res.code != HTTP_UNAUTHORIZED || token == null) {
      return res
    }

    Timber.d("[3] $req")

    val newToken: String? = runBlocking {
      mutex.withLock {
        val user =
          userLocalSource.user().first().also { Timber.d("[4] $req $it") }
        val maybeUpdatedToken = user?.token

        when {
          user == null || maybeUpdatedToken == null -> null.also { Timber.d("[5-1] $req") } // already logged out!
          maybeUpdatedToken != token -> maybeUpdatedToken.also { Timber.d("[5-2] $req") } // refreshed by another request
          else -> {
            Timber.d("[5-3] $req")

            val refreshTokenRes =
              apiService.get().refreshToken(RefreshTokenBody(user.refreshToken, user.username))
                .also {
                  Timber.d("[6] $req $it")
                }

            val code = refreshTokenRes.code()
            if (code == HTTP_OK) {
              refreshTokenRes.body()?.token?.also {
                Timber.d("[7-1] $req")
                userLocalSource.save(
                  user.toBuilder()
                    .setToken(it)
                    .build()
                )
              }
            } else if (code == HTTP_UNAUTHORIZED) {
              Timber.d("[7-2] $req")
              userLocalSource.save(null)
              null
            } else {
              Timber.d("[7-3] $req")
              null
            }
          }
        }
      }
    }

    return if (newToken !== null) chain.proceedWithToken(req, newToken) else res
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
