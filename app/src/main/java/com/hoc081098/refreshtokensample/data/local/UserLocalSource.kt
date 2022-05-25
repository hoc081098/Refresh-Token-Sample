package com.hoc081098.refreshtokensample.data.local

import kotlinx.coroutines.flow.Flow
import java.io.IOException

interface UserLocalSource {
  fun user(): Flow<UserLocal?>

  /**
   * Updates the data transactionally in an atomic read-modify-write operation. All operations
   * are serialized, and the [transform] itself is a coroutine so it can perform heavy work
   * such as RPCs.
   *
   * The coroutine completes when the data has been persisted durably to disk (after which
   * [user] will reflect the update). If the transform or write to disk fails, the
   * transaction is aborted and an exception is thrown.
   *
   * @return the snapshot returned by the transform
   * @throws IOException when an exception is encountered when writing data to disk
   * @throws Exception when thrown by the transform function
   */
  suspend fun update(transform: suspend (current: UserLocal?) -> UserLocal?): UserLocal?
}

@JvmField
internal val USER_LOCAL_NULL: UserLocal = UserLocal.getDefaultInstance()
