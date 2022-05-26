package com.hoc081098.refreshtokensample.data.remote.interceptor

import com.hoc081098.refreshtokensample.AppDispatchers
import com.hoc081098.refreshtokensample.data.local.UserLocal
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
import timber.log.Timber
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject
import javax.inject.Provider

class AuthInterceptor @Inject constructor(
  private val userLocalSource: UserLocalSource,
  private val apiService: Provider<ApiService>,
  private val appDispatchers: AppDispatchers,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request().also { debug("[1] START url=${it.url}") }

    if (NO_AUTH in request.headers.values(CUSTOM_HEADER)) {
      return chain.proceedWithToken(request, null)
    }

    val token = runBlocking(appDispatchers.io) { userLocalSource.user().first() }
      ?.token
      .also { debug("[2] READ TOKEN token=${it.firstTwoCharactersAndLastTwoCharacters()}, url=${request.url}") }
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
    debug("[3] BEFORE REFRESHING token=${token.firstTwoCharactersAndLastTwoCharacters()}, url=$requestUrl")

    return runBlocking(appDispatchers.io) {
      userLocalSource.update { currentUserLocal ->
        val maybeUpdatedToken = currentUserLocal?.token
        debug("[4] INSIDE REFRESHING maybeUpdatedToken=${maybeUpdatedToken.firstTwoCharactersAndLastTwoCharacters()}, url=$requestUrl")

        when {
          maybeUpdatedToken == null ->
            null
              .also { debug("[5-1] LOGGED OUT url=$requestUrl") } // already logged out!
          maybeUpdatedToken != token ->
            currentUserLocal
              .also { debug("[5-2] REFRESHED BY ANOTHER url=$requestUrl") } // refreshed by another request
          else -> {
            debug("[5-3] START REFRESHING REQUEST url=$requestUrl")

            val refreshTokenRes = apiService
              .get()
              .refreshToken(currentUserLocal.toRefreshTokenBody())

            when (val code = refreshTokenRes.code()) {
              HTTP_OK -> {
                currentUserLocal
                  .toBuilder()
                  .setToken(refreshTokenRes.body()!!.token)
                  .build()
                  .also { debug("[6-1] REFRESH SUCCESSFULLY newToken=${it.token.firstTwoCharactersAndLastTwoCharacters()}, url=$requestUrl") }
              }
              HTTP_UNAUTHORIZED -> null.also {
                debug("[6-2] REFRESH FAILED HTTP_UNAUTHORIZED url=$requestUrl")
              }
              else -> currentUserLocal.also {
                debug("[6-3] REFRESH FAILED code=$code, url=$requestUrl")
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

  @Suppress("NOTHING_TO_INLINE")
  private inline fun debug(s: String) = Timber.tag(LOG_TAG).d(s)

  private companion object {
    private val LOG_TAG = AuthInterceptor::class.java.simpleName
  }
}

private fun UserLocal.toRefreshTokenBody() = RefreshTokenBody(refreshToken, username)

@Suppress("NOTHING_TO_INLINE")
private inline fun String?.firstTwoCharactersAndLastTwoCharacters(): String? {
  this ?: return null
  return if (length <= 4) this else "${take(2)}...${takeLast(2)}"
}
