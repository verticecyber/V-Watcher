# V-Watcher

V-Watcher is an Android application for local device health, behavioral monitoring,
and defensive inspection. The application is designed around on-device processing:
device observations remain local unless a future, explicitly documented feature says
otherwise.

## Project status

The repository is in active pre-release preparation for Google Play. The current
release scope, known gaps, and evidence are maintained in
[`docs/sot/V_WATCHER_SOT_UNIFIED.md`](docs/sot/V_WATCHER_SOT_UNIFIED.md).
Release-specific gates and submission tasks are indexed in
[`docs/release/PLAY_STORE_READINESS.md`](docs/release/PLAY_STORE_READINESS.md).

## Requirements

- Android Studio with an Android SDK for compile/target API 36
- JDK 11 or newer
- A connected Android device or emulator for runtime validation
- Git

## Build and test

Open the repository in Android Studio, allow Gradle to synchronize, and run:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

For a release build, configure signing through environment variables rather than
committing credentials:

```bash
export KEYSTORE_PATH=/absolute/path/to/upload-key.jks
export STORE_PASSWORD='your-store-password'
export KEY_PASSWORD='your-key-password'
./gradlew bundleRelease
```

The keystore and password values must remain outside the repository. See
[`docs/release/RELEASE_BUILD.md`](docs/release/RELEASE_BUILD.md) before producing
an artifact for Play Console.

## Repository layout

| Path | Purpose |
| --- | --- |
| `app/` | Android application source, resources, and tests |
| `docs/sot/` | Current source of truth and reconciled product evidence |
| `docs/release/` | Release, privacy, security, and Play Console readiness |
| `docs/reference/` | Environment and supporting reference material |
| `docs/evidence/validation/` | Machine-generated validation inputs and traces |
| `docs/evidence/historical/` | Superseded validation reports |
| `docs/archive/` | Historical plans and superseded research |
| `tools/validation/` | Reproducible validation utilities |

## Configuration

Copy `.env.example` only when a local integration requires it. Do not commit
`.env`, API keys, signing credentials, keystores, `local.properties`, APKs, or
Android App Bundles.

## Contributing

Read [`CONTRIBUTING.md`](CONTRIBUTING.md) before opening a change. Security issues
must follow [`SECURITY.md`](SECURITY.md).
