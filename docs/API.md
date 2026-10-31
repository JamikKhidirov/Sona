# Audius API в Sona

Сетевой слой вынесен в модуль **`:core:network:api:music`** (namespace
`com.sona.core.network.audius`). Вместо одного «божественного» интерфейса API
разделён на четыре доменных контракта:

| Интерфейс | Файл | Ответственность |
|---|---|---|
| `TrackApi` | `api/TrackApi.kt` | треки, стриминг, лайки, репосты, комментарии |
| `PlaylistApi` | `api/PlaylistApi.kt` | плейлисты, альбомы, треки плейлиста |
| `UserApi` | `api/UserApi.kt` | пользователи, артисты, подписки, профиль |
| `SearchApi` | `api/SearchApi.kt` | общий поиск, full search, autocomplete |

Все четыре создаются Retrofit'ом в `di/ApiModule.kt` и доступны через Dagger.
Фича может инжектить только тот интерфейс, который ей реально нужен.

```kotlin
@Inject constructor(
    private val trackApi: TrackApi,
    private val userApi: UserApi,
)

// или напрямую из графа приложения
val trackApi = appComponent.trackApi()
val playlistApi = appComponent.playlistApi()
val userApi = appComponent.userApi()
val searchApi = appComponent.searchApi()
```

---

## Содержание

