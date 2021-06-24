package com.hoc081098.refreshtokensample.data.local

import kotlinx.coroutines.flow.Flow

interface UserLocalSource {
  fun user(): Flow<UserLocal?>

  suspend fun save(userLocal: UserLocal)
}