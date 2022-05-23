package com.hoc081098.refreshtokensample.data.remote.interceptor

import com.hoc081098.refreshtokensample.data.local.UserLocalSource
import com.hoc081098.refreshtokensample.data.remote.ApiService
import com.hoc081098.refreshtokensample.data.remote.ApiService.Factory.CUSTOM_HEADER
import com.hoc081098.refreshtokensample.data.remote.ApiService.Factory.NO_AUTH
import com.hoc081098.refreshtokensample.data.remote.body.RefreshTokenBody
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject
import javax.inject.Provider
import timber.log.Timber.Forest.d as debug

class AuthInterceptor @Inject constructor(
  private val userLocalSource: UserLocalSource,
  private val apiService: Provider<ApiService>,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().also { debug("[1] START url=${it.url}") }

    if (NO_AUTH in request.headers.values(CUSTOM_HEADER)) {
      return chain.proceedWithToken(request, null)
    }

    val token = runBlocking { userLocalSource.user().first() }
      ?.token
      .also { debug("[2] READ TOKEN token=$it, url=${request.url}") }
    val response = chain.proceedWithToken(request, token)

    if (response.code != HTTP_UNAUTHORIZED || token == null) {
      return response
    }

    val newToken = executeTokenRefreshing(request, token)

    return if (newToken !== null && newToken != token) {
      chain.proceedWithToken(request, newToken)
    } else {
      response
    }
  }

  private fun executeTokenRefreshing(request: Request, token: String?): String? {
    val requestUrl = request.url
    debug("[3] BEFORE REFRESHING token=$token, url=$requestUrl")

    return runBlocking {
      userLocalSource.update { currentUserLocal ->
        val maybeUpdatedToken = currentUserLocal?.token
        debug("[4] INSIDE REFRESHING maybeUpdatedToken=$maybeUpdatedToken, url=$requestUrl")

        when {
          maybeUpdatedToken == null ->
            null
              .also { debug("[5-1] LOGGED OUT url=$requestUrl") } // already logged out!
          maybeUpdatedToken != token ->
            currentUserLocal
              .also { debug("[5-2] REFRESHED BY ANOTHER url=$requestUrl") } // refreshed by another request
          else -> {
            debug("[5-3] START REFRESHING REQUEST url=$requestUrl")

            val refreshTokenRes = apiService.get()
              .refreshToken(
                RefreshTokenBody(
                  refreshToken = currentUserLocal.refreshToken,
                  username = currentUserLocal.username
                )
              )
              .also { debug("[6] DONE REFRESHING REQUEST status=${it.code()}, url=$requestUrl") }

            when (val code = refreshTokenRes.code()) {
              HTTP_OK -> {
                currentUserLocal
                  .toBuilder()
                  .setToken(refreshTokenRes.body()!!.token)
                  .build()
                  .also { debug("[7-1] REFRESH SUCCESSFULLY newToken=${it.token}, url=$requestUrl") }
              }
              HTTP_UNAUTHORIZED -> {
                debug("[7-2] REFRESH FAILED HTTP_UNAUTHORIZED url=$requestUrl")
                null
              }
              else -> {
                debug("[7-3] REFRESH FAILED code=$code, url=$requestUrl")
                currentUserLocal
              }
            }
          }
        }
      }?.token
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
