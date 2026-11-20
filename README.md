# StudyDeck

**Know what is due. Do what matters.**

StudyDeck is a fully offline Android task manager built for students. It keeps
assignments, subjects, deadlines and reminders on the device — no account
server, no cloud sync, no network permission.

---

## Features

- **Local accounts** — register and sign in without a server. Passwords are
  hashed with PBKDF2-HMAC-SHA256 (120,000 iterations, per-user 16-byte salt).
- **Dashboard** — progress ring plus stat cards for pending, overdue, due today
  and completed work. Tap a card to jump straight into the filtered task list.
- **Tasks** — add, edit, complete and delete tasks with a subject, priority,
  due date, optional due time and an optional reminder.
- **Task list** — filter chips (All, Pending, Completed, High priority,
  Overdue), sorting (due date, priority, newest), grouping by due window
  (Overdue, Today, Tomorrow, This week, Later, Completed) and swipe-to-complete
  with undo.
- **Subjects** — colour-coded subjects that group work across the app.
- **Reminders** — scheduled with WorkManager and delivered as notifications;
  tapping a reminder opens that task.
- **Personalisation** — light/dark/system theme, six accent colours including
  Material You dynamic colour, a reduce-motion switch and a default reminder
  lead time.
- **Onboarding and profile** — first-run walkthrough, editable profile, and an
  in-app about screen.

## Tech stack

| Area | Choice |
| --- | --- |
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (Material 3, Compose BOM 2026.02.01) |
| Architecture | MVVM + repositories, manual DI via `AppContainer` |
| Persistence | Room 2.8.5 (SQLite) |
| Preferences | DataStore Preferences 1.3.0 |
| Background work | WorkManager 2.11.2 |
| Navigation | Navigation Compose 2.10.2 |
| Build | AGP 9.3.3, Kotlin KSP, Gradle wrapper |
| SDK | minSdk 31 (Android 12), compileSdk/targetSdk 37, Java 11 |

## Getting started

### Prerequisites

- Android Studio (latest stable) or the command-line Android SDK
- JDK 11 or newer
- An Android 12+ device or emulator (minSdk 31)

### Run it

1. Clone the repository.
2. Open the project in Android Studio and let it sync.
3. Press **Run**, or from a terminal:

```powershell
.\gradlew.bat :app:installDebug
```

Android Studio generates `local.properties` with your SDK path automatically.
It is git-ignored and must not be committed.

### Build and test

```powershell
.\gradlew.bat :app:assembleDebug      # debug APK
.\gradlew.bat :app:test               # JVM unit tests
.\gradlew.bat :app:compileDebugKotlin # compile check only
```

## Project structure

```
app/src/main/java/com/example/studenttaskmanager/
├── MainActivity.kt              # single activity host
├── StudyDeckApplication.kt      # creates the AppContainer
├── data/
│   ├── local/                   # Room database, DAOs
│   ├── model/                   # entities and enums
│   ├── prefs/                   # DataStore settings
│   ├── repository/              # repositories
│   └── security/                # password hashing
├── di/AppContainer.kt           # manual dependency injection
├── reminder/                    # WorkManager scheduling and notifications
├── ui/
│   ├── auth/  home/  tasks/     # feature screens and view models
│   ├── settings/  onboarding/  splash/
│   ├── common/  theme/  navigation/
│   └── AppViewModelProvider.kt  # view-model factory
└── util/                        # date and time helpers

app/src/test/                    # JVM unit tests
app/schemas/                     # exported Room schemas
```

## Data and privacy

StudyDeck requests only the `POST_NOTIFICATIONS` permission, and only to show
task reminders. All accounts, subjects and tasks live in a local Room database;
nothing is uploaded. Settings are stored in a DataStore preferences file.
Uninstalling the app deletes all data.

## Notes

- The `release` build type is currently signed with the debug key so that
  minified builds stay installable for local testing. Replace it with a real
  signing configuration before distributing the app.
- `app/schemas/` holds the exported Room schema so future migrations can be
  verified against it.

## License

Released under the MIT License — see [LICENSE](LICENSE).

Bundled fonts are licensed under the SIL Open Font License; their license texts
are in `app/src/main/assets/`.