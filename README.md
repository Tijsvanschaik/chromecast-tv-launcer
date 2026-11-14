# Glass Home (chromecast-tv-launcer)

Sideloadable Android TV / Google TV home *candidate* — dark liquid glass over a calm nature plate.

**Application id:** `com.local.glasshome`  
**Stack:** Kotlin, Compose for TV (`androidx.tv.material3`)  
**v1:** Opens from the Apps row. Stock Google TV Home stays enabled.

## Modules

| Module | Path | Role |
| --- | --- | --- |
| `:app` | `app/` | Single home Activity + wiring |
| `:glass` | `glass/` | Theme, `GlassCard`, scrim, focus |
| `:apps` | `apps/` | Installed apps, favorites pins, launch, app info |
| `:background` | `background/` | Calm still nature plate |

## Build

```bash
./gradlew :app:assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## What v0.2 does

- Lists installed TV and phone-launcher apps. Must-keeps (YouTube, Netflix, and the rest) sort to the front when that label is installed.
- OK launches the app. Hold OK for App info or Pin / Unpin.
- First launch pins the installed must-keeps. Pin order is stored on the device.
- Calm still behind frosted cards. Stock Home still owns the Home button.

## Sideload

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Open **Glass Home** from the Apps row. Uninstall with `adb uninstall com.local.glasshome`. Do not disable stock Home.
