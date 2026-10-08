package com.example.music.service

import com.example.music.data.FilterTracks
import com.example.music.data.SortDirection
import com.example.music.data.SortMethod
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
        @Query("user_id") user_id: String? = null, //Идентификатор пользователя, отправляющего запрос
    ): Response<T>



    //Возвращает дорожки, созданные пользователем с использованием пользовательского дескриптора
    @GET("users/handle/{handle}/tracks")
    suspend fun <T> getTracksByUserHandle(
        // Path — обязательный
        //Дескриптор пользователя
        @Path("handle") handle: String,

        // Query — опциональные (пагинация)
        /*
        Количество элементов, которые нужно пропустить.
        Полезно для разбивки на страницы (номер страницы * лимит)
         */
        @Query("offset") offset: Int? = null,
        //Количество элементов для выборки
        @Query("limit") limit: Int? = null,

        // Идентификатор пользователя, отправляющего запрос
        @Query("user_id") userId: String? = null,

        // Поле для сортировки
        @Query("sort_method") sortMethod: SortMethod? = null,

        //Направление сортировки
        @Query("sort_direction") sortDirection: SortDirection? = null,

        //Фильтровать по общедоступным дорожкам
        @Query("filter_tracks") filterTracks: FilterTracks? = null,

        // Запрос фильтра - (поиск по названию)
        @Query("query") query: String? = null

    ): Response<T>






}