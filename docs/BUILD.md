# Сборка Sona

Всё, что нужно, чтобы собрать проект с нуля, проверить его без устройства и
понять, что именно уже проверено.

![Sona](../docs/assets/hero.svg)

---

## Содержание

- [Требования](#требования)
- [Первый запуск](#первый-запуск)
- [Основные команды](#основные-команды)
- [Что и зачем собирать](#что-и-зачем-собирать)
- [Где лежат APK](#где-лежат-apk)
- [Проверка без устройства](#проверка-без-устройства)
- [Результаты проверок](#результаты-проверок)
- [Подпись release](#подпись-release)
- [Установка на устройство](#установка-на-устройство)
- [Troubleshooting](#troubleshooting)
- [CI-скелет](#ci-скелет)
- [Ограничения окружения](#ограничения-окружения)

---

## Требования

| Компонент | Версия | Комментарий |
|---|---|---|
| JDK | **25** | `gradle/gradle-daemon-jvm.properties` → `toolchainVersion=25` |
| Gradle | **9.5.0** | через `gradlew` / `gradlew.bat`, скачивается сам |
| Android SDK | **API 37** | `compileSdk = 37`, `targetSdk = 37` |
| Build Tools | 37.x | ставится вместе с SDK |
| Android Studio | Narwhal+ | опционально, для IDE |
| Kotlin | 2.2.10 | версия из `libs.versions.toml` |
| AGP | 9.3.3 | `com.android.application` / `com.android.library` |
| minSdk | 24 | Android 7.0 |
| RAM | ≥ 4 ГБ | `org.gradle.jvmargs=-Xmx2048m` |

Проект **не требует** эмулятора, `adb` или подключённого телефона для
`assembleDebug`, `assembleRelease` и компиляции тестовых исходников.

### Что должно быть установлено в SDK

```text
platforms/android-37
build-tools/37.0.0
platform-tools
```

Android Studio ставит их автоматически. Без IDE:

```bash
sdkmanager "platforms;android-37" "build-tools;37.0.0" "platform-tools"
```

---

## Первый запуск

### 1. Локальный SDK

Файл `local.properties` **не коммитится** (он в `.gitignore`). Создайте его:

Windows:

```properties
sdk.dir=C\:\\Users\\<user>\\AppData\\Local\\Android\\Sdk
```

macOS / Linux:

```properties
sdk.dir=/Users/<user>/Library/Android/sdk
```

Альтернатива — переменная окружения `ANDROID_HOME`.

### 2. JDK

Gradle сам скачает нужный toolchain (JDK 25) при первом запуске — это может
занять пару минут. Если JDK 25 уже установлен, укажите его:

```bash
export JAVA_HOME=/path/to/jdk-25      # macOS / Linux
setx JAVA_HOME "C:\Program Files\Java\jdk-25"   # Windows
```

### 3. Первая сборка

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

Linux / macOS:

```bash
./gradlew :app:assembleDebug --console=plain
```

Первый запуск: 3–8 минут (скачивание зависимостей + KSP). Повторный —
20–40 секунд.

---

## Основные команды

### Debug APK

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

### Release APK (unsigned)

```powershell
.\gradlew.bat :app:assembleRelease --console=plain
```

### Проверка исходников тестов

```powershell
.\gradlew.bat compileDebugUnitTestKotlin compileDebugAndroidTestKotlin --console=plain
```

Компилирует `src/test` и `src/androidTest` **всех** модулей, не запуская тесты
и не требуя устройства.

### Один модуль

```powershell
.\gradlew.bat :core:network:api:music:compileDebugKotlin --console=plain
.\gradlew.bat :core:ui:compileDebugKotlin --console=plain
.\gradlew.bat :feature:player:impl:compileDebugKotlin --console=plain
```

Именно так быстрее всего ловить ошибки в конкретном слое.

### Чистая сборка

```powershell
.\gradlew.bat clean :app:assembleDebug --console=plain
```

### Только Kotlin без KSP

Если нужно понять, **код** это или **процессор**:

```powershell
.\gradlew.bat :core:network:api:music:compileDebugKotlin -x :core:network:api:music:kspDebugKotlin --console=plain
```

Это выводит «чистые» ошибки компилятора Kotlin без шума Dagger/KSP.

### Полезные флаги

| Флаг | Зачем |
|---|---|
| `--console=plain` | без анимаций прогресса — нормальный лог |
| `--stacktrace` | полный стек при падении |
| `--info` | какие файлы реально компилируются |
| `--rerun-tasks` | игнорировать up-to-date |
| `--no-configuration-cache` | отключить configuration cache разово |

---

## Что и зачем собирать

| Задача | Что проверяет | Нужен SDK | Нужен эмулятор |
|---|---|---|---|
| `:app:assembleDebug` | весь Kotlin + ресурсы + DEX + упаковка APK | да | нет |
| `:app:assembleRelease` | R8/упаковка release-варианта | да | нет |
| `compileDebugUnitTestKotlin` | тестовые исходники (JVM) | да | нет |
| `compileDebugAndroidTestKotlin` | instrumentation-исходники | да | нет |
| `:core:*:compileDebugKotlin` | один модуль | да | нет |
| `test` | JUnit-тесты | да | нет |
| `connectedDebugAndroidTest` | UI-тесты | да | **да** |

`assembleDebug` — главный критерий «проект компилируется». Он собирает все 74
модуля, включает KSP-генерацию Dagger и упаковку ресурсов.

---

## Где лежат APK

```text
app/build/outputs/apk/debug/app-debug.apk              ≈ 29 МБ
app/build/outputs/apk/release/app-release-unsigned.apk ≈ 19 МБ
```

Размер debug больше из-за отладочных символов, `ui-tooling` и отсутствия
минификации.

Найти после сборки:

```powershell
Get-ChildItem app\build\outputs\apk -Recurse -Filter *.apk |
    Select-Object FullName, @{n='MB';e={[math]::Round($_.Length/1MB,2)}}
```

---

## Проверка без устройства

Полный чек-лист, который имеет смысл прогонять перед коммитом:

```powershell
# 1. Основной модуль сети (самое хрупкое место — Dagger + KSP)
.\gradlew.bat :core:network:api:music:compileDebugKotlin --console=plain

# 2. Общий UI
.\gradlew.bat :core:ui:compileDebugKotlin --console=plain

# 3. Все фичи
.\gradlew.bat :feature:home:impl:compileDebugKotlin :feature:searchmusic:impl:compileDebugKotlin :feature:favorites:impl:compileDebugKotlin :feature:profile:impl:compileDebugKotlin :feature:detailmusic:impl:compileDebugKotlin :feature:musiclist:impl:compileDebugKotlin :feature:player:impl:compileDebugKotlin :feature:settings:impl:compileDebugKotlin :feature:navigation:impl:compileDebugKotlin :feature:splashscreen:impl:compileDebugKotlin --console=plain

# 4. Приложение целиком
.\gradlew.bat :app:assembleDebug --console=plain

# 5. Unit-тесты (форматтеры, мапперы)
.\gradlew.bat :core:ui:testDebugUnitTest :core:network:api:music:testDebugUnitTest --console=plain

# 6. Тестовые исходники
.\gradlew.bat compileDebugUnitTestKotlin compileDebugAndroidTestKotlin --console=plain

# 7. Release
.\gradlew.bat :app:assembleRelease --console=plain
```

Успех — строка `BUILD SUCCESSFUL` в конце каждой команды.

### Что считается «зелёным»

- Нет строк `e: file:///...` (ошибки Kotlin).
- Нет `FAILED` у задач.
- Нет `error:` от AAPT/ресурсов.
- KSP не пишет `could not be resolved`.

Предупреждения (`w:`) допустимы: часть из них — deprecated-API Media3 и
неиспользуемые параметры.

---

## Результаты проверок

Проверено в этом окружении (Windows, JDK 25, SDK 37, без устройства):

| Команда | Результат | Время |
|---|---|---|
| `:core:network:api:music:compileDebugKotlin` | **BUILD SUCCESSFUL** | ~19 s |
| `:app:assembleDebug` | **BUILD SUCCESSFUL** | ~39 s (инкрементально) |
| `:core:ui:testDebugUnitTest :core:network:api:music:testDebugUnitTest` | **BUILD SUCCESSFUL**, 10 тестов, 0 падений | ~1 min 36 s |
| `compileDebugUnitTestKotlin compileDebugAndroidTestKotlin` | **BUILD SUCCESSFUL** | ~52 s |
| `:app:assembleRelease` | **BUILD SUCCESSFUL** | ~3 min |

Что это доказывает:

- весь production-код компилируется;
- Dagger-граф валиден (KSP генерирует компоненты без ошибок);
- ресурсы и манифесты корректны;
- тестовые исходники компилируются;
- release-вариант собирается без минификации.

Что это **не** доказывает:

- что Audius API отвечает так, как ожидается (нет сети/эмулятора);
- что UI выглядит правильно на реальном экране;
- что воспроизведение работает (нужен аудио-выход);
- что runtime-разрешения выдаются (нужен пользователь).

---

## Подпись release

Сейчас release **не подписан** — `app-release-unsigned.apk` нельзя поставить
на устройство.

### 1. Создать keystore

```bash
keytool -genkeypair -v \
  -keystore sona-release.jks \
  -alias sona \
  -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Добавить в `app/build.gradle.kts`

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../sona-release.jks")
            storePassword = System.getenv("SONA_STORE_PASSWORD")
            keyAlias = "sona"
            keyPassword = System.getenv("SONA_KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization { enable = false }
        }
    }
}
```

Пароли — только через переменные окружения или `~/.gradle/gradle.properties`,
никогда в репозитории. `*.jks` и `*.keystore` стоит добавить в `.gitignore`.

### 3. Собрать

```powershell
$env:SONA_STORE_PASSWORD="..."
$env:SONA_KEY_PASSWORD="..."
.\gradlew.bat :app:assembleRelease --console=plain
```

Результат: `app/build/outputs/apk/release/app-release.apk`.

---

## Установка на устройство

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.sona.music/.MainActivity
```

Логи:

```bash
adb logcat | grep -E "sona|AndroidRuntime"
```

Если приложение падает на старте — почти всегда это Dagger (`DaggerAppComponent`
не создан) или отсутствующий `PlaybackService` в манифесте.

---

## Troubleshooting

### `TrackApi could not be resolved` (KSP)

```text
e: [ksp] InjectProcessingStep was unable to process 'MusicRepositoryImpl(...)'
   because 'TrackApi' could not be resolved.
```

Это **не** ошибка Dagger, а следствие ошибки компиляции Kotlin: файл использует
тип, но не импортирует его. KSP видит «error type» и падает первым.

Лечение — посмотреть «чистую» компиляцию:

```powershell
.\gradlew.bat :core:network:api:music:compileDebugKotlin -x :core:network:api:music:kspDebugKotlin --console=plain
```

и добавить недостающие импорты:

```kotlin
import com.sona.core.network.audius.api.TrackApi
import com.sona.core.network.audius.api.PlaylistApi
import com.sona.core.network.audius.api.UserApi
```

Правило: **сначала чинить `e: file:///...`, потом смотреть на `e: [ksp]`**.

### `Unresolved reference` во всех файлах модуля

Обычно сломан `build.gradle.kts` модуля: нет `implementation(project(":core:common"))`
или нет `alias(libs.plugins.kotlin.compose)`. Проверьте `dependencies { }`.

### Configuration cache ломается

```powershell
.\gradlew.bat :app:assembleDebug --no-configuration-cache --console=plain
```

Если помогло — почистите кэш:

```text
.gradle/configuration-cache
```

### `SDK location not found`

Нет `local.properties` или неверный `sdk.dir`. См. [Первый запуск](#первый-запуск).

### Gradle скачивает JDK каждый раз

Задайте `JAVA_HOME` на JDK 25 или проверьте, что
`gradle/gradle-daemon-jvm.properties` не перегенерирован другой версией IDE.

### «broken symlink» при обходе проекта

Windows + `Get-ChildItem -Recurse` по `build/` иногда ругается на симлинки.
Это не влияет на сборку — просто не обходите `build` и `.gradle`.

### OOM / `Java heap space`

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

### Долгая release-сборка

Release дольше debug в 4–5 раз из-за упаковки и обработки ресурсов. Это норма.
Для быстрой проверки кода достаточно `assembleDebug`.

### `Duplicate class` / `Program type already present`

Конфликт зависимостей (чаще Media3 или Compose). Проверьте:

```powershell
.\gradlew.bat :app:dependencies --configuration debugRuntimeClasspath
```

---

## CI-скелет

GitHub Actions, проверка без устройства:

```yaml
name: build
on: [push, pull_request]

jobs:
  assemble:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '25'
      - uses: android-actions/setup-android@v3
      - name: Assemble
        run: ./gradlew :app:assembleDebug --console=plain
      - name: Compile tests
        run: ./gradlew compileDebugUnitTestKotlin compileDebugAndroidTestKotlin --console=plain
      - uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

Кэш Gradle заметно ускоряет повторные прогоны:

```yaml
      - uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ hashFiles('**/*.gradle*', 'gradle/libs.versions.toml') }}
```

---

## Ограничения окружения

- В окружении, где готовился релиз, **не было эмулятора и adb**, поэтому
  `connectedDebugAndroidTest` и ручная проверка UI не выполнялись.
- Сетевой доступ к `api.audius.co` из сборочного окружения не проверялся:
  контракты API подтверждены компиляцией и типами, но не живыми ответами.
- Release-подпись и публикация в стор — за пределами репозитория.
- Все заявления «работает» в документации означают «компилируется и
  соответствует контракту», а не «проверено на устройстве».

---

Дальше: [архитектура](ARCHITECTURE.md) · [экраны](SCREENS.md) · [анимации](ANIMATIONS.md) · [API](API.md) · [модули](MODULES.md).