- [База и конфигурация](#база-и-конфигурация)
- [Авторизация](#авторизация)
- [Формат ответа](#формат-ответа)
- [TrackApi](#trackapi)
- [PlaylistApi](#playlistapi)
- [UserApi](#userapi)
- [SearchApi](#searchapi)
- [DTO и маппинг](#dto-и-маппинг)
- [Репозиторий](#репозиторий)
- [Стриминг](#стриминг)
- [Ошибки и таймауты](#ошибки-и-таймауты)
- [Примеры запросов](#примеры-запросов)
- [Ограничения](#ограничения)

---

## База и конфигурация

```kotlin
object AudiusConfig {
    const val APP_NAME = "Sona"
    const val API_BASE_URL = "https://api.audius.co/v1/"

    fun streamUrl(trackId: String): String =
        "${API_BASE_URL}tracks/$trackId/stream?app_name=$APP_NAME"
}
```

| Параметр | Значение |
|---|---|
| Base URL | `https://api.audius.co/v1/` |
| `app_name` | `Sona` |
| Converter | Gson (`GsonConverterFactory`) |
| HTTP client | OkHttp 5.5.0 |
| Timeouts | connect 30 s, read 45 s, write 30 s |
| Redirects | follow |
| Retry | `retryOnConnectionFailure(true)` |
| Logging | `HttpLoggingInterceptor.Level.BASIC` |

Каждый запрос передаёт `app_name` явно через `@Query("app_name")`. Это
требование Audius: без него часть эндпоинтов отвечает ошибкой.

---

## Авторизация

Публичные запросы (тренды, поиск, треки, профили) работают без токена.

Для приватных действий (лайк, репост, подписка, `users/me`) нужен OAuth bearer
token. Он вводится в **Settings → Audius account** и хранится в DataStore.

```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.token()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
```

Токен попадает во все запросы автоматически. Если он пустой — заголовок не
добавляется, и приложение работает в публичном режиме.

> Сейчас лайки в Sona **локальные** (DataStore). `favoriteTrack` / `unfavoriteTrack`
> готовы к использованию, но UI не отправляет их на сервер без токена.

---

## Формат ответа

Audius заворачивает полезную нагрузку в `data`:

```json
{
  "data": [
    { "id": "D7KyD", "title": "Track name", "user": { "name": "Artist" } }
  ]
}
```

```kotlin
data class AudiusEnvelope<T>(
    @SerializedName("data") val data: T? = null,
)
```

`data` может быть `null` — репозиторий использует `.orEmpty()` / `?: error(...)`.

Некоторые эндпоинты возвращают не фиксированную схему, а произвольный JSON
(`JsonElement`): `tracks/trending/ids`, `users/account`, `users/{id}/reposts`,
`playlists/{id}`, `search`, `search/full`, `search/autocomplete`.

---

## TrackApi

Файл: `core/network/api/music/src/main/java/com/sona/core/network/audius/api/TrackApi.kt`

### Чтение

| Метод | HTTP | Endpoint | Параметры |
|---|---|---|---|
| `getTrendingTracks` | GET | `tracks/trending` | `app_name`, `genre?`, `time=week`, `limit=50`, `offset=0` |
| `getUndergroundTrendingTracks` | GET | `tracks/trending/underground` | `app_name`, `limit`, `offset` |
| `getTrendingTrackIds` | GET | `tracks/trending/ids` | `app_name`, `genre?`, `time`, `limit` |
| `getTracks` | GET | `tracks` | `app_name`, `id` (список) |
| `getTrack` | GET | `tracks/{trackId}` | `app_name` |
| `searchTracks` | GET | `tracks/search` | `query`, `app_name`, `genre?`, `mood?`, `limit=25`, `offset=0` |
| `getRelatedTracks` | GET | `tracks/{trackId}/related` | `app_name`, `limit=25`, `offset=0` |
| `getTrackRemixes` | GET | `tracks/{trackId}/remixes` | `app_name`, `limit=25`, `offset=0` |
| `getTrackStems` | GET | `tracks/{trackId}/stems` | `app_name` |
| `getTrackComments` | GET | `tracks/{trackId}/comments` | `app_name`, `limit=25`, `offset=0` |
| `getTrackAccessInfo` | GET | `tracks/{trackId}/access_info` | `app_name` |

### Стриминг и файлы

| Метод | HTTP | Endpoint | Возврат |
|---|---|---|---|
| `streamTrack` | GET | `tracks/{trackId}/stream` | `ResponseBody`, `@Streaming` |
| `downloadTrack` | GET | `tracks/{trackId}/download` | `ResponseBody`, `@Streaming` |

### Действия

| Метод | HTTP | Endpoint | Возврат |
|---|---|---|---|
| `postTrackComment` | POST | `tracks/{trackId}/comments` | `CommentResponse` |
| `favoriteTrack` | POST | `tracks/{trackId}/favorite` | `Response<Unit>` |
| `unfavoriteTrack` | DELETE | `tracks/{trackId}/favorite` | `Response<Unit>` |
| `repostTrack` | POST | `tracks/{trackId}/repost` | `Response<Unit>` |
| `unrepostTrack` | DELETE | `tracks/{trackId}/repost` | `Response<Unit>` |
| `updateTrack` | PUT | `tracks/{trackId}` | `Response<Unit>` |
| `deleteTrack` | DELETE | `tracks/{trackId}` | `Response<Unit>` |

`is_streamable=false` треки отфильтровываются плеером до постановки в очередь.

---

## PlaylistApi

Файл: `api/PlaylistApi.kt`

| Метод | HTTP | Endpoint | Параметры |
|---|---|---|---|
| `getPlaylists` | GET | `playlists` | `app_name`, `id` (список) |
| `getPlaylist` | GET | `playlists/{playlistId}` | `app_name` |
| `getPlaylistTracks` | GET | `playlists/{playlistId}/tracks` | `app_name`, `limit=100`, `offset=0` |
| `searchPlaylists` | GET | `playlists/search` | `query`, `app_name`, `limit=25`, `offset=0` |
| `getTrendingPlaylists` | GET | `playlists/trending` | `app_name`, `time=week`, `limit=25`, `offset=0` |
| `getPlaylistFavorites` | GET | `playlists/{playlistId}/favorites` | `app_name` |
| `getPlaylistAccessInfo` | GET | `playlists/{playlistId}/access_info` | `app_name` |
| `favoritePlaylist` | POST | `playlists/{playlistId}/favorite` | `app_name` |
| `unfavoritePlaylist` | DELETE | `playlists/{playlistId}/favorite` | `app_name` |
| `repostPlaylist` | POST | `playlists/{playlistId}/repost` | `app_name` |
| `unrepostPlaylist` | DELETE | `playlists/{playlistId}/repost` | `app_name` |
| `updatePlaylist` | PUT | `playlists/{playlistId}` | body `UpdatePlaylistRequest` |
| `deletePlaylist` | DELETE | `playlists/{playlistId}` | `app_name` |

`getPlaylist` возвращает `JsonElement`, потому что Audius может отдать как
объект, так и массив. `MusicRepositoryImpl.getPlaylist` нормализует оба случая
через Gson.

---

## UserApi

Файл: `api/UserApi.kt`

### Профили и поиск

| Метод | HTTP | Endpoint | Параметры |
|---|---|---|---|
| `getUsers` | GET | `users` | `app_name`, `id` (список) |
| `getUser` | GET | `users/{userId}` | `app_name` |
| `getUserByHandle` | GET | `users/handle/{handle}` | `app_name` |
| `searchUsers` | GET | `users/search` | `query`, `app_name`, `genre[]`, `sort_method?`, `is_verified?`, `limit=25`, `offset=0` |
| `getCurrentUser` | GET | `users/me` | `app_name` |
| `getAccount` | GET | `users/account` | `app_name` |

### Контент пользователя

| Метод | HTTP | Endpoint | Параметры |
|---|---|---|---|
| `getUserTracks` | GET | `users/{userId}/tracks` | `limit=50`, `offset=0`, `sort_method?`, `sort_direction?`, `filter_tracks?`, `query?` |
| `getUserTracksByHandle` | GET | `users/handle/{handle}/tracks` | те же |
| `getAiAttributedTracks` | GET | `users/{userId}/tracks/ai_attributed` | `sort?`, `sort_method?`, `sort_direction?`, `filter_tracks?`, `limit`, `offset` |
| `getUserPlaylists` | GET | `users/{userId}/playlists` | `limit=50`, `offset=0` |
| `getUserFavorites` | GET | `users/{userId}/favorites` | `limit=50`, `offset=0` |
| `getUserReposts` | GET | `users/{userId}/reposts` | `limit=50`, `offset=0` |
| `getFollowers` | GET | `users/{userId}/followers` | `limit=50`, `offset=0` |
| `getFollowing` | GET | `users/{userId}/following` | `limit=50`, `offset=0` |
| `getRelatedUsers` | GET | `users/{userId}/related` | `limit=25` |
| `getUserTags` | GET | `users/{userId}/tags` | `app_name` |
| `getUserHistory` | GET | `users/{userId}/history` | `limit=50`, `offset=0` |

### Действия

| Метод | HTTP | Endpoint |
|---|---|---|
| `followUser` | POST | `users/{userId}/follow` |
| `unfollowUser` | DELETE | `users/{userId}/follow` |
| `muteUser` | POST | `users/{userId}/mute` |
| `unmuteUser` | DELETE | `users/{userId}/mute` |
| `verifyToken` | GET | `users/verify_token` |

---

## SearchApi

Файл: `api/SearchApi.kt`

| Метод | HTTP | Endpoint | Параметры | Возврат |
|---|---|---|---|---|
| `search` | GET | `search` | `query`, `app_name`, `genre?`, `mood?`, `limit=25`, `offset=0` | `JsonElement` |
| `searchFull` | GET | `search/full` | `query`, `app_name`, `limit`, `offset`, `kind?` | `JsonElement` |
| `autocomplete` | GET | `search/autocomplete` | `query`, `app_name`, `limit=10` | `JsonElement` |

Основной экран поиска **не** использует `SearchApi`: он параллельно вызывает
`TrackApi.searchTracks`, `UserApi.searchUsers` и `PlaylistApi.searchPlaylists`,
чтобы получить типизированные списки для трёх вкладок. `SearchApi` доступен для
будущих сценариев (единый поиск, подсказки).

---

## DTO и маппинг

Файлы:

- `dto/AudiusDtos.kt` — сетевые модели с `@SerializedName`.
- `mapper/AudiusMappers.kt` — `toDomain()` / `toDomainOrNull()`.

| DTO | Доменная модель |
|---|---|
| `TrackDto` | `com.sona.core.common.model.Track` |
| `UserDto` | `com.sona.core.common.model.Artist` |
| `PlaylistDto` | `com.sona.core.common.model.Playlist` |
| `CommentDto` | используется напрямую в API-слое |

Мапперы:

- склеивают `artwork.150x150 / 480x480 / 1000x1000` в `artworkUrl`;
- подставляют `streamUrl` из `AudiusConfig.streamUrl(id)`;
- отбрасывают записи без `id` / `title` (`toDomainOrNull`);
- подставляют `handle`, если `name` пустой.

```kotlin
fun TrackDto.toDomainOrNull(): Track? {
    val trackId = id ?: return null
    val title = title ?: return null
    return Track(
        id = trackId,
        title = title,
        artist = user?.toDomain() ?: Artist(id = "", handle = "", name = "Unknown artist"),
        artworkUrl = artwork?.bestUrl(),
        // ...
    )
}
```

---

## Репозиторий

`MusicRepositoryImpl` — единственная точка, где сеть превращается в доменные
модели и `Result<T>`.

| Метод репозитория | API |
|---|---|
| `getTrending(genre, time, limit, offset)` | `TrackApi.getTrendingTracks` |
| `search(query, limit)` | `TrackApi.searchTracks` + `UserApi.searchUsers` + `PlaylistApi.searchPlaylists` |
| `getTrack(trackId)` | `TrackApi.getTrack` |
| `getUser(userId)` | `UserApi.getUser` |
| `getUserTracks(userId)` | `UserApi.getUserTracks` |
| `getPlaylist(playlistId)` | `PlaylistApi.getPlaylist` |
| `getPlaylistTracks(playlistId)` | `PlaylistApi.getPlaylistTracks` |
| `getCurrentUser()` | `UserApi.getCurrentUser` |

Все вызовы:

1. выполняются на `Dispatchers.IO` через `DispatcherProvider`;
2. оборачиваются в `runCatching`;
3. возвращают `Result<T>`, а не бросают исключения в UI;
4. для поиска используют `supervisorScope` — частичный успех допустим.

---

## Стриминг

`Track.streamUrl` формируется локально:

```text
https://api.audius.co/v1/tracks/{trackId}/stream?app_name=Sona
```

`ExoAudioPlayer` строит `MediaItem`:

```kotlin
private fun toMediaItem(track: Track): MediaItem = MediaItem.Builder()
    .setMediaId(track.id)
    .setUri(track.streamUrl)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artistName)
            .setArtworkUri(track.artworkUrl?.toUri())
            .build(),
    )
    .build()
```

- `@Streaming` в Retrofit нужен только для прямого скачивания/стриминга через
  `ResponseBody`; ExoPlayer работает с URL напрямую.
- `PlaybackService` — foreground-service с `mediaPlayback`, поэтому музыка
  продолжает играть при свёрнутом приложении.
- `isStreamable=false` треки не попадают в очередь.

---

## Ошибки и таймауты

| Ситуация | Поведение |
|---|---|
| Нет сети | `Result.failure`, экран показывает `ErrorState` + Retry |
| HTTP 4xx/5xx | Retrofit бросает `HttpException`, репозиторий заворачивает в `Result.failure` |
| Пустой `data` | `.orEmpty()` → пустой список или `error("Track not found")` |
| Таймаут | 30 s connect / 45 s read |
| Один из трёх поисков упал | остальные результаты показываются |
| Все три поиска упали | ошибка на экране |
| Нет токена | приватные запросы вернут 401; UI покажет сообщение |

Логирование — `BASIC` (метод, URL, код, время), без тел запросов и токенов.

---

## Примеры запросов

### Тренды за неделю

```bash
curl "https://api.audius.co/v1/tracks/trending?app_name=Sona&time=week&limit=50"
```

### Поиск треков

```bash
curl "https://api.audius.co/v1/tracks/search?query=lofi&app_name=Sona&limit=25"
```

### Треки артиста

```bash
curl "https://api.audius.co/v1/users/{userId}/tracks?app_name=Sona&limit=50"
```

### Треки плейлиста

```bash
curl "https://api.audius.co/v1/playlists/{playlistId}/tracks?app_name=Sona&limit=100"
```

### Стрим

```text
https://api.audius.co/v1/tracks/{trackId}/stream?app_name=Sona
```

### С токеном

```bash
curl -H "Authorization: Bearer $AUDIUS_TOKEN" \
  "https://api.audius.co/v1/users/me?app_name=Sona"
```

---

## Ограничения

- **Нет OAuth-флоу**: токен вставляется вручную в настройках.
- **Лайки локальные**: Audius favorite/repost эндпоинты объявлены, но UI их не
  вызывает без токена.
- **Нет пагинации в UI**: лимиты фиксированы (50–100), «загрузить ещё» нет.
- **Нет кэша ответов**: Coil кэширует только изображения; JSON каждый раз из сети.
- **Нет retry с backoff**: только `retryOnConnectionFailure` OkHttp.
- **`SearchApi` не используется экраном поиска** — оставлен как контракт для
  будущего единого поиска.
- **`playlists/{id}` нестабилен по форме** — нормализуется вручную.
- **Живая проверка API** в окружении сборки недоступна (нет эмулятора и сети),
  поэтому все проверки — компиляция + контракты + документация.

---

Дальше: [архитектура](ARCHITECTURE.md) · [экраны](SCREENS.md) · [анимации](ANIMATIONS.md) · [модули](MODULES.md).
