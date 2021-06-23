package com.hoc081098.refreshtokensample.data

import com.hoc081098.refreshtokensample.domain.AuthRepo
import com.hoc081098.refreshtokensample.domain.User
import kotlinx.coroutines.flow.Flow

class AuthRepoImpl : AuthRepo {
  override fun user(): Flow<User?> {
    TODO("Not yet implemented")
  }

  override suspend fun login() {
    TODO("Not yet implemented")
  }

  override suspend fun logout() {
    TODO("Not yet implemented")
  }

  override suspend fun sample() {
    TODO("Not yet implemented")
  }
}