# Android Device Testing Checklist

Run on a physical Android phone with Instagram installed and the Reel Blocker accessibility service enabled.

## Setup

- [ ] App installed and opened
- [ ] Accessibility service enabled (Home screen shows "Service active")
- [ ] Instagram logged in

## Tab covers

- [ ] Reels tab in the bottom nav is covered (black) and tapping it does nothing
- [ ] Explore/Search tab is covered and tapping it does nothing
- [ ] Create (+) is NOT covered and opens the camera/new post flow
- [ ] Home and Profile tabs are NOT covered and work normally

## Content bounce (safety net)

- [ ] Swiping right/left from Home into Reels shows the block overlay and bounces back
- [ ] Opening Explore via search shortcut shows the overlay and bounces back
- [ ] Opening a Reel from a shared deep link bounces out (Back, then Home after a few tries)

## Toggles

- [ ] Turning off "Block Reels" restores the Reels tab; Explore still blocked
- [ ] Turning off "Block Explore" restores the Explore tab; Reels still blocked
- [ ] Turning off master "Blocking enabled" removes all covers/overlays immediately
- [ ] Re-enabling restores blocking without restarting Instagram

## Other apps unaffected

- [ ] Open a different app (e.g. Chrome) — no overlays or covers appear
- [ ] Return to Instagram — covers reappear

## Resilience

- [ ] Force-stop Instagram and reopen — blocking still works
- [ ] Lock/unlock phone during Instagram use — covers reapply
- [ ] Disable the service in settings — Home screen shows "Service disabled" with a fix shortcut

## Notes / known gaps

- Inline Reels in the Home feed are intentionally not blocked.
- Non-English Instagram UI may need extra content-description strings in `InstagramDetector.kt`.
