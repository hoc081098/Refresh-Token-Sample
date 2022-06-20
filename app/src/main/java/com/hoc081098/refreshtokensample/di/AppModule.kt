package com.hoc081098.refreshtokensample.di

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.integration.android.AndroidKeystoreKmsClient
import com.hoc081098.refreshtokensample.AppDispatchers
import com.hoc081098.refreshtokensample.AppDispatchersImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@Retention(AnnotationRetention.BINARY)
@Qualifier
@MustBeDocumented
annotation class AppCoroutineScope

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
  @Binds
  @Singleton
  abstract fun appDispatchers(impl: AppDispatchersImpl): AppDispatchers

  internal companion object {
    private const val KEYSET_NAME = "__refresh_token_sample_encrypted_prefs_keyset__"
    private const val PREF_FILE_NAME = "refresh_token_sample_secret_prefs"
    private const val MASTER_KEY_URI =
      "${AndroidKeystoreKmsClient.PREFIX}refresh_token_sample_master_key"

    @Provides
    @Singleton
    @AppCoroutineScope
    fun appScope(appDispatchers: AppDispatchers) =
      CoroutineScope(appDispatchers.io + SupervisorJob())

    @Provides
    @Singleton
    fun aead(@ApplicationContext context: Context): Aead {
      AeadConfig.register()

      return AndroidKeysetManager
        .Builder()
        .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
        .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
        .withMasterKeyUri(MASTER_KEY_URI)
        .build()
        .keysetHandle
        .getPrimitive(Aead::class.java)
    }
  }
}
