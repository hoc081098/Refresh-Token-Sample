package com.hoc081098.refreshtokensample.di

import com.hoc081098.refreshtokensample.AppDispatchers
import com.hoc081098.refreshtokensample.AppDispatchersImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    @Provides
    @Singleton
    @AppCoroutineScope
    fun appScope(appDispatchers: AppDispatchers) =
      CoroutineScope(appDispatchers.io + SupervisorJob())
  }
}
