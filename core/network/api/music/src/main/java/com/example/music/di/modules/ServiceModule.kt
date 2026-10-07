package com.example.music.di.modules

import com.example.music.service.ApiPlaylistService
import com.example.music.service.ApiSearchService
import com.example.music.service.ApiTracsService
import com.example.music.service.ApiUserService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton


//Документация
//https://docs.audius.co/api#tag/cid_data

//Также постман
//https://www.postman.com/samgutentag/audius-devs/http-request/g3jyfrw/get-tips


@Module
object ServiceModule {


    @Provides
    @Singleton
    fun providerUserService(
        retrofit: Retrofit
    ): ApiUserService {
        return retrofit.create(ApiUserService::class.java)
    }


    @Provides
    @Singleton
    fun providerTracsService(
        retrofit: Retrofit
    ): ApiTracsService{

        return retrofit.create(ApiTracsService::class.java)
    }


    @Singleton
    @Provides
    fun progiverPlaylistService(
        retrofit: Retrofit
    ): ApiPlaylistService{
        return retrofit.create(ApiPlaylistService::class.java)
    }


    @Provides
    @Singleton
    fun providerSearchService(
        retrofit: Retrofit
    ): ApiSearchService{

        return retrofit.create(ApiSearchService::class.java)
    }

}