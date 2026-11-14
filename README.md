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
| `:apps` | `apps/` | Favorites + apps placeholders (real query in Phase 3) |
| `:background` | `background/` | Calm still nature plate |

## Build

```bash
./gradlew :app:assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## Sideload

See project docs (`docs/launcher/sideload.md` in the orchestration store). Do not disable stock Home for v1.
