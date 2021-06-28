package com.hoc081098.refreshtokensample.data.local

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class UserLocalSourceImpl @Inject constructor(private val dataStore: DataStore<UserLocal>) :
  UserLocalSource {
  override fun user(): Flow<UserLocal?> = dataStore.data
    .map {
      if (it == UserLocal.getDefaultInstance() || !it.isValid()) null
      else it
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

private fun UserLocal.isValid(): Boolean =
  id != null || username != null || token != null || refreshToken != null
