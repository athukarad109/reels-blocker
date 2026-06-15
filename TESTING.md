# Device Testing Checklist

Run these tests on a **physical iPhone** after building from Xcode. Screen Time APIs are not available in Simulator.

## Prerequisites

- [ ] Reel Blocker installed via Xcode (development signing)
- [ ] Screen Time permission granted
- [ ] Instagram selected during onboarding
- [ ] Instagram installed on device

## Test 1: Shield blocks direct access

1. Ensure blocker is enabled in Settings.
2. Ensure no active session (home screen shows "Instagram locked").
3. Tap Instagram on the home screen.

**Expected:**
- [ ] Custom shield appears ("Instagram is locked")
- [ ] Instagram does not open

## Test 2: Create session opens Instagram

1. Open Reel Blocker.
2. Tap **Create Post**.

**Expected:**
- [ ] Status changes to "Create session active"
- [ ] Countdown timer appears
- [ ] Instagram opens (ideally to create/camera flow)

## Test 3: Manual end session re-locks

1. With an active session, return to Reel Blocker.
2. Tap **End Session**.
3. Try opening Instagram from home screen.

**Expected:**
- [ ] Status returns to "Instagram locked"
- [ ] Shield blocks Instagram again

## Test 4: Timer re-lock

1. Set session duration to **3 minutes** in Settings.
2. Start a create session.
3. Wait for timer to expire (keep Reel Blocker in background).

**Expected:**
- [ ] Notification appears when session ends (if notifications allowed)
- [ ] Opening Reel Blocker shows "Instagram locked"
- [ ] Instagram is shielded again when launched from home screen

**Fallback check:** Open Reel Blocker after expiry even if DeviceActivity extension did not fire.

**Expected:**
- [ ] Foreground expiry check re-applies shield on app open

## Test 5: Unlock request handoff

1. Ensure Instagram is locked.
2. Tap Instagram on home screen.
3. On shield, tap **Request unlock**.
4. Open Reel Blocker.

**Expected:**
- [ ] Alert offers to start a create session

## Test 6: Stale session recovery

1. Start a session.
2. Force-quit Reel Blocker.
3. Wait for session to expire.
4. Re-open Reel Blocker.

**Expected:**
- [ ] App detects expired session
- [ ] Instagram is re-shielded

## Test 7: Blocker toggle

1. Disable **Block Instagram** in Settings.
2. Open Instagram from home screen.

**Expected:**
- [ ] Instagram opens without shield

3. Re-enable blocker.

**Expected:**
- [ ] Shield applies again when no active session

## Test 8: Persistence after reboot

1. Enable blocker with no active session.
2. Restart iPhone.
3. Try opening Instagram.

**Expected:**
- [ ] Shield still blocks Instagram (ManagedSettings persists)

## Known platform limitations

- DeviceActivityMonitor callbacks can be delayed or missed; foreground expiry in the main app is the safety net.
- Reels/Explore tabs remain visible during active sessions.
- `instagram://camera` behavior may change with Instagram app updates.

## Debug tips

- Attach debugger to extension: **Debug → Attach to Process by PID or Name** → `DeviceActivityMonitor`
- Verify App Group is enabled on all four targets with identical ID: `group.com.reelblocker.shared`
- Confirm Family Controls (Development) capability on all four targets
