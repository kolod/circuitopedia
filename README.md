# Circuitopedia

A free, ad-free, open-source electronics and electrical engineering reference and calculator app
for Android. It has no ads, no tracking and no network access, and works fully offline.

The app shell is in place. Calculators and reference pages are added as separate modules, one
spec at a time.

## Building

Prerequisites:

- JDK 25. Gradle provisions it automatically if missing, see `gradle/gradle-daemon-jvm.properties`.
- Android SDK with platform `android-37`. Point `sdk.dir` in `local.properties` at it.

```sh
./gradlew :app:assembleDebug            # debug APK in app/build/outputs/apk/debug/
./gradlew test lint                     # JVM unit tests and Android Lint
./gradlew :app:connectedDebugAndroidTest  # UI tests, needs an emulator or device
```

## Adding a calculator or reference page

See [modules/README.md](modules/README.md). In short: create `modules/<name>/` and add one line
to `app/src/main/kotlin/io/github/kolod/circuitopedia/ModuleRegistry.kt`.

## Contributing

Development follows [Spec Kit](https://github.com/github/spec-kit). The project rules are in
[.specify/memory/constitution.md](.specify/memory/constitution.md), and the specs are in
[specs/](specs/).

## License

[GNU General Public License v3.0](LICENSE)
