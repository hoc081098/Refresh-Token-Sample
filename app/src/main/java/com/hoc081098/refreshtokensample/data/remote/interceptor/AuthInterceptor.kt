package com.hoc081098.refreshtokensample.data.remote.interceptor

import com.hoc081098.refreshtokensample.AppDispatchers
import com.hoc081098.refreshtokensample.data.local.UserLocal
import com.hoc081098.refreshtokensample.data.local.UserLocalSource
import com.hoc081098.refreshtokensample.data.remote.ApiService
import com.hoc081098.refreshtokensample.data.remote.ApiService.Factory.CUSTOM_HEADER
import com.hoc081098.refreshtokensample.data.remote.ApiService.Factory.NO_AUTH
import com.hoc081098.refreshtokensample.data.remote.body.RefreshTokenBody
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

class AuthInterceptor @Inject constructor(
  private val userLocalSource: UserLocalSource,
  private val apiService: Provider<ApiService>,
  private val appDispatchers: AppDispatchers,
) : Interceptor {
  private val mutex = Mutex()

  override fun intercept(chain: Interceptor.Chain): Response {
    val req = chain.request().also { debug("[1] $it") }

    if (NO_AUTH in req.headers.values(CUSTOM_HEADER)) {
      return chain.proceedWithToken(req, null)
    }

    val token = runBlocking(appDispatchers.io) { userLocalSource.user().first() }
      ?.token
      .also { debug("[2] $req $it") }
    val res = chain.proceedWithToken(req, token)

    if (res.code != HTTP_UNAUTHORIZED || token == null) {
      return res
    }

    debug("[3] $req")

    val newToken: String? = runBlocking(appDispatchers.io) {
      mutex.withLock {
        val user =
          userLocalSource.user().first().also { debug("[4] $req $it") }
        val maybeUpdatedToken = user?.token

        when {
          user == null || maybeUpdatedToken == null -> null.also { debug("[5-1] $req") } // already logged out!
          maybeUpdatedToken != token -> maybeUpdatedToken.also { debug("[5-2] $req") } // refreshed by another request
          else -> {
            debug("[5-3] $req")

            val refreshTokenRes = apiService.get()
              .refreshToken(user.toRefreshTokenBody())
              .also { debug("[6] $req $it") }

            when (refreshTokenRes.code()) {
              HTTP_OK -> {
                debug("[7-1] $req")
                refreshTokenRes.body()!!.token.also { updatedToken ->
                  userLocalSource.update {
                    (it ?: return@update null)
                      .toBuilder()
                      .setToken(updatedToken)
                      .build()
                  }
                }
              }
              HTTP_UNAUTHORIZED -> {
                debug("[7-2] $req")
                userLocalSource.update { null }
                null
              }
              else -> {
                debug("[7-3] $req")
                null
              }
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

  @Suppress("NOTHING_TO_INLINE")
  private inline fun debug(s: String) = Timber.tag(LOG_TAG).d(s)

  private companion object {
    private val LOG_TAG = AuthInterceptor::class.java.simpleName
  }
}

private fun UserLocal.toRefreshTokenBody() = RefreshTokenBody(refreshToken, username)
