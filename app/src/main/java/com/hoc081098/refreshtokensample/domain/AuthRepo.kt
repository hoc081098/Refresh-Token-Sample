package com.hoc081098.refreshtokensample.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepo {
  fun user(): Flow<User?>

  suspend fun login()

  suspend fun logout()
}
