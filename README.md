# Sona 🎵

Android-приложение для прослушивания музыки. Модульная архитектура, Kotlin, Gradle Kotlin DSL, Dagger, CI/CD.

## Структура

- `app/` — главный модуль
- `core/` — базовые модули (API музыки + Dagger)
- `feature/` — фича-модули
- `.github/workflows` — CI/CD
- `build.gradle.kts`
- `settings.gradle.kts`

## Сборка

```bash
git clone https://github.com/JamikKhidirov/Sona.git
cd Sona
./gradlew build          # Linux/macOS
gradlew.bat build        # Windows
./gradlew installDebug   # установка на устройство
