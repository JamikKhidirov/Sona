# Changelog

Все значимые изменения Sona. Формат — [Keep a Changelog](https://keepachangelog.com/ru/1.1.0/),
версии — [SemVer](https://semver.org/lang/ru/).

---

## [1.0.0] — 2026-10-31

Первый полный релиз: приложение собирается, все экраны реализованы, сеть
разделена на доменные API, документация на русском.

Тег: `v1.0.0` · `versionCode = 1` · `versionName = "1.0"` · `applicationId = com.sona.music`

### Добавлено

**Воспроизведение**

- `:core:audio:api` — контракт `AudioPlayer` + `PlayerState` без зависимости от Media3.
- `:core:audio:impl` — `ExoAudioPlayer` на Media3 ExoPlayer: очередь, shuffle,
  repeat (off/all/one), seek, обработка ошибок, авто-переход к следующему треку.
- `PlaybackService` — foreground-service типа `mediaPlayback`, музыка играет
  при свёрнутом приложении.
- `PlaybackServiceInjector` — внедрение Dagger в сервис.
- Фильтрация нестримабельных треков (`is_streamable=false`) до постановки в очередь.
- Медиа-уведомление с обложкой, названием и артистом.

**Экраны**

- `splash` — бренд, слоган, индикатор загрузки, автопереход через 1.1 с.
- `home` — тренды, новые треки, артисты, жанровые фильтры, featured-карточка,
  приветствие по времени суток.
- `search` — три вкладки (треки / артисты / плейлисты), debounce, жанры,
  пустые и ошибочные состояния.
- `favorites` — лайки, «недавно играло», суммарное время, play-all.
- `profile` — карточка аккаунта, статистика, лайки, недавние, меню, «О приложении».
- `track/{trackId}` — обложка, артист, жанр/настроение, статистика, описание,
  похожие треки.
- `collection?source&id&title` — trending / genre / artist / playlist в одном экране.
- `player` — полноэкранный плеер, seek, shuffle, repeat, очередь в `ModalBottomSheet`.
- `settings` — тема (system/light/dark), Audius-токен, очистка библиотеки,
  устройство, разрешения.

**Навигация**

- `NavHost` с едиными переходами: `fadeIn 260 ms + slideInHorizontally(1/14)`,
  pop — зеркально.
- Top-level вкладки через `popUpTo(home) { saveState = true }` + `launchSingleTop`
  + `restoreState`.
- Splash удаляется из back stack.
- `SonaBottomBar` с `animateColorAsState`, скрывается на `player` и `splash`.
- Mini-player с прогрессом и переходом в полный плеер.

**Сеть**

- Разделение Audius API на четыре доменных интерфейса:
  - `TrackApi` — треки, стриминг, лайки, репосты, комментарии, ремиксы, stems;
  - `PlaylistApi` — плейлисты, альбомы, треки плейлиста, поиск, тренды;
  - `UserApi` — пользователи, артисты, подписки, контент, история;
  - `SearchApi` — общий поиск, full search, autocomplete.
- `NetworkModule` — `Gson`, `OkHttpClient` с таймаутами 30/45/30 с, `Retrofit`.
- `ApiModule` — четыре API, `MusicModule` — биндинг `MusicRepository`.
- `AppComponent` отдаёт `trackApi()`, `playlistApi()`, `userApi()`, `searchApi()`.
- `AuthInterceptor` + `TokenProvider` — bearer-токен из DataStore.
- `MusicRepositoryImpl` — единая точка «сеть → домен», `Result<T>`, `supervisorScope`
  для поиска.
- DTO + мапперы: `TrackDto`, `UserDto`, `PlaylistDto`, `CommentDto` → доменные модели.

**Домен и данные**

- `core/common/model`: `Track`, `Artist`, `Playlist`, `SearchResult`, `Genres`,
  `AudiusConfig`, `ThemeMode`, `RepeatMode`.
- Use cases: `GetTrendingTracks`, `SearchCatalog`, `GetTrack`, `GetArtist`,
  `GetArtistTracks`, `GetPlaylist`, `GetPlaylistTracks`, `GetCurrentUser`,
  `ToggleFavorite`, `ObserveFavorites`, `ObserveFavoriteIds`, `ObserveRecentlyPlayed`,
  `AddRecentlyPlayed`, `ObserveSettings`, `SetThemeMode`, `SetApiToken`, `ClearLibrary`.
- `LibraryRepositoryImpl` и `SettingsRepositoryImpl` на DataStore (`sona_prefs`).
- Ключи: `theme_mode`, `api_token`, `favorites_json`, `recently_played_json`.

**Информация об устройстве**

- 28 self-contained reader-модулей под `core/data/*`: `alarms`, `apps`, `battery`,
  `bluetooth`, `browser`, `clipboard`, `contacts`, `device`, `display`, `documents`,
  `downloads`, `gallery`, `hardware`, `location`, `media`, `network`, `nfc`,
  `notifications`, `performance`, `permissions`, `sensors`, `sms`, `sound`,
  `storage`, `usage`, `usb`, `vpn`, `wifi`.
- `DeviceInfoCollector` — параллельный сбор через `coroutineScope { async { … } }`.
- `DeviceSnapshot` — иммутабельный снимок.
- Каждый reader проверяет runtime-разрешение и не бросает исключений.

**UI**

- `:core:ui` — общие компоненты: `Artwork`, `TrackRow`, `TrackTile`, `ArtistRow`,
  `ArtistTile`, `PlaylistRow`, `PlayingIndicator`, `MiniPlayer`, `SonaBackground`,
  `SonaLogo`, `SonaChip`, `SectionHeader`, `EmptyState`, `ErrorState`, `LoadingState`,
  `GradientCircleButton`, `CircleIconButton`, `OutlinePill`, `StatPill`.
- Тема: цвета, типографика, формы, `SonaTheme`.
- Форматтеры: `formatDuration`, `formatCount`, `greetingForNow`.
- Компоненты фич в `feature/*/impl/presentation/components/`:
  `HomeHeader`, `HomeSearchPill`, `FeaturedTrackCard`, `SearchField`, `BrowseGenres`,
  `GenreCard`, `FavoritesHero`, `TrackDetailHero`, `CollectionHero`, `ProfileHero`,
  `ProfileStat`, `ConnectAudiusCard`, `ProfileTracksRow`, `ProfileMenuItem`,
  `SettingsSectionLabel`, `ThemeOption`, `SettingsActionRow`, `AboutRow`,
  `DeviceInfoCard`, `PlayerControls`, `QueueSheet`, `SonaBottomBar`, `SplashBrand`.
- Анимации: кроссфейд обложек 220 мс, эквалайзер `0.25 → 1` (420/530/640 мс,
  `Reverse`), `animateColorAsState` в bottom bar.
- **Аватар приложения** `ic_sona_mark` — один градиентный знак с нотой для
  splash, шапки Home и adaptive-иконки запуска.
- Splash-анимация: пружинный вход аватара 0.82 → 1.0 (~600 мс), «дыхание»
  1 → 1.045 → 1 (1.6 s), пульсирующее радиальное свечение.
- Unit-тесты: `FormattersTest` (длительность, счётчики) и `AudiusMappersTest`
  (треки, артисты, плейлисты, fallback'и, `streamUrl`) — 8 содержательных
  тестов, 0 падений.

**Документация**

- `README.md` — обзор, возможности, экраны, навигация, анимации, архитектура,
  стек, сборка, теги.
- `docs/ARCHITECTURE.md` — слои, потоки данных, DI, решения.
- `docs/SCREENS.md` — все экраны, состояния, интенты, компоненты.
- `docs/ANIMATIONS.md` — переходы, тайминги, эквалайзер, как менять.
- `docs/API.md` — Audius API по доменам, DTO, маппинг, стриминг, ошибки.
- `docs/MODULES.md` — дерево модулей, правила зависимостей, как добавить фичу.
- `docs/DI.md` — Dagger 2: компонент, модули по слоям, скоупы, ViewModel
  multibinding, инъекция в сервис, тестирование графа, ошибки KSP.
- `docs/BUILD.md` — требования, команды, APK, подпись, troubleshooting.
- `CHANGELOG.md` — этот файл.
- Анимированные SVG: `docs/assets/hero.svg`, `nav-transition.svg`, `equalizer.svg`.

### Изменено

- **Архитектура модулей**: `core:model` и `core:domain` удалены; модели, контракты
  и use cases переехали в `:core:common`, сеть — в `:core:network:api:music`.
- `core:data` стал агрегатором device-info, а не слоем данных музыки.
- Общий UI переехал из `core/uikit` в **`:core:ui`**; старый модуль удалён.
- **Все пакеты переименованы** `com.example.*` → `com.sona.*`; namespace,
  applicationId и тестовые классы приведены к единому виду.
- `PlaylistApi`, `TrackApi`, `UserApi` вынесены из монолитного API-интерфейса
  в отдельные контракты — фича инжектит только нужное.
- Обложки: единый `Artwork` вместо трёх реализаций в разных экранах.
- Навигация: переходы заданы глобально в `NavHost`, а не в каждом экране.
- Тема и палитра переработаны под «тёмный музыкальный» стиль с градиентами.
- `app_name` для Audius — `Sona` (было `sona`).

### Исправлено

- `MusicRepositoryImpl`: потерянные импорты `TrackApi`, `PlaylistApi`, `UserApi`
  вызывали `KSP failed with exit code: PROCESSING_ERROR` →
  `InjectProcessingStep was unable to process 'MusicRepositoryImpl(...)'`.
  Импорты восстановлены, KSP-шаг проходит.
- Тестовые исходники всех 12 модулей не компилировались (отсутствовали
  `testImplementation`/`androidTestImplementation` и runner) — зависимости добавлены.
- `:core:audio:api` был JVM-модулем, но использовал Android-типы — переведён
  в Android-библиотеку с namespace `com.sona.core.audio.api`.
- Дублирующиеся файлы в `com.example` после переноса удалены, пустые каталоги убраны.
- Ссылки в документации указывали на удалённые модули (`core:uikit`, `core:model`) —
  обновлены.

### Удалено

- `com.example.musicapp` и все `com.example.*` пакеты.
- `:core:model`, `:core:domain`, `:core:uikit`.
- Шаблонные `ExampleUnitTest` / `ExampleInstrumentedTest` из `com.example`.
- Дублирующие реализации обложек и карточек в фичах (заменены на `:core:ui`).

### Проверено

| Команда | Результат |
|---|---|
| `:core:network:api:music:compileDebugKotlin` | BUILD SUCCESSFUL |
| `:app:assembleDebug` | BUILD SUCCESSFUL |
| `:core:ui:testDebugUnitTest :core:network:api:music:testDebugUnitTest` | BUILD SUCCESSFUL, 10 тестов, 0 падений |
| `compileDebugUnitTestKotlin compileDebugAndroidTestKotlin` | BUILD SUCCESSFUL |
| `:app:assembleRelease` | BUILD SUCCESSFUL |

Артефакты: `app-debug.apk` ≈ 29 МБ, `app-release-unsigned.apk` ≈ 19 МБ.

### Известные ограничения

- Audius API не проверялся живыми запросами: в сборочном окружении нет
  эмулятора, `adb` и сети.
- Release-APK не подписан.
- Нет OAuth-флоу: токен вводится вручную в настройках.
- Лайки хранятся локально (DataStore); favorite/repost эндпоинты объявлены,
  но UI их не вызывает без токена.
- Нет пагинации в UI: фиксированные лимиты 25–100.
- Нет кэша JSON-ответов (Coil кэширует только изображения).
- Заготовки: `core/cache`, `core/camera`, `core/worker:sync`, `feature/comments`,
  `core/network/firebase/auth`, `core/network/supabase/auth`.

---

## Формат следующих версий

```text
## [1.1.0] — YYYY-MM-DD
### Добавлено
### Изменено
### Исправлено
### Удалено
### Проверено
```

- **MAJOR** — несовместимые изменения публичного контракта модулей.
- **MINOR** — новые экраны, API-домены, модули.
- **PATCH** — исправления без новых возможностей.

---

[1.0.0]: https://github.com/JamikKhidirov/Sona/releases/tag/v1.0.0
