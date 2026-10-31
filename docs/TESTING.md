# Тестирование Sona

Документ описывает стратегию тестирования, полную матрицу тестов, тестовые
двойники, правила написания и запуск в CI. Связанные документы:
[ARCHITECTURE.md](ARCHITECTURE.md#9-тестируемость-и-швы),
[CI_CD.md](CI_CD.md), [MODULES.md](MODULES.md).

---

## Содержание

- [Принципы](#принципы)
- [Как запускать](#как-запускать)
- [Матрица тестов](#матрица-тестов)
- [Итоговые счётчики](#итоговые-счётчики)
- [Тестовые двойники](#тестовые-двойники)
- [MainDispatcherRule и корутины](#maindispatcherrule-и-корутины)
- [Правила написания тестов](#правила-написания-тестов)
- [Что покрыто по областям](#что-покрыто-по-областям)
- [Что не тестируется](#что-не-тестируется)
- [Тесты в CI](#тесты-в-ci)
- [Как добавить тест](#как-добавить-тест)
- [Troubleshooting](#troubleshooting)

---

## Принципы

1. **Быстрые JVM-тесты.** Всё, что можно проверить без Android, проверяется
   обычным JUnit 4 на JVM: модели, use cases, репозитории, форматтеры,
   ViewModel.
2. **Без сети и эмулятора.** Ни один unit-тест не ходит в Audius, не читает
   DataStore и не запускает `ExoPlayer`.
3. **Двойники, а не магия.** Вместо Mockito используются маленькие ручные
   фейки: так тест читается как сценарий и не зависит от рефлексии.
4. **Поведение, а не реализация.** Проверяем `UiState`, `Effect`, вызовы
   контрактов и результат, а не приватные методы.
5. **Один тест — один сценарий.** Имя теста описывает ожидаемое поведение:
   `failure_uses_throwable_message_and_falls_back_to_default`.
6. **Детерминизм.** Все корутины идут через `StandardTestDispatcher`,
   `advanceUntilIdle()`; время и порядок событий контролируются тестом.

Инструменты: **JUnit 4**, **kotlinx-coroutines-test**, стандартные assertions
(`assertEquals`, `assertTrue`, `assertNull`, `assertFailsWith`).

---

## Как запускать

```bash
# все unit-тесты всех модулей
./gradlew testDebugUnitTest

# только содержательные тесты (12 модулей)
./gradlew \
  :core:common:testDebugUnitTest \
  :core:audio:api:testDebugUnitTest \
  :core:network:api:music:testDebugUnitTest \
  :core:ui:testDebugUnitTest \
  :feature:home:impl:testDebugUnitTest \
  :feature:detailmusic:impl:testDebugUnitTest \
  :feature:searchmusic:impl:testDebugUnitTest \
  :feature:favorites:impl:testDebugUnitTest \
  :feature:musiclist:impl:testDebugUnitTest \
  :feature:profile:impl:testDebugUnitTest \
  :feature:player:impl:testDebugUnitTest \
  :feature:settings:impl:testDebugUnitTest
```

На Windows: `.\gradlew.bat ...`.

Компиляция тестовых исходников без запуска (включая будущие
instrumentation-тесты):

```bash
./gradlew compileDebugUnitTestKotlin compileDebugAndroidTestKotlin
```

Отчёт HTML по модулю:

```text
<module>/build/reports/tests/testDebugUnitTest/index.html
```

Например:
`feature/home/impl/build/reports/tests/testDebugUnitTest/index.html`.

Через Fastlane:

```bash
bundle exec fastlane test          # unit-тесты
bundle exec fastlane test_sources  # компиляция test + androidTest
bundle exec fastlane ci            # test + test_sources + build_debug
```

---

## Матрица тестов

### Ядро

| Модуль | Класс | Тестов | Что проверяется |
|---|---|---:|---|
| `core:common` | `ModelsTest` | 14 | `Track.streamUrl`/`shareUrl`, абсолютные и относительные `permalink`, fallback на `streamUrl`, `Artist.displayName`, выбор обложки, `AudiusConfig`, жанры, пустой `SearchResult`, `AppSettings.isSignedIn`, значения enum |
| `core:common` | `UseCasesTest` | 8 | делегирование всех use cases в репозитории, значения по умолчанию, проброс ошибок, `Flow`-обёртки, мутации библиотеки и настроек |
| `core:audio:api` | `PlayerStateTest` | 9 | `currentTrack` по индексу, выход за границы, `hasNext`/`hasPrevious`, `RepeatMode.ALL/ONE`, clamp прогресса, `copy()` |
| `core:network:api:music` | `AudiusMappersTest` | 8 | маппинг `TrackDto`/`UserDto`/`PlaylistDto`, fallback'и, `streamUrl`, `shareUrl` |
| `core:network:api:music` | `MusicRepositoryImplTest` | 14 | тренды, поиск по трём источникам, частичные и полные отказы, `getTrack`, `getUser`, треки артиста, плейлист-массив и плейлист-объект, лимит 100, `getCurrentUser` |
| `core:ui` | `FormattersTest` | 6 | `formatDuration` (минуты/часы/ноль/отрицательные), `formatCount` (K/M/B, округление), `greetingForHour` по всем бакетам, `greetingForNow` |

### Фичи

| Модуль | Класс | Тестов | Что проверяется |
|---|---|---:|---|
| `feature:home:impl` | `HomeViewModelTest` | 9 | первичная загрузка и производные секции, пустой результат, ошибка с сообщением и fallback, выбор/сброс жанра, play из позиции, toggle текущего трека, featured, лайк, retry |
| `feature:detailmusic:impl` | `DetailMusicViewModelTest` | 9 | загрузка трека и похожих без дублей, ошибка и retry, дефолтный текст ошибки, игнор повторной загрузки, лайк из Flow, очередь «трек + похожие», toggle текущего, сообщения лайка, «в очередь» |
| `feature:searchmusic:impl` | `SearchViewModelTest` | 11 | короткий запрос, trim + debounce, ошибка сохраняет прошлые результаты, дефолтный текст, вкладки, жанр, очистка, retry, очередь из результатов, toggle, лайк |
| `feature:favorites:impl` | `FavoritesViewModelTest` | 7 | заполнение состояния и суммарная длительность, play-all, пустая библиотека, play с индекса, toggle, лайк, очистка |
| `feature:musiclist:impl` | `MusicListViewModelTest` | 12 | trending/genre/artist/playlist, неизвестный источник → trending, пусто, ошибка + retry, дефолтный текст, игнор повторного ключа, play-all/play-track, toggle, лайк |
| `feature:profile:impl` | `ProfileViewModelTest` | 9 | signed-out defaults без API, signed-in загрузка, ошибка, выход и повторный вход, refresh без токена, библиотечные Flow, очередь recently-played/favorites/один трек, toggle |
| `feature:player:impl` | `PlayerViewModelTest` | 7 | производный `UiState`, комбинация player + favorites, transport-интенты, shuffle, лайк текущего, лайк без трека, `RepeatMode` |
| `feature:settings:impl` | `SettingsViewModelTest` | 9 | первичные настройки и токен-драфт, смена темы, сохранение токена, пустой токен, очистка токена, диалог, очистка библиотеки, ошибка device provider, refresh устройства |

### Шаблонные

В 10 перечисленных модулях и ещё в 27 инфраструктурных модулях остаётся
сгенерированный `ExampleUnitTest` (1 тест). Он не проверяет поведение и
существует как заготовка. Полный `testDebugUnitTest` запускает и их.

---

## Итоговые счётчики

| Набор | Тестов |
|---|---:|
| Содержательные тесты (12 модулей) | 132 |
| Шаблонные `ExampleUnitTest` в этих же модулях | 10 |
| **Итого в 12 модулях** | **142** |
| Шаблонные тесты в остальных 37 модулях | 37 |
| **Полный `testDebugUnitTest`** | **179** |

Все тесты проходят: **0 failures, 0 errors**.

---

## Тестовые двойники

Двойники объявляются `private` внутри тестового файла — они не попадают в
публичное API модуля и не мешают другим тестам.

| Двойник | Где | Что умеет |
|---|---|---|
| `FakeMusicRepository` | `core:common`, `home`, `searchmusic`, `detailmusic`, `musiclist` | настраиваемые `Result` для трендов, поиска, трека, артиста, плейлиста, пользователя; счётчики вызовов |
| `FakeLibraryRepository` | `core:common` и все feature-тесты | `MutableStateFlow` любимых и недавних, запись toggle/clear |
| `FakeSettingsRepository` | `core:common`, `profile`, `settings` | `MutableStateFlow<AppSettings>`, сохранение темы и токена |
| `FakeTrackApi` / `FakeUserApi` / `FakePlaylistApi` | `MusicRepositoryImplTest` | полные реализации интерфейсов Retrofit с лямбдами на каждый метод; позволяют отдать `null`, ошибку или битый DTO |
| `FakeAudioPlayer` | все feature-тесты | `MutableStateFlow<PlayerState>`, запись `playQueue`, `togglePlayPause`, `seekTo`, `next/previous`, shuffle/repeat |
| `FakeDeviceSnapshotProvider` | `SettingsViewModelTest` | `Result<DeviceSnapshot>`: успех, ошибка, повторный вызов |
| `MainDispatcherRule` | каждый feature-модуль | подмена `Dispatchers.Main` на `StandardTestDispatcher` |

Пример фейка аудиоплеера:

```kotlin
private class FakeAudioPlayer : AudioPlayer {
    val state = MutableStateFlow(PlayerState())
    var playedQueue: List<Track>? = null

    override val playerState: StateFlow<PlayerState> = state
    override fun playQueue(tracks: List<Track>, startIndex: Int) {
        playedQueue = tracks
    }
    // остальные методы — no-op
}
```

---

## MainDispatcherRule и корутины

`viewModelScope` использует `Dispatchers.Main`, поэтому в тестах его нужно
подменить. В каждом feature-модуле есть копия правила:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

Правило подключается через `@get:Rule`, а тест запускается на том же
диспетчере:

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()

@Test
fun initial_load_fills_state() = runTest(mainDispatcherRule.dispatcher) {
    val vm = HomeViewModel(fakeMusicRepository, fakeLibraryRepository, fakeAudioPlayer)
    vm.onIntent(HomeIntent.Load)
    advanceUntilIdle()

    assertEquals(2, vm.state.value.tracks.size)
}
```

Почему `StandardTestDispatcher`, а не `UnconfinedTestDispatcher`:
он не выполняет корутины до первого `advanceUntilIdle()`, поэтому можно
проверить промежуточное `isLoading` и порядок эффектов. `runTest` получает
тот же диспетчер, чтобы виртуальное время было общим.

Для debounce в поиске используется `advanceTimeBy(...)` + `advanceUntilIdle()`;
для `StateFlow` — `advanceUntilIdle()` после каждого интента.

---

## Правила написания тестов

1. **AAA.** Arrange → Act → Assert, три визуальных блока.
2. **Имя = поведение.** `snake_case`, без `test`-префикса:
   `search_fails_only_when_every_source_fails`.
3. **Один смысл на тест.** Не смешивайте успех и ошибку в одном тесте.
4. **Никаких реальных задержек.** Только `advanceTimeBy` /
   `advanceUntilIdle`, никакого `Thread.sleep`.
5. **Никаких `Dispatchers.IO` в тестах.** Репозитории вызываются напрямую;
   `DispatcherProvider` подменяется только там, где он есть в конструкторе.
6. **Проверяйте эффекты.** Одноразовые сообщения — через `Channel`/`Flow`
   `Effect`; подписка оформляется до отправки интента.
7. **Проверяйте отказ.** Для каждого успешного сценария — хотя бы один тест
   на ошибку, пустой ответ или отсутствующий id.
8. **Не тестируйте фреймворк.** Не проверяйте, что Compose рисует кнопку;
   проверяйте состояние, которое получает кнопка.
9. **Фейк, а не Mockito.** Если нужен новый контракт — допишите маленький
   фейк рядом с тестом.
10. **Тест должен падать при регрессии.** Если тест проходит и при удалении
    логики — он бесполезен.

---

## Что покрыто по областям

| Область | Покрытие | Примеры |
|---|---|---|
| Модели и вычисляемые поля | ✅ | `shareUrl`, `streamUrl`, `displayName`, `bestArtworkUrl`, `isSignedIn`, `Genres` |
| Use cases | ✅ | делегирование, defaults, ошибки, Flow |
| Маппинг DTO → модель | ✅ | `TrackDto`, `UserDto`, `PlaylistDto`, битые элементы, `null` |
| Репозиторий | ✅ | успех, частичный отказ, полный отказ, пустые данные, лимиты |
| Форматтеры | ✅ | длительность, счётчики, приветствие по часам |
| Состояние плеера | ✅ | очередь, индексы, repeat, прогресс |
| ViewModel: загрузка | ✅ | успех, пусто, ошибка, retry, дефолтные сообщения |
| ViewModel: действия | ✅ | play/pause, очередь, лайк, очистка, настройки, токен |
| ViewModel: устройства | ✅ | успешный снимок, ошибка, refresh |
| Навигация, Compose-рендеринг | ❌ | см. ниже |
| Media3 / foreground service | ❌ | требует устройства/эмулятора |

---

## Что не тестируется

- **Compose UI и скриншоты.** Нет Robolectric и screenshot-тестов: UI
  проверяется вручную и через состояние ViewModel.
- **Instrumented-тесты.** Каталоги `src/androidTest` компилируются в CI, но
  не запускаются: нет эмулятора.
- **Media3 / ExoPlayer.** Реальный плеер, foreground service и медиа-сессия
  проверяются на устройстве.
- **Сетевые вызовы.** Retrofit-интерфейсы подменены фейками; live-проверка
  Audius вынесена в отдельную необязательную джобу `api-smoke`.
- **DataStore.** Заменён фейковым `SettingsRepository`; миграции и реальный
  файл проверяются вручную.
- **Device-info readers.** Требуют `Context` и разрешений; тестируется только
  шов `DeviceSnapshotProvider`.

---

## Тесты в CI

GitHub Actions, джоба `test` (`.github/workflows/ci.yml`):

```yaml
- name: Unit tests
  run: ./gradlew testDebugUnitTest --stacktrace

- name: Compile test sources
  run: ./gradlew compileDebugUnitTestKotlin compileDebugAndroidTestKotlin
```

Артефакты отчётов при падении:

```text
**/build/reports/tests/testDebugUnitTest/**
**/build/test-results/testDebugUnitTest/**
```

Fastlane-лейны `test` и `test_sources` делают то же самое локально. Джоба
`test` блокирует `fastlane` и `telegram`: APK не уйдёт, пока тесты красные.

---

## Как добавить тест

### Новая ViewModel

1. Создайте `src/test/java/<package>/<Name>ViewModelTest.kt` в `impl`-модуле.
2. Добавьте `testImplementation(libs.junit)` и
   `testImplementation(libs.kotlinx.coroutines.test)` в `build.gradle.kts`,
   если их ещё нет.
3. Скопируйте `MainDispatcherRule` из соседнего feature-модуля.
4. Опишите фейки репозиториев и `FakeAudioPlayer`.
5. Покройте: начальную загрузку, пустой результат, ошибку, retry, каждое
   действие, одноразовый эффект.

Шаблон:

```kotlin
class FeatureViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val musicRepository = FakeMusicRepository()
    private val libraryRepository = FakeLibraryRepository()
    private val audioPlayer = FakeAudioPlayer()
    private val viewModel by lazy {
        FeatureViewModel(musicRepository, libraryRepository, audioPlayer)
    }

    @Test
    fun load_fills_state() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.onIntent(FeatureIntent.Load)
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.items.size)
        assertFalse(viewModel.state.value.isLoading)
    }
}
```

### Новый репозиторий или API

1. Тестируйте `MusicRepositoryImpl` с фейковыми Retrofit-интерфейсами.
2. Проверьте: успех, `null`-ответ, пустой список, битый DTO, исключение.
3. Не поднимайте MockWebServer, если достаточно фейка интерфейса.

### Новая чистая функция

1. Положите тест рядом с функцией в `core:common` или `core:ui`.
2. Покройте границы: `0`, отрицательные, максимальные значения, пустые
   строки, `null`.
3. Для времени и локали передавайте значение параметром, а не читайте
   системные часы (пример — `greetingForHour`).

---

## Troubleshooting

| Симптом | Причина | Решение |
|---|---|---|
| `Module with the Main dispatcher had failed to initialize` | нет `MainDispatcherRule` | добавьте `@get:Rule` и запускайте `runTest(rule.dispatcher)` |
| Тест зависает | `runTest` без `advanceUntilIdle` | выполните `advanceUntilIdle()` после интента |
| `UncompletedCoroutinesError` | осталась активная коллекция/`Channel` | отмените `viewModelScope` или используйте `backgroundScope` |
| `assertEquals` по `StateFlow` падает | состояние ещё не эмитировано | `advanceUntilIdle()` перед проверкой |
| Тест проходит локально, падает в CI | зависимость от времени/порядка | уберите `Thread.sleep`, используйте виртуальное время |
| `Unresolved reference: test` | не добавлена зависимость | `testImplementation(libs.kotlinx.coroutines.test)` |
| Долгая сборка тестов | холодный Gradle | второй прогон использует кэш; не удаляйте `~/.gradle` без нужды |

---

## Чек-лист перед коммитом

- [ ] `./gradlew testDebugUnitTest` — зелёный.
- [ ] `./gradlew compileDebugUnitTestKotlin compileDebugAndroidTestKotlin` —
      без ошибок.
- [ ] Новое поведение покрыто тестом.
- [ ] Имена тестов описывают поведение.
- [ ] Нет реальной сети, `Thread.sleep` и `Dispatchers.IO`.
- [ ] Счётчики в этом документе и в [README](../README.md) обновлены, если
      число тестов изменилось.
