# Contributing to V-Watcher

## Before changing code

1. Read the current source of truth in `docs/sot/`.
2. Check the release impact in `docs/release/`.
3. Keep product behavior changes separate from documentation or evidence changes.
4. Never add credentials, signing keys, generated release artifacts, or local
   machine configuration to Git.

## Development workflow

Use a focused branch and a Conventional Commit message, for example:

```text
feat(telemetry): add battery health observation
fix(ui): preserve exam state after rotation
docs(release): clarify signing prerequisites
```

Before submitting a change, run the smallest relevant checks:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

If the Android SDK or another required tool is unavailable, report that limitation
instead of silently skipping the check.

## Documentation and evidence

Current claims belong in `docs/sot/`. Release decisions belong in `docs/release/`.
Historical plans and superseded evidence belong under `docs/archive/` or
`docs/evidence/historical/`. Update links when moving a document and preserve the
original meaning of evidence.
