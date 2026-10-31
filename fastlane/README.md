fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android test

```sh
[bundle exec] fastlane android test
```

Run the JVM unit tests (formatters + Audius mappers)

### android test_sources

```sh
[bundle exec] fastlane android test_sources
```

Compile unit-test and instrumentation-test sources

### android build_debug

```sh
[bundle exec] fastlane android build_debug
```

Build the debug APK

### android build_release

```sh
[bundle exec] fastlane android build_release
```

Build the release APK (unsigned unless a keystore is configured)

### android telegram

```sh
[bundle exec] fastlane android telegram
```

Send an APK to Telegram (skips when the bot secrets are absent)

### android ci

```sh
[bundle exec] fastlane android ci
```

Full CI pipeline: unit tests, test sources and debug APK

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
