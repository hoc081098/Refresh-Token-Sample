package com.hoc081098.refreshtokensample.data

import com.hoc081098.refreshtokensample.AppDispatchers
import com.hoc081098.refreshtokensample.data.local.UserLocal
import com.hoc081098.refreshtokensample.data.local.UserLocalSource
import com.hoc081098.refreshtokensample.data.remote.ApiService
import com.hoc081098.refreshtokensample.data.remote.body.LoginBody
import com.hoc081098.refreshtokensample.data.remote.response.LoginResponse
import com.hoc081098.refreshtokensample.di.AppCoroutineScope
import com.hoc081098.refreshtokensample.domain.AuthRepo
import com.hoc081098.refreshtokensample.domain.User
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthRepoImpl @Inject constructor(
  private val userLocalSource: UserLocalSource,
  private val appDispatchers: AppDispatchers,
  private val apiService: ApiService,
  @AppCoroutineScope private val appScope: CoroutineScope,
) : AuthRepo {
  init {
    appScope.launch {
      runCatching { apiService.checkAuth() }
    }
  }

  private val userFlow by lazy {
    userLocalSource
      .user()
      .map { it?.toUser() }
      .distinctUntilChanged()
  }

  override fun user() = userFlow

  override suspend fun login() = withContext(appDispatchers.io) {
    val response = apiService.login(
      LoginBody(
        username = "hoc081098",
        password = "123456",
      ),
    )

    userLocalSource.update { response.toUserLocal() }

    Unit
  }

  override suspend fun logout() = withContext(appDispatchers.io) {
    userLocalSource.update { null }

    Unit
  }
}

private fun LoginResponse.toUserLocal(): UserLocal = UserLocal.newBuilder()
  .setId(id)
  .setUsername(username)
  .setToken(token)
  .setRefreshToken(refreshToken)
  .build()

private fun UserLocal.toUser(): User = User(
  id = id,
  username = username,
)
