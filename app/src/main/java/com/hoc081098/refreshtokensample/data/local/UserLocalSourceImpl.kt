package com.hoc081098.refreshtokensample.data.local

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class UserLocalSourceImpl @Inject constructor(private val dataStore: DataStore<UserLocal>) :
  UserLocalSource {
  override fun user(): Flow<UserLocal?> = dataStore.data
    .onEach { Timber.d("User=$it") }
    .map { if (it != USER_LOCAL_NULL) it else null }
    .catch { cause: Throwable ->
      if (cause is IOException) {
        emit(null)
      } else {
        throw cause
      }
    }

  override suspend fun save(userLocal: UserLocal?) {
    dataStore.updateData {
      if (userLocal === null) USER_LOCAL_NULL
      else userLocal
    }
  }

  companion object {
    private val USER_LOCAL_NULL: UserLocal = UserLocal.getDefaultInstance()
  }
}
