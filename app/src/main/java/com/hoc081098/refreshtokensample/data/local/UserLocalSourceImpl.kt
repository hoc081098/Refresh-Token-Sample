package com.hoc081098.refreshtokensample.data.local

import androidx.datastore.core.DataStore
import com.hoc081098.refreshtokensample.AppDispatchers
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import timber.log.Timber

class UserLocalSourceImpl @Inject constructor(
  private val dataStore: DataStore<UserLocal>,
  private val appDispatchers: AppDispatchers,
) : UserLocalSource {
  override fun user() = dataStore.data
    .onEach { Timber.d("userLocal=$it") }
    .map { v -> v.takeIf { it != USER_LOCAL_NULL } }
    .catch { cause: Throwable ->
      if (cause is IOException) {
        emit(null)
      } else {
        throw cause
      }
    }
    .flowOn(appDispatchers.io)

  override suspend fun update(transform: suspend (current: UserLocal?) -> UserLocal?) =
    withContext(appDispatchers.io) {
      dataStore.updateData { current ->
        transform(current.takeIf { it != USER_LOCAL_NULL }) ?: USER_LOCAL_NULL
      }
    }
}
