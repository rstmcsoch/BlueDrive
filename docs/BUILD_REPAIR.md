# APK build investigation and repair

## Confirmed causes

1. The first Actions run failed in **Set up Android SDK**, not in the subsequent
   explicit package-install step. setup-android v3 requested `tools` by default;
   the remote SDK repository could not find that legacy package. The user applied
   `packages: platform-tools` on main in d705537. This branch merges that change
   and leaves the workflow byte-for-byte identical to main.
2. The next run passed SDK setup and installation, then failed to load
   `org.gradle.wrapper.GradleWrapperMain`. The repository contained the wrapper
   scripts and properties but not `gradle/wrapper/gradle-wrapper.jar`. The official
   Gradle v8.13.0 JAR was restored from gradle/gradle, matching the distribution
   version in the existing properties file. SHA-256:
   `81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f`.
3. Previous pushes were rejected because this branch also modified an Actions
   workflow and the GitHub integration lacks workflow-write permission. The SDK
   correction was already on main. Merging main and retaining its exact workflow
   removes that unnecessary diff. The branch has now pushed successfully without
   changing integration permissions.

## Additional issues found by inspection

- Glide runtime was declared twice (catalog 5.0.5 and literal 4.16.0), with a
  4.16.0 processor. Use a single 4.16.0 catalog version for both runtime/processor.
- Duplicate JUnit/AndroidX test dependencies had conflicting versions. Consolidate
  each dependency into one catalog-backed declaration.
- Unused Room, Telegram Bots SDK, SLF4J and explicit lifecycle/KTX declarations
  inflated the dependency graph. No application source imports Room, the SDK or
  SLF4J; the retained Telegram implementation uses HttpURLConnection/Android JSON.
  Remove these unused declarations, not the integration source. Required lifecycle
  dependencies still come from AndroidX Activity/AppCompat.
- RecyclerView/CardView are directly used but were only transitively supplied.
  Declare them explicitly.
- Manifest package is redundant with AGP namespace. Remove the redundant manifest
  attribute without changing namespace/application ID; explicitly keep the image
  viewer unexported.
- Guard refresh submission after executor shutdown on Activity destruction.
- Give the filter control wrap-content width inside its horizontally scrolling
  container instead of zero width with weight. Remove the non-accessible clear
  touch interception; retain the actual accessible Clear search button.
- Recover private JSON with AtomicFile.openRead after interrupted writes.
- Image preview now keeps a real loading state until Glide resolves the image,
  applies system-bar insets and does not delete user downloads when closed.
- Increase wrapper network timeout to 60 seconds, retaining HTTPS validation.

The dependency cleanups are inspection-based fixes, not claimed to be failures
observed from an Android compilation.

## Checks actually run

- `python3 tools/check_build_setup.py`: PASS.
- `python3 tools/check_resources.py`: PASS (including Java resource IDs).
- `python3 -m unittest discover -s tools/tests -v`: 5 tests PASS, including missing/
  corrupt wrapper, obsolete SDK package and mismatched Glide regression cases.
- Java syntax parsed for all 24 Java source/test files: PASS; not type checking.
- `git diff --check`: PASS.
- A temporary PyPI-packaged Java runtime was installed outside the repository.
  Wrapper starts and reaches its Gradle distribution downloader, proving the
  missing-main-class failure is resolved locally. Full `assembleDebug`, `test`
  and `lintDebug` each stop at `SSLHandshakeException` fetching
  `https://services.gradle.org/distributions/gradle-8.13-bin.zip`. That host is
  outside this sandbox's network allowlist. Downloading the GitHub distribution
  also redirects to a blocked release-assets host. Android SDK is unavailable.
  No APK, Gradle test success or lint success is claimed.
- Actions job metadata confirms the user's second run passed SDK setup/package
  installation and failed at Build debug APK. Workflow dispatch from the agent
  returned HTTP 403 `Resource not accessible by integration`; no CI run was started.

## Run the repaired build

After merging this PR, open Actions → Build Android APK → Run workflow → main.
Start a **new** run, not Re-run jobs on the old commit. Alternatively, before
merging, manually select `arena/e57727af-bluedrive` in the Run workflow branch
picker. The workflow remains the same known SDK-corrected workflow on main.

After a green build, download `BlueDrive-debug-APK` from Artifacts and extract
`app-debug.apk`. It is a debug-signed test APK. Do not uninstall an existing app
without backing up data if a signing mismatch prevents an update.

No production remote provider has been enabled. This repair does not change the
Telegram compliance gate or fabricate transfer capability. Further compiler,
resource linker, dependency-download or runtime errors must be evaluated from an
actual Android build/device run; the offline checks cannot exclude all of them.
