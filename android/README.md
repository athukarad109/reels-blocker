# Reel Blocker (Android)

A sideloadable Android app that blocks Instagram's **Reels** and **Explore** tabs while keeping **Create**, Home, Profile, and DMs fully usable.

It uses an Android **AccessibilityService** to:

1. Cover the Reels and Explore buttons in Instagram's bottom navigation (they appear hidden and taps are swallowed).
2. Detect when the Reels viewer or Explore grid is on screen anyway (swipe, deep link, etc.), drop a full-screen block overlay, and bounce you back with Back / Home.

This is the approach that genuinely works on the platform, unlike the iOS version in this repo.

## Requirements

- An Android phone running Android 8.0 (API 26) or newer
- Instagram installed on the phone
- A way to build the APK (see options below) — Android Studio is NOT required

## A note on the Gradle wrapper jar

This repo ships the wrapper scripts and config but not the binary `gradle/wrapper/gradle-wrapper.jar` (binaries are not committed here).

- If you build in the **cloud (Option A below)**, you do not need to do anything — the workflow installs Gradle directly.
- If you build **locally (Option B below)**, run `gradle wrapper --gradle-version 8.9` once inside `android/`. After that, `gradlew.bat` / `gradlew` work normally.

## Build a debug APK

You do NOT need Android Studio. Pick one of these.

### Option A: Cloud build with GitHub Actions (no local tools at all)

A workflow at [`.github/workflows/android-build.yml`](../.github/workflows/android-build.yml) builds the APK on GitHub's servers.

1. Push this repo to GitHub.
2. Go to the repo's **Actions** tab. The "Build Android APK" workflow runs on every push that touches `android/`, or you can trigger it manually with **Run workflow**.
3. When it finishes, open the run and download the **reel-blocker-debug-apk** artifact (a zip containing `app-debug.apk`).
4. Send that APK to your phone (and friends) and install it.

This path also generates everything for you — no JDK, no Gradle, no SDK, no wrapper jar needed locally.

### Option B: Local command line (no Android Studio, but tools installed once)

Install three things, then build from a terminal:

1. **JDK 17** — e.g. `winget install EclipseAdoptium.Temurin.17.JDK` (Windows) or any Temurin 17.
2. **Gradle 8.9** — e.g. `winget install Gradle.Gradle`, or via [SDKMAN](https://sdkman.io/) / Chocolatey (`choco install gradle`).
3. **Android SDK command-line tools** (no Studio): download "Command line tools only" from the [Android developer site](https://developer.android.com/studio#command-line-tools-only), unzip, then:

```bash
# Point to where you put the SDK
setx ANDROID_HOME "%USERPROFILE%\Android\sdk"        # Windows (new terminal after)
# export ANDROID_HOME="$HOME/Android/sdk"            # macOS/Linux

sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
sdkmanager --licenses
```

Then create the wrapper once and build:

```bash
cd android
gradle wrapper --gradle-version 8.9
./gradlew.bat assembleDebug    # Windows
./gradlew assembleDebug        # macOS/Linux
```

The APK is written to:

```
android/app/build/outputs/apk/debug/app-debug.apk
```

## Install

- Via Android Studio: connect the phone (USB debugging on) and press Run.
- Via APK: copy `app-debug.apk` to the phone and open it. You may need to allow "Install unknown apps" for your file manager or browser.

## Enable the service

1. Open Reel Blocker.
2. Tap **Open Accessibility Settings**.
3. Find **Reel Blocker** under installed/downloaded services, turn it on, and accept the dialog.
4. Return to the app. It switches to the control screen automatically.

## Controls

- **Blocking enabled** — master switch
- **Block Reels** — hide the Reels tab and bounce out of the Reels viewer
- **Block Explore** — hide the Search/Explore tab and bounce out of Explore
- **Service status** — shows whether the accessibility service is active, with a shortcut to re-enable it

## Sharing with friends

Send them `app-debug.apk`. They install it, enable "install from unknown sources" if prompted, and turn on the accessibility service as above. No Play Store account or developer setup needed.

## How detection works

All Instagram-specific selectors live in
[`InstagramDetector.kt`](app/src/main/java/com/reelblocker/android/service/InstagramDetector.kt):

- Reels viewer: resource id suffix `clips_viewer_view_pager` (and a couple of fallbacks), required to be `isVisibleToUser` with non-zero bounds to avoid false positives.
- Explore: explore/search grid resource ids, or a search bar plus a results grid.
- Bottom-nav buttons: located by content description (`Reels`, `Search and explore`).

If an Instagram update breaks blocking, update the id/description lists in that one file.

## Limitations

- Content-description matching targets the English UI; other languages may need added strings.
- Reels embedded inline in the Home feed are part of Home and are not blocked (only the Reels tab/viewer and Explore are).
- If Android kills the service under aggressive battery optimization, re-enable it (the app surfaces status). Consider excluding Reel Blocker from battery optimization.

## Testing checklist

See [TESTING.md](TESTING.md).
