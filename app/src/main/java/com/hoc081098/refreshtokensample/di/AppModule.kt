package com.hoc081098.refreshtokensample.di

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.hoc081098.refreshtokensample.AppDispatchers
import com.hoc081098.refreshtokensample.AppDispatchersImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class AppCoroutineScope

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
  @Binds
  @Singleton
  abstract fun appDispatchers(impl: AppDispatchersImpl): AppDispatchers

  internal companion object {
    private const val KEYSET_NAME = "keyset"
    private const val PREF_FILE_NAME = "keyset_prefs"
    private const val MASTER_KEY_URI = "android-keystore://master_key"

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
