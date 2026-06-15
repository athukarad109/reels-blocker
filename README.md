# Reel Blocker

Two implementations live in this repo:

- **Android** ([`android/`](android/README.md)) — the recommended solution. Uses an AccessibilityService to actually hide Instagram's Reels and Explore tabs and bounce you out of those surfaces, while keeping Create usable.
- **iOS** (this directory) — a weaker gatekeeper. iOS does not allow hiding tabs inside Instagram, so it shields the app and time-boxes access to a create session.

---

# Reel Blocker (iOS)

Personal-use iOS gatekeeper that shields Instagram by default and unlocks it only for timed **Create** sessions via Apple's Screen Time API.

## What it does

- Blocks Instagram when opened from the home screen (custom shield screen)
- Unlocks Instagram only when you start a **Create Post** session from Reel Blocker
- Opens Instagram's create flow using `instagram://camera`
- Re-locks automatically when the session timer ends, or when you tap **End Session**
- Foreground expiry check acts as a fallback if DeviceActivity callbacks are delayed

## What it cannot do

iOS does not allow third-party apps to hide Reels or Explore tabs inside Instagram. During an active session, those tabs remain visible. Use shorter session durations in Settings to reduce browsing time.

## Requirements

- Mac with Xcode 15+
- Physical iPhone running iOS 17+ (Screen Time APIs do not work in Simulator)
- Apple Developer account (paid) for on-device signing
- Instagram installed on the same iPhone

## Open and build on Mac

1. Copy or clone this repo to your Mac.
2. Open `ReelBlocker.xcodeproj` in Xcode.
3. Select your **Development Team** for all four targets:
   - ReelBlocker
   - ShieldConfiguration
   - ShieldAction
   - DeviceActivityMonitor
4. For each target, in **Signing & Capabilities**:
   - Add **Family Controls (Development)** if not already present
   - Add **App Groups** → `group.com.reelblocker.shared`
5. In [Apple Developer Portal](https://developer.apple.com/account/resources/identifiers/list), register App IDs for all four bundle IDs and enable Family Controls + App Groups on each.
6. Connect your iPhone, select it as the run destination, and build (**Cmd+R**).

### Bundle IDs

| Target | Bundle ID |
|--------|-----------|
| Main app | `com.reelblocker.app` |
| Shield Configuration | `com.reelblocker.app.ShieldConfiguration` |
| Shield Action | `com.reelblocker.app.ShieldAction` |
| Device Activity Monitor | `com.reelblocker.app.DeviceActivityMonitor` |

## First-run setup on iPhone

1. Launch Reel Blocker.
2. Tap **Allow Screen Time Access** and approve the system prompt.
3. Tap **Select Instagram** and pick Instagram in the system picker.
4. Tap **Finish Setup**.
5. (Recommended) Remove Instagram from your home screen and use Reel Blocker as your entry point.

## Daily use

1. Open Reel Blocker → **Create Post**.
2. Instagram opens to the create/camera flow.
3. When finished, return to Reel Blocker and tap **End Session**, or wait for the timer.
4. If you try to open Instagram directly while locked, the shield appears. Tap **Request unlock**, then open Reel Blocker to start a session.

## Project structure

```
ReelBlocker/              Main SwiftUI app
Shared/                   Code shared with extensions (App Group storage, shield logic)
ShieldConfiguration/      Custom shield UI extension
ShieldAction/             Shield button handler (unlock handoff)
DeviceActivityMonitor/    Session timer re-lock extension
```

## Settings

- **Block Instagram** — master toggle
- **Session duration** — 3, 5, 10, or 15 minutes (default 10)
- **Re-select Instagram** — if the stored app token becomes invalid

## Testing

See [TESTING.md](TESTING.md) for the device test checklist.

## License

Personal project — use at your own discretion.
