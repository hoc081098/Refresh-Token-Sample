package com.hoc081098.refreshtokensample.di

import com.hoc081098.refreshtokensample.AppDispatchers
import com.hoc081098.refreshtokensample.AppDispatchersImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
  @Binds
  @Singleton
  abstract fun appDispatchers(impl: AppDispatchersImpl): AppDispatchers
}