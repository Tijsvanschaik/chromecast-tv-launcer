# Glass Home (chromecast-tv-launcer)

Sideloadable Android TV / Google TV home *candidate* — dark liquid glass over calm nature plates.

**Application id:** `com.local.glasshome`  
**Stack:** Kotlin, Compose for TV (`androidx.tv.material3`)  
**Home button:** Opens from the Apps row. Stock Google TV Home stays enabled.

## Modules

| Module | Path | Role |
| --- | --- | --- |
| `:app` | `app/` | Single home Activity, hero clock, quick settings, action sheet |
| `:glass` | `glass/` | Theme, `GlassCard`, `GlassPill`, scrim, focus |
| `:apps` | `apps/` | Installed apps, `AppsViewModel`, pins/hidden store, banner art, launch, app info |
| `:background` | `background/` | Rotating calm still plates |

## What v2 does

- Clock and date hero with Wi‑Fi, Bluetooth and Settings shortcuts.
- Favorites and Apps use each app's TV banner as the card face. The name fades in under the focused card.
- Must-keeps (YouTube, Netflix and the rest) sort to the front, matched by package id, then by exact label. First launch pins them.
- Hold OK for Pin/Unpin, Move (favorites: Left/Right, then OK), Hide/Unhide, App info. Hidden apps are listed behind a "Hidden apps" button.
- Pins and hidden apps are stored per package. They survive app updates and temporary disables and are removed only on uninstall.
- Four calm stills rotate every 3 hours. No live blur, no continuous animation.

## Build

```bash
./gradlew :apps:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

Release (R8 + resource shrinking, about 2 MB) is signed when these are set:

```bash
export GLASSHOME_KEYSTORE=/path/glasshome-release.jks
export GLASSHOME_KEYSTORE_PASSWORD=… GLASSHOME_KEY_ALIAS=glasshome GLASSHOME_KEY_PASSWORD=…
./gradlew :app:assembleRelease   # app/build/outputs/apk/release/app-release.apk
```

CI (`.github/workflows/android.yml`) runs tests, lint and a debug build on every PR. Pushing a `v*` tag builds the signed release and attaches `glasshome-<tag>.apk` to a GitHub Release. Required repo secrets: `GLASSHOME_KEYSTORE_BASE64`, `GLASSHOME_KEYSTORE_PASSWORD`, `GLASSHOME_KEY_ALIAS`, `GLASSHOME_KEY_PASSWORD`. Always use the same keystore, because a different key cannot update an installed build.

## Sideload

```bash
adb uninstall com.local.glasshome          # one time only, when switching from a debug build
adb install -r glasshome-v2.0.0.apk
```

Open **Glass Home** from the Apps row. Do not disable stock Home.
