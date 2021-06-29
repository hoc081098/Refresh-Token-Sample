@file:Suppress("BlockingMethodInNonBlockingContext")

package com.hoc081098.refreshtokensample.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class UserLocalSerializer @Inject constructor() : Serializer<UserLocal> {
  override val defaultValue: UserLocal
    get() = UserLocal.getDefaultInstance()

  override suspend fun readFrom(input: InputStream): UserLocal {
    return try {
      UserLocal.parseFrom(input)
    } catch (e: InvalidProtocolBufferException) {
      throw CorruptionException("Cannot read proto.", e)
    }
  }

  override suspend fun writeTo(t: UserLocal, output: OutputStream) = t.writeTo(output)
}
