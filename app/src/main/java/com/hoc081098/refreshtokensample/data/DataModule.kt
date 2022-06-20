package com.hoc081098.refreshtokensample.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import com.hoc081098.refreshtokensample.BuildConfig
import com.hoc081098.refreshtokensample.data.local.Crypto
import com.hoc081098.refreshtokensample.data.local.CryptoImpl
import com.hoc081098.refreshtokensample.data.local.UserLocal
import com.hoc081098.refreshtokensample.data.local.UserLocalSerializer
import com.hoc081098.refreshtokensample.data.local.UserLocalSource
import com.hoc081098.refreshtokensample.data.local.UserLocalSourceImpl
import com.hoc081098.refreshtokensample.data.remote.ApiService
import com.hoc081098.refreshtokensample.data.remote.interceptor.AuthInterceptor
import com.hoc081098.refreshtokensample.data.remote.response.LoginResponse
import com.hoc081098.refreshtokensample.data.remote.response.RefreshTokenResponse
import com.hoc081098.refreshtokensample.domain.AuthRepo
import com.hoc081098.refreshtokensample.domain.DemoRepo
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Retention(AnnotationRetention.BINARY)
@Qualifier
@MustBeDocumented
annotation class BaseUrl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
  @Binds
  @Singleton
  abstract fun authRepo(impl: AuthRepoImpl): AuthRepo

  @Binds
  abstract fun demoRepo(impl: DemoRepoImpl): DemoRepo

  @Binds
  @Singleton
  abstract fun userLocalSource(impl: UserLocalSourceImpl): UserLocalSource

  @Binds
  @Singleton
  abstract fun userLocalSerializer(impl: UserLocalSerializer): Serializer<UserLocal>

  @Binds
  abstract fun crypto(impl: CryptoImpl): Crypto

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
    fun client(
      authInterceptor: AuthInterceptor,
      httpLoggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
      OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(httpLoggingInterceptor)
        .build()

    @Provides
    @BaseUrl
    fun baseUrl(): String = "http://10.0.2.2:3000/"

    @Provides
    @Singleton
    fun httpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
      level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
      } else {
        HttpLoggingInterceptor.Level.NONE
      }
    }

    @ExperimentalStdlibApi
    @Provides
    fun loginResponseAdapter(moshi: Moshi): JsonAdapter<LoginResponse> = moshi.adapter()

    @ExperimentalStdlibApi
    @Provides
    fun refreshTokenResponseAdapter(moshi: Moshi): JsonAdapter<RefreshTokenResponse> =
      moshi.adapter()
  }
}
