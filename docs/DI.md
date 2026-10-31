# Dependency Injection в Sona

Sona использует **Dagger 2** с KSP: граф собирается на этапе компиляции, без
рефлексии и без runtime-ошибок «missing binding». Ни одна фича не создаёт
зависимости вручную и не обращается к глобальному синглтону — всё приходит
через конструктор или `@Inject`-поле сервиса.

![Sona](../docs/assets/hero.svg)

---

## Содержание

- [Почему Dagger 2, а не Hilt](#почему-dagger-2-а-не-hilt)
- [Граф приложения](#граф-приложения)
- [AppComponent](#appcomponent)
- [Модули по слоям](#модули-по-слоям)
- [Скоупы и квалификаторы](#скоупы-и-квалификаторы)
- [ViewModel: multibinding + factory](#viewmodel-multibinding--factory)
- [Инъекция в сервис](#инъекция-в-сервис)
- [Правила](#правила)
- [Как добавить зависимость](#как-добавить-зависимость)
- [Как добавить ViewModel](#как-добавить-viewmodel)
- [Как добавить API](#как-добавить-api)
- [Тестирование графа](#тестирование-графа)
- [Ошибки KSP и что они значат](#ошибки-ksp-и-что-они-значат)
- [Проверка](#проверка)
- [Карта файлов](#карта-файлов)

---

## Почему Dagger 2, а не Hilt

| Критерий | Dagger 2 (выбран) | Hilt |
|---|---|---|
| Генерация | KSP, compile-time | обёртка над Dagger + Gradle-плагин |
| Контроль графа | явный `@Component` | скрыт за `@HiltAndroidApp` |
| Multi-module | компонент собирается в `:app` | требует соглашений по модулям |
| Скорость сборки | предсказуемая | плагин + агрегация |
| Размер | минимальный | дополнительные артефакты |
| Обучение | видно весь граф | «магия» аннотаций |

Для 74 модулей и одного `:app` явный компонент оказался проще: видно, какие
модули входят в граф, и легко добавить/убрать слой.

---

## Граф приложения

```mermaid
flowchart TD
    App["SonaApplication"] -->|DaggerAppComponent.factory().create(this)| Component["AppComponent @Singleton"]

    Component --> AppModule
    Component --> NetworkModule
    Component --> ApiModule
    Component --> MusicModule
    Component --> CoreDataModule
    Component --> AudioModule
    Component --> PlayerModule
    Component --> VMBinding["ViewModelBindingModule"]

    NetworkModule --> Gson
    NetworkModule --> OkHttp["OkHttpClient"]
    NetworkModule --> Retrofit
    ApiModule --> TrackApi
    ApiModule --> PlaylistApi
    ApiModule --> UserApi
    ApiModule --> SearchApi
    MusicModule --> MusicRepositoryImpl
    CoreDataModule --> LibraryRepositoryImpl
    CoreDataModule --> SettingsRepositoryImpl
    CoreDataModule --> TokenProvider
    AudioModule --> ExoAudioPlayer
    PlayerModule --> ExoPlayer
    VMBinding --> VMs["Map<Class, Provider<ViewModel>>"]

    MusicRepositoryImpl --> TrackApi
    MusicRepositoryImpl --> PlaylistApi
    MusicRepositoryImpl --> UserApi
    MusicRepositoryImpl --> Gson
    OkHttp --> AuthInterceptor
    AuthInterceptor --> TokenProvider
    Retrofit --> OkHttp
    Retrofit --> Gson
    ExoAudioPlayer --> ExoPlayer
    VMs --> Factory["SonaViewModelFactory"]
```

Ключевая идея: **`:app` — единственное место, где строится граф**. Модули
только объявляют `@Provides` / `@Binds`, а `AppComponent` соединяет их.

---

## AppComponent

Файл: `app/src/main/java/com/sona/music/di/AppComponent.kt`

```kotlin
@Singleton
@Component(
    modules = [
        AppModule::class,
        NetworkModule::class,
        ApiModule::class,
        MusicModule::class,
        CoreDataModule::class,
        AudioModule::class,
        PlayerModule::class,
        ViewModelBindingModule::class,
    ],
)
interface AppComponent {

    fun inject(service: PlaybackService)

    fun viewModelFactory(): SonaViewModelFactory

    fun trackApi(): TrackApi
    fun playlistApi(): PlaylistApi
    fun userApi(): UserApi
    fun searchApi(): SearchApi

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance @ApplicationContext context: Context): AppComponent
    }
}
```

| Элемент | Зачем |
|---|---|
| `@Singleton` | один экземпляр компонента и всех `@Singleton`-зависимостей на процесс |
| `modules = [...]` | полный список модулей графа |
| `inject(PlaybackService)` | field injection для `MediaSessionService` (его создаёт система) |
| `viewModelFactory()` | фабрика для Compose `viewModel(factory = ...)` |
| `trackApi()` … | доступ к доменным API из любого слоя/инструментов |
| `@Component.Factory` | передаёт `Context` в граф без статики |
| `@BindsInstance` | `Context` становится обычной зависимостью |

Создание — ровно один раз, в `SonaApplication`:

```kotlin
class SonaApplication : Application(), PlaybackServiceInjector {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(this)
    }

    override fun inject(service: PlaybackService) {
        appComponent.inject(service)
    }
}
```

`DaggerAppComponent` генерируется KSP. Если его нет в автодополнении — значит
KSP-шаг не отработал (см. [Ошибки KSP](#ошибки-ksp-и-что-они-значат)).

---

## Модули по слоям

### `AppModule` — app-уровень

`app/src/main/java/com/sona/music/di/AppModule.kt`

```kotlin
@Module
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    companion object {
        @Provides
        @Singleton
        @ApplicationScope
        fun provideApplicationScope(dispatchers: DispatcherProvider): CoroutineScope =
            CoroutineScope(SupervisorJob() + dispatchers.default)
    }
}
```

- `DispatcherProvider` — интерфейс, чтобы в тестах подменить диспетчеры.
- `@ApplicationScope` — `CoroutineScope`, живущий столько же, сколько процесс.

### `NetworkModule` — инфраструктура сети

`core/network/api/music/.../di/NetworkModule.kt`

| Провайд | Скоуп | Детали |
|---|---|---|
| `Gson` | `@Singleton` | `setLenient()` |
| `HttpLoggingInterceptor` | `@Singleton` | `Level.BASIC` |
| `OkHttpClient` | `@Singleton` | auth + logging, 30/45/30 s, redirects, retry |
| `Retrofit` | `@Singleton` | base URL `https://api.audius.co/v1/`, Gson converter |

### `ApiModule` — четыре доменных API

`core/network/api/music/.../di/ApiModule.kt`

```kotlin
@Module
object ApiModule {
    @Provides @Singleton
    fun provideTrackApi(retrofit: Retrofit): TrackApi =
        retrofit.create(TrackApi::class.java)

    @Provides @Singleton
    fun providePlaylistApi(retrofit: Retrofit): PlaylistApi =
        retrofit.create(PlaylistApi::class.java)

    @Provides @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides @Singleton
    fun provideSearchApi(retrofit: Retrofit): SearchApi =
        retrofit.create(SearchApi::class.java)
}
```

Разделение даёт фиче только нужный контракт: экран поиска не видит методы
плейлистов, а `MusicRepositoryImpl` видит ровно три API.

### `MusicModule` — репозиторий

```kotlin
@Module
abstract class MusicModule {
    @Binds @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository
}
```

`MusicRepositoryImpl` использует constructor injection:

```kotlin
@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val trackApi: TrackApi,
    private val playlistApi: PlaylistApi,
    private val userApi: UserApi,
    private val gson: Gson,
    private val dispatchers: DispatcherProvider,
) : MusicRepository
```

### `CoreDataModule` — DataStore и токен

`core/common/.../di/CoreDataModule.kt`

| Интерфейс | Реализация |
|---|---|
| `LibraryRepository` | `LibraryRepositoryImpl` |
| `SettingsRepository` | `SettingsRepositoryImpl` |
| `TokenProvider` | тот же `SettingsRepositoryImpl` |

`SettingsRepositoryImpl` одновременно реализует два контракта — Dagger
разрешает это двумя `@Binds` на один `@Singleton` экземпляр.

### `AudioModule` + `PlayerModule`

`core/audio/impl/.../di/AudioModule.kt`

```kotlin
@Module
abstract class AudioModule {
    @Binds @Singleton
    abstract fun bindAudioPlayer(impl: ExoAudioPlayer): AudioPlayer
}

@Module
object PlayerModule {
    @Provides @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer =
        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
}
```

`ExoPlayer` — синглтон: UI, `ExoAudioPlayer` и `PlaybackService` работают с
одним и тем же плеером. Это важно, иначе сервис и экран управляли бы разными
очередями.

### `ViewModelBindingModule`

См. следующий раздел.

---

## Скоупы и квалификаторы

| Аннотация | Где | Что делает |
|---|---|---|
| `@Singleton` | компонент и провайды | один экземпляр на процесс |
| `@ApplicationContext` | `Context` | именно `Application`, не Activity |
| `@ApplicationScope` | `CoroutineScope` | долгоживущий scope |
| `@ViewModelKey` | методы `@Binds @IntoMap` | ключ карты ViewModel |
| `@JvmSuppressWildcards` | параметр фабрики | корректный Java-тип `Map<Class, Provider<VM>>` |

В проекте **нет** `@ActivityScope` / `@FragmentScope`: Activity одна
(`MainActivity`), а экраны — Compose-функции без собственных скоупов.
ViewModel живут в `NavBackStackEntry`, их создаёт `SonaViewModelFactory`.

Квалификаторы объявлены в `core/common/.../di/Qualifiers.kt`:

```kotlin
@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class ApplicationContext

@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
```

---

## ViewModel: multibinding + factory

### Ключ карты

```kotlin
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)
```

### Биндинги

`app/.../di/ViewModelBindingModule.kt`

```kotlin
@Module
abstract class ViewModelBindingModule {

    @Binds @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel

    // ... AppViewModel, SearchViewModel, FavoritesViewModel,
    //     ProfileViewModel, SettingsViewModel, DetailMusicViewModel,
    //     MusicListViewModel, PlayerViewModel
}
```

### Фабрика

`core/common/.../presentation/SonaViewModelFactory.kt`

```kotlin
@Singleton
class SonaViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass]
            ?: creators.entries.firstOrNull { (key, _) -> modelClass.isAssignableFrom(key) }?.value
            ?: error("No ViewModel binding found for ${modelClass.name}")
        return creator.get() as T
    }
}
```

### Как это используется в экране

```kotlin
@Composable
fun HomeRoute(
    onTrackClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = ...),
) { /* ... */ }
```

Фабрика приходит из `AppComponent`; экран не знает ни о `MusicRepository`, ни
о `TrackApi`. Это и есть главная проверка DI: **в UI-слое нет ни одного
`new`/`by lazy` для зависимостей**.

---

## Инъекция в сервис

`PlaybackService` создаётся системой, поэтому constructor injection
невозможен. Используется field injection + интерфейс-мост:

```kotlin
interface PlaybackServiceInjector {
    fun inject(service: PlaybackService)
}
```

```kotlin
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        (application as PlaybackServiceInjector).inject(this)
        mediaSession = MediaSession.Builder(this, player).build()
    }
}
```

`SonaApplication` реализует `PlaybackServiceInjector` и делегирует вызов в
`appComponent.inject(service)`.

---

## Правила

1. **Constructor injection** — по умолчанию. Field injection только там, где
   объект создаёт фреймворк (сервис, Activity при необходимости).
2. **Интерфейс + `@Binds`** — фича зависит от контракта, а не от реализации.
3. **`@Provides`** — только для типов, которые нельзя аннотировать: `Gson`,
   `OkHttpClient`, `Retrofit`, `ExoPlayer`, `CoroutineScope`.
4. **`@Singleton`** — только для действительно общих объектов: сеть, БД,
   репозитории, плеер. ViewModel — никогда.
5. **Никакого `Context` в синглтонах** кроме `@ApplicationContext`.
6. **Диспетчеры через `DispatcherProvider`**, не `Dispatchers.IO` напрямую.
7. **`core/*` не знает о `:app`**. Компонент собирается только в `:app`.
8. **Никакого service locator** (`object Graph`, `companion object` с
   зависимостями) — только инъекция.
9. **Каждый модуль компилируется отдельно**: `@Module` живёт рядом со слоем,
   который предоставляет.
10. **KSP-ошибки чинятся после Kotlin-ошибок**, не наоборот.

---

## Как добавить зависимость

### 1. Свой тип с конструктором

```kotlin
@Singleton
class ArtworkCache @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
)
```

Ничего в модулях писать не нужно — Dagger сам найдёт `@Inject`-конструктор.

### 2. Интерфейс

```kotlin
interface ArtworkCache { /* ... */ }

@Module
abstract class CacheModule {
    @Binds @Singleton
    abstract fun bindArtworkCache(impl: ArtworkCacheImpl): ArtworkCache
}
```

Не забудьте добавить `CacheModule::class` в `AppComponent.modules`.

### 3. Тип без конструктора

```kotlin
@Module
object FooModule {
    @Provides @Singleton
    fun provideFoo(@ApplicationContext context: Context): Foo =
        Foo(context)
}
```

### 4. Проверить граф

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

Dagger падает на этапе KSP, если зависимости не хватает — это и есть
compile-time проверка графа.

---

## Как добавить ViewModel

1. Создать `FeatureViewModel` с `@Inject constructor(...)`.
2. Добавить биндинг:

```kotlin
@Binds @IntoMap
@ViewModelKey(FeatureViewModel::class)
abstract fun bindFeatureViewModel(vm: FeatureViewModel): ViewModel
```

3. Использовать `SonaViewModelFactory` в экране.
4. Собрать `:app:assembleDebug`.

Если забыть шаг 2, приложение упадёт в runtime с
`No ViewModel binding found for ...`. Это единственная runtime-ошибка,
которую DI может дать, — поэтому биндинг проверяется на ревью.

---

## Как добавить API

1. Создать интерфейс в `core/network/api/music/.../api/`.
2. Добавить провайд в `ApiModule`.
3. Экспонировать в `AppComponent` (если нужен снаружи).
4. Использовать в репозитории через constructor injection.

```kotlin
@Provides @Singleton
fun provideCommentApi(retrofit: Retrofit): CommentApi =
    retrofit.create(CommentApi::class.java)
```

---

## Тестирование графа

### Подмена модуля

```kotlin
@Module
object FakeNetworkModule {
    @Provides @Singleton
    fun provideMusicRepository(): MusicRepository = FakeMusicRepository()
}

@Component(modules = [FakeNetworkModule::class])
interface TestComponent {
    fun musicRepository(): MusicRepository
}
```

### Тест без Dagger

Так как всё на constructor injection, большинство тестов обходится без
компонента:

```kotlin
class GetTrendingTracksTest {
    private val repository = FakeMusicRepository()
    private val useCase = GetTrendingTracks(repository)
}
```

### Проверка, что граф собирается

`assembleDebug` — это и есть тест графа: KSP генерирует
`DaggerAppComponent` и падает при любой недостающей зависимости.

---

## Ошибки KSP и что они значат

### `InjectProcessingStep was unable to process 'X(...)' because 'Y' could not be resolved`

```text
e: [ksp] InjectProcessingStep was unable to process 'MusicRepositoryImpl(...)'
   because 'TrackApi' could not be resolved.
```

Это **не** ошибка Dagger. KSP видит «error type» потому, что Kotlin-файл не
импортировал `TrackApi`. Лечение:

1. Собрать без KSP:

```powershell
.\gradlew.bat :core:network:api:music:compileDebugKotlin -x :core:network:api:music:kspDebugKotlin --console=plain
```

2. Найти `e: file:///...Unresolved reference 'TrackApi'`.
3. Добавить импорт:

```kotlin
import com.sona.core.network.audius.api.TrackApi
import com.sona.core.network.audius.api.PlaylistApi
import com.sona.core.network.audius.api.UserApi
```

4. Повторить сборку.

### `DaggerAppComponent` не найден

- KSP-плагин не подключён к `:app` (`id("com.google.devtools.ksp")`).
- Нет `ksp(libs.dagger.compiler)`.
- Сборка упала раньше и генерация не выполнилась.

### `Cannot provide without an @Inject constructor or @Provides`

В графе нет провайда. Добавьте `@Inject constructor`, `@Binds` или `@Provides`
и подключите модуль к `AppComponent`.

### `AppComponent scoped with @Singleton may not reference bindings with different scopes`

В компоненте появился скоуп, которого нет в иерархии. В Sona все провайды —
`@Singleton`; если нужен другой скоуп, сначала добавьте субкомпонент.

### `Map<Class<out ViewModel>, Provider<ViewModel>> cannot be provided`

Забыт `@JvmSuppressWildcards` в параметре фабрики или ни один ViewModel не
забинден через `@IntoMap`.

---

## Проверка

```powershell
# граф целиком
.\gradlew.bat :app:assembleDebug --console=plain

# только сеть (самый хрупкий модуль)
.\gradlew.bat :core:network:api:music:compileDebugKotlin --console=plain

# тестовые исходники
.\gradlew.bat compileDebugUnitTestKotlin compileDebugAndroidTestKotlin --console=plain
```

Ожидаемый результат — `BUILD SUCCESSFUL` и наличие
`app/build/generated/ksp/debug/java/com/sona/music/di/DaggerAppComponent.java`.

---

## Карта файлов

| Файл | Роль |
|---|---|
| `app/.../di/AppComponent.kt` | корневой `@Singleton`-компонент, `@Component.Factory` |
| `app/.../di/AppModule.kt` | `DispatcherProvider`, `@ApplicationScope` |
| `app/.../di/ViewModelBindingModule.kt` | `@IntoMap` биндинги всех ViewModel |
| `app/.../SonaApplication.kt` | создаёт компонент, реализует `PlaybackServiceInjector` |
| `core/common/.../di/CoreDataModule.kt` | репозитории DataStore, `TokenProvider` |
| `core/common/.../di/Qualifiers.kt` | `@ApplicationContext`, `@ApplicationScope` |
| `core/common/.../di/ViewModelKey.kt` | `@MapKey` для ViewModel |
| `core/common/.../presentation/SonaViewModelFactory.kt` | фабрика из карты |
| `core/network/api/music/.../di/NetworkModule.kt` | Gson, OkHttp, Retrofit |
| `core/network/api/music/.../di/ApiModule.kt` | `TrackApi`, `PlaylistApi`, `UserApi`, `SearchApi` |
| `core/network/api/music/.../di/MusicModule.kt` | `MusicRepository` |
| `core/audio/impl/.../di/AudioModule.kt` | `AudioPlayer`, `ExoPlayer` |
| `core/audio/impl/.../service/PlaybackService.kt` | field injection сервиса |

---

Дальше: [архитектура](ARCHITECTURE.md) · [модули](MODULES.md) · [API](API.md) · [сборка](BUILD.md).
