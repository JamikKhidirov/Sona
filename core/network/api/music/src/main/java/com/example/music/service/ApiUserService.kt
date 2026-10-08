package com.example.music.service

import com.example.music.data.user.FilterTracks
import com.example.music.data.user.FilterTracs
import com.example.music.data.user.SortAi
import com.example.music.data.user.SortDirection
import com.example.music.data.user.SortMethod
import com.example.music.data.user.SortMethodUser
import com.google.gson.annotations.SerializedName
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


    /*
    Получает треки, созданные искусственным интеллектом
    и привязанные к пользователю, по его хешу
     */
    @GET("users/handle/{handle}/tracks/ai_attributed")
    suspend fun <T> getAiGeneratesTracs(
        //Хеш пользователя (обязательное поле)
        @Path("handle") handle: String,
        //Количество элементов, которые нужно пропустить.
        // Полезно для разбивки на страницы (номер страницы * лимит)
        @Query("offset") offer: Int? = null,

        //Идентификатор пользователя, отправляющего запрос
        @Query("_id") id: String? = null,

        //[Устаревшее] Поле для сортировки
        @Query("sort") sort: SortAi? = null,

        //Запрос фильтра
        @Query("query") query: String? = null,

        //Метод сортировки
        @Query("sort_method") sort_method: SortMethod? = null,

        //Направление сортировки
        @Query("sort_direction") sort_direction: SortDirection? = null,

        //Фильтр по общедоступным дорожкам
        @Query("filter_tracks") filter_tracks: FilterTracs? = null

    ): Response<T>


    @GET("users/search")
    suspend fun <T> searchUserQuery(
        /*
        Количество элементов, которые нужно пропустить.
        Полезно для разбивки на страницы (номер страницы * лимит)
         */
        @Query("offset") offset: Int? = null,
        //Количество элементов для выборки
        @Query("limit") limit: Int? = null,
        //Поисковый запрос
        @Query("query") query: String? = null,

        //Жанры для фильтрации
        @Query("genre") genre: List<String>,

        //Метод сортировки
        @Query("sort_method") sort_method: SortMethodUser? = null,

        //Показывать в результатах поиска только верифицированных пользователей
        @Query("is_verified") is_verified: String? = null
    ): Response<T>



    //Проверьте, был ли данный токен jwt ID подписан субъектом (пользователем) в полезной нагрузке
    @GET("users/verify_token")
    suspend fun <T> getJWTSubscribeObj(): Response<T>





}