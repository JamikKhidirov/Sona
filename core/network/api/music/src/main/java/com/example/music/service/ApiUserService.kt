package com.example.music.service

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiUserService {

    //Получает список пользователей по идентификатору
    @GET("users")
    suspend fun <T> getUsersListById(
        @Query("user_id") userId: String, //Идентификатор пользователя, отправляющего запрос
        @Query("id") ids: List<String>, // Идентификатор пользователя (пользователей)
    ): Response<T>



    /*
    Получает идентификаторы пользователей по любому адресу
    кошелька Ethereum или адресу аккаунта Solana, связанному с их аккаунтом Audius.*/
    @GET("users/address")
    suspend fun <T> getIdUsersPayCahes(
        @Query("address") adress: List<String> //Адрес кошелька
    ): Response<T>


    //Получает одного пользователя по его хешу
    @GET("users/handle/{handle}")
    suspend fun <T> getUsertoHash(
        @Path("handle") handle: String, //Хеш пользователя
        @Query("user_id") user_id: String //Идентификатор пользователя, отправляющего запрос
    ): Response<T>






}