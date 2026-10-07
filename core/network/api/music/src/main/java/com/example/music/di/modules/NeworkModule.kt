package com.example.music.di.modules

import androidx.core.os.BuildCompat
import dagger.Module
import dagger.Provides
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton




@Module
object NeworkModule {

    private const val BASE_URL = "https://api.audius.co/v1"


    @Provides
    @Singleton
    fun providerLoginInterseptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
           level = HttpLoggingInterceptor.Level.BODY
        }
    }



    @Provides
    @Singleton
    fun providerOkhttp(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient{
        val TIMEOUT_SECONDS = 0
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .build()
    }


    @Provides
    @Singleton
    fun providerRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}