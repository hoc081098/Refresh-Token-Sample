package com.hoc081098.refreshtokensample.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import com.hoc081098.refreshtokensample.data.local.UserLocal
import com.hoc081098.refreshtokensample.data.local.UserLocalSerializer
import com.hoc081098.refreshtokensample.data.local.UserLocalSource
import com.hoc081098.refreshtokensample.data.local.UserLocalSourceImpl
import com.hoc081098.refreshtokensample.data.remote.ApiService
import com.hoc081098.refreshtokensample.domain.AuthRepo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class BaseUrl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
  @Binds
  @Singleton
  abstract fun authRepo(impl: AuthRepoImpl): AuthRepo

  @Binds
  @Singleton
  abstract fun userLocalSource(impl: UserLocalSourceImpl): UserLocalSource

  @Binds
  @Singleton
  abstract fun userLocalSerializer(impl: UserLocalSerializer): Serializer<UserLocal>

  internal companion object {
    @Provides
    @Singleton
    fun dataStore(
      @ApplicationContext applicationContext: Context,
      serializer: Serializer<UserLocal>
    ): DataStore<UserLocal> = DataStoreFactory.create(
      serializer = serializer,
      produceFile = { applicationContext.dataStoreFile("user_local") },
    )

    @Provides
    @Singleton
    fun apiService(retrofit: Retrofit): ApiService = ApiService(retrofit)

    @Provides
    @Singleton
    fun retrofit(client: OkHttpClient, @BaseUrl baseUrl: String, moshi: Moshi): Retrofit =
      Retrofit.Builder()
        .client(client)
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun moshi(): Moshi = Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()

    @Provides
    @Singleton
    fun client(): OkHttpClient = OkHttpClient.Builder()
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(15, TimeUnit.SECONDS)
      .writeTimeout(15, TimeUnit.SECONDS)
      .build()

    @Provides
    @BaseUrl
    fun baseUrl(): String = "https://mvi-coroutines-flow-server.herokuapp.com/"
  }
}