# Audius network module

Пакет `com.sona.core.network.audius`. Никаких `com.example.*` — только чистые слои:

```
audius/
├── api/          # Retrofit-интерфейсы по доменам
│   ├── TrackApi.kt
│   ├── PlaylistApi.kt
│   ├── UserApi.kt
│   └── SearchApi.kt
├── di/           # Dagger
│   ├── NetworkModule.kt   # Gson, OkHttp, Retrofit
│   ├── ApiModule.kt       # 4 API через DI
│   └── MusicModule.kt     # MusicRepository
├── dto/          # сетевые модели
├── mapper/       # DTO -> domain
├── model/        # enum-фильтры/сортировки
├── interceptor/  # авторизация
└── repository/   # MusicRepositoryImpl
```

## API по доменам

### `TrackApi` — музыка/треки
| Метод | Endpoint |
|---|---|
| `getTrendingTracks` | `GET tracks/trending` |
| `getUndergroundTrendingTracks` | `GET tracks/trending/underground` |
| `getTrendingTrackIds` | `GET tracks/trending/ids` |
| `getTracks` | `GET tracks` |
| `getTrack` | `GET tracks/{trackId}` |
| `searchTracks` | `GET tracks/search` |
| `getRelatedTracks` | `GET tracks/{trackId}/related` |
| `getTrackRemixes` | `GET tracks/{trackId}/remixes` |
| `getTrackStems` | `GET tracks/{trackId}/stems` |
| `getTrackComments` | `GET tracks/{trackId}/comments` |
| `postTrackComment` | `POST tracks/{trackId}/comments` |
| `getTrackAccessInfo` | `GET tracks/{trackId}/access_info` |
| `streamTrack` | `GET tracks/{trackId}/stream` |
| `downloadTrack` | `GET tracks/{trackId}/download` |
| `favoriteTrack` / `unfavoriteTrack` | `POST/DELETE tracks/{trackId}/favorite` |
| `repostTrack` / `unrepostTrack` | `POST/DELETE tracks/{trackId}/repost` |
| `updateTrack` / `deleteTrack` | `PUT/DELETE tracks/{trackId}` |

### `PlaylistApi` — плейлисты/альбомы
| Метод | Endpoint |
|---|---|
| `getPlaylists` | `GET playlists` |
| `getPlaylist` | `GET playlists/{playlistId}` |
| `getPlaylistTracks` | `GET playlists/{playlistId}/tracks` |
| `searchPlaylists` | `GET playlists/search` |
| `getTrendingPlaylists` | `GET playlists/trending` |
| `getPlaylistFavorites` | `GET playlists/{playlistId}/favorites` |
| `getPlaylistAccessInfo` | `GET playlists/{playlistId}/access_info` |
| `favoritePlaylist` / `unfavoritePlaylist` | `POST/DELETE playlists/{playlistId}/favorite` |
| `repostPlaylist` / `unrepostPlaylist` | `POST/DELETE playlists/{playlistId}/repost` |
| `updatePlaylist` / `deletePlaylist` | `PUT/DELETE playlists/{playlistId}` |

### `UserApi` — аккаунт/пользователи
| Метод | Endpoint |
|---|---|
| `getUsers` | `GET users` |
| `getUser` | `GET users/{userId}` |
| `getUserByHandle` | `GET users/handle/{handle}` |
| `searchUsers` | `GET users/search` |
| `getCurrentUser` | `GET users/me` |
| `getAccount` | `GET users/account` |
| `getUserTracks` | `GET users/{userId}/tracks` |
| `getUserTracksByHandle` | `GET users/handle/{handle}/tracks` |
| `getAiAttributedTracks` | `GET users/{userId}/tracks/ai_attributed` |
| `getUserPlaylists` | `GET users/{userId}/playlists` |
| `getUserFavorites` | `GET users/{userId}/favorites` |
| `getUserReposts` | `GET users/{userId}/reposts` |
| `getFollowers` / `getFollowing` | `GET users/{userId}/followers` / `following` |
| `getRelatedUsers` | `GET users/{userId}/related` |
| `getUserTags` | `GET users/{userId}/tags` |
| `getUserHistory` | `GET users/{userId}/history` |
| `followUser` / `unfollowUser` | `POST/DELETE users/{userId}/follow` |
| `muteUser` / `unmuteUser` | `POST/DELETE users/{userId}/mute` |
| `verifyToken` | `GET users/verify_token` |

### `SearchApi` — общий поиск
| Метод | Endpoint |
|---|---|
| `search` | `GET search` |
| `searchFull` | `GET search/full` |
| `autocomplete` | `GET search/autocomplete` |

## DI

`NetworkModule` даёт `Gson` / `OkHttpClient` / `Retrofit`, `ApiModule` — четыре API.
Любая фича может попросить только нужный интерфейс в конструкторе:

```kotlin
@Inject constructor(
    private val trackApi: TrackApi,
    private val playlistApi: PlaylistApi,
    private val userApi: UserApi,
    private val searchApi: SearchApi,
)
```

`AppComponent` также отдаёт их наружу:

```kotlin
component.trackApi()
component.playlistApi()
component.userApi()
component.searchApi()
```

Все запросы автоматически добавляют `app_name` и, если токен сохранён,
`Authorization: Bearer …` через `AuthInterceptor`.
