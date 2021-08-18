package com.hoc081098.refreshtokensample.data.local

import com.google.crypto.tink.Aead
import javax.inject.Inject

interface Crypto {
  fun encrypt(text: ByteArray): ByteArray

  fun decrypt(encryptedData: ByteArray): ByteArray
}

class CryptoImpl @Inject constructor(
  private val aead: Aead
) : Crypto {
  override fun encrypt(text: ByteArray): ByteArray {
    return aead.encrypt(text, null)
  }

  override fun decrypt(encryptedData: ByteArray): ByteArray {
    return if (encryptedData.isNotEmpty()) {
      aead.decrypt(encryptedData, null)
    } else {
      encryptedData
    }
  }
}
