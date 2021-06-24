package com.hoc081098.refreshtokensample.data.local

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserLocalSourceImpl(private val dataStore: DataStore<UserLocal>) : UserLocalSource {
  override fun user(): Flow<UserLocal?> = dataStore.data
    .map {
      @Suppress("USELESS_CAST")
      it as UserLocal?
    }
    .catch { cause: Throwable ->
      if (cause is IOException) {
        emit(null)
      } else {
        throw cause
      }
    }

  override suspend fun save(userLocal: UserLocal) {
    dataStore.updateData { userLocal }
  }
}