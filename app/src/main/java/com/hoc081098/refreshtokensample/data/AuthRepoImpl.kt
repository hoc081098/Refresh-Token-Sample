package com.hoc081098.refreshtokensample.data

import com.hoc081098.refreshtokensample.AppDispatchers
import com.hoc081098.refreshtokensample.data.local.UserLocalSource
import com.hoc081098.refreshtokensample.data.remote.ApiService
import com.hoc081098.refreshtokensample.domain.AuthRepo
import com.hoc081098.refreshtokensample.domain.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepoImpl @Inject constructor(
  private val userLocalSource: UserLocalSource,
  private val appDispatchers: AppDispatchers,
  private val apiService: ApiService,
) : AuthRepo {
  override fun user(): Flow<User?> {
    TODO("Not yet implemented")
  }

  override suspend fun login() = withContext(appDispatchers.io) {
  }

  override suspend fun logout() {
    TODO("Not yet implemented")
  }

  override suspend fun sample() {
    TODO("Not yet implemented")
  }
}