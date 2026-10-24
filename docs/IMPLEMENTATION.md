# BlueDrive implementation checklist
- [x] Inspect native Java/XML architecture, layouts, models, legacy index and networking.
- [x] Review supplied visual references: compact library, category cards, honest storage summary.
- [x] Gate Telegram production operations pending applicable permission.
- [ ] Build adaptive native dashboard/library, settings, insights and transfer destination.
- [ ] Preserve index and offer SAF import; do not overwrite external legacy data.
- [ ] Persist appearance, locale, sort and view preferences.
- [ ] Validate resources, build, tests and lint; record environment blockers.

Compliance review (2026-10-24): https://telegram.org/tos/bot-developers sections
5.2(e) explicitly names cloud storage sites as divergent external interfaces;
5.2(f) prohibits proxy credentials to circumvent bans/moderation. User-owned bots
are not evidence of permission. No applicable approval found. Remote operations
remain disabled, source retained. No replacement cloud provider selected.
https://core.telegram.org/bots/api and /bots/faq returned HTTP 403 during review;
50 MB multipart / 20 MB getFile limits supplied in the task are NOT independently
reverified. No remote capability or quota is advertised by this build.

## Delivered source
- Shared native Activity destination host (`dashboard.xml`, Java screen components),
  Home / Files / Transfers / More; category deep links reuse the same host.
- Honest indexed counts/sizes, six categories, recent files, size-distribution bars
  and largest records; no quota percentages or invented content.
- Local SAF document references with persisted read grants, PhotoView/Glide image
  viewing, system media/document opening, explicit sharing, details, reversible
  local-only record hiding. No user file or remote object is deleted.
- Search, clear, seven sort modes, persisted list/grid preference, adaptive grid
  columns and phone/tablet category columns; back returns to Home.
- MaterialComponents DayNight resource hierarchy, persisted system/light/dark mode;
  English/Arabic/French redesign resources, RTL-compatible platform layouts.
- Keystore AES-GCM migration of legacy credentials; plaintext removed only after
  encrypted persistence succeeds. Failure retains original values for recovery.
  Credentials are not displayed; explicit forgetting retains file data. Backup
  disabled because device-bound Keystore ciphertext is not portable.
- `StorageProvider` callback/progress/cancellation contract plus explicit unavailable
  implementation. All four retained Telegram public operation entry points fail
  closed before making network requests. Legacy Activity implementations retained
  as unregistered source, not exposed through navigation.
- No all-files/read/write permission request. Inaccessible legacy indexes require
  explicit SAF import. Originals remain untouched, an exact private backup is saved
  before import; six/eight-field parsing, ID deduplication, malformed-line skipping.
  New local references use an atomic private JSON sidecar, not a destructive rewrite
  of the old text file. Delimiter-ambiguous records are not guessed or rewritten.

## Validation actually performed
- `python3 tools/check_resources.py`: PASS (XML parsing, Java resource names,
  matching new English/French/Arabic keys).
- Java syntax parsed with javalang for all source/test Java files: PASS.
- `git diff --check`: PASS.
- `./gradlew assembleDebug`, `./gradlew test`, `./gradlew lintDebug`: all STOPPED
  before Gradle execution: JAVA_HOME unset and no `java` executable. No Android SDK
  or emulator found either. Wrapper executable permission repaired.
- Added JUnit cases for legacy records, remote IDs, malformed/ambiguous input,
  deduplication, categories, sort ordering and fail-closed policy. NOT EXECUTED.

## Incomplete / needs Android validation
This is not a verified release build. No APK, emulator or device visual, font-scale,
rotation, keyboard, accessibility, locale-recreation, theme or intent tests ran.
The shared navigation currently remains a bottom bar on tablets, not a rail.
Transfer destination is a truthful empty/unavailable state, not an operational
queue or mock. Retry, foreground transfer execution and persisted transfer history
are deferred with real provider integration. No approved remote provider, remote
upload/download/delete or remote rename is enabled. Existing network source and
callbacks are retained but cannot establish production compatibility or compliance.
No real-device credential or index migration was run. Unsupported/ambiguous index
lines remain recoverable in the source and backup, but are not displayed.
Local additions are references, not copied backups; access can be revoked by the
originating document provider. The old public download directory is not changed;
a user may add a file from it through the system picker. About/privacy/help and
insights currently use native dialogs, not dedicated Activities. A complete
release QA pass and approved provider implementation remain necessary.
