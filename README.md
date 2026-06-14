# Weltreise App

Android App (Kotlin) für das Weltreise Spiel – AAU SE2 Gruppe 1.

## Tech Stack

- Kotlin + Android SDK
- WebSocket (STOMP / OkHttp)
- Gradle (Kotlin DSL)
- JUnit 4 + JaCoCo
- SonarCloud Quality Gates

## Setup

Android Studio öffnen → "Open" → dieses Verzeichnis auswählen.

```bash
# Build
./gradlew assembleDebug

# Tests
./gradlew testDebugUnitTest
```

## CI/CD

GitHub Actions führt bei jedem Push/PR auf develop automatisch aus:

- Build (debug)
- Unit Tests + JaCoCo Coverage Report
- SonarCloud Scan

## Branch-Workflow

- Feature-Branches: `feature/<beschreibung>`
- Commit-Convention: Conventional Commits
- Merges nur via Pull Request (kein Squash/Rebase)
- `develop` ist der aktive Entwicklungsbranch
- `main` wird nur beim Sprint Review mit `develop` gemergt
