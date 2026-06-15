package com.reelblocker.android.service

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.reelblocker.android.R
import com.reelblocker.android.data.BlockerPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Watches Instagram and enforces the blocking rules:
 *  - Cover the Reels/Explore bottom-nav buttons so they appear hidden.
 *  - If a blocked surface (Reels viewer / Explore grid) becomes visible anyway, drop a full-screen
 *    overlay and bounce the user back with Back, escalating to Home if Back does not escape.
 *
 * The Create tab and all other Instagram screens are left untouched.
 */
class ReelBlockerAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var overlays: BlockOverlayManager
    private lateinit var preferences: BlockerPreferences

    @Volatile
    private var settings = BlockerPreferences.Settings(enabled = true, blockReels = true, blockExplore = true)

    private var lastBounceAt = 0L
    private var consecutiveBounces = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlays = BlockOverlayManager(this)
        preferences = BlockerPreferences(applicationContext)
        _isRunning.value = true

        scope.launch {
            preferences.settings.collect { newSettings ->
                settings = newSettings
                if (!newSettings.enabled) {
                    overlays.tearDown()
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.packageName != InstagramDetector.INSTAGRAM_PACKAGE) {
            // Left Instagram: clear everything so other apps are never covered.
            overlays.tearDown()
            consecutiveBounces = 0
            return
        }
        if (!settings.enabled) {
            overlays.tearDown()
            return
        }

        val root = rootInActiveWindow ?: return
        val result = InstagramDetector.analyze(root)

        handleTabCovers(result)
        handleBlockedSurface(result)
    }

    private fun handleTabCovers(result: InstagramDetector.Result) {
        val reelsBounds = result.reelsTabBounds
        if (settings.blockReels && reelsBounds != null) {
            overlays.coverTab(BlockOverlayManager.TabId.REELS, reelsBounds)
        } else {
            overlays.removeTabCover(BlockOverlayManager.TabId.REELS)
        }

        val exploreBounds = result.exploreTabBounds
        if (settings.blockExplore && exploreBounds != null) {
            overlays.coverTab(BlockOverlayManager.TabId.EXPLORE, exploreBounds)
        } else {
            overlays.removeTabCover(BlockOverlayManager.TabId.EXPLORE)
        }
    }

    private fun handleBlockedSurface(result: InstagramDetector.Result) {
        val reelsBlocked = settings.blockReels && result.isReels
        val exploreBlocked = settings.blockExplore && result.isExplore

        if (!reelsBlocked && !exploreBlocked) {
            overlays.hideFullBlock()
            consecutiveBounces = 0
            return
        }

        val message = if (reelsBlocked) {
            getString(R.string.block_overlay_reels)
        } else {
            getString(R.string.block_overlay_explore)
        }
        overlays.showFullBlock(message)
        bounceAway()
    }

    /**
     * Sends the user back out of the blocked surface. Back usually returns to the previous tab; if we
     * keep landing on a blocked surface (e.g. a deep-linked Reel with no back stack), escalate to Home.
     */
    private fun bounceAway() {
        val now = SystemClock.uptimeMillis()
        if (now - lastBounceAt < BOUNCE_DEBOUNCE_MS) return
        lastBounceAt = now

        consecutiveBounces++
        if (consecutiveBounces >= MAX_BACK_BEFORE_HOME) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            consecutiveBounces = 0
            overlays.hideFullBlock()
        } else {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    override fun onInterrupt() {
        overlays.tearDown()
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        _isRunning.value = false
        if (::overlays.isInitialized) overlays.tearDown()
        scope.cancel()
        return super.onUnbind(intent)
    }

    companion object {
        private const val BOUNCE_DEBOUNCE_MS = 400L
        private const val MAX_BACK_BEFORE_HOME = 3

        private val _isRunning = MutableStateFlow(false)

        /** Observed by the UI to show live service status. */
        val isRunning = _isRunning.asStateFlow()
    }
}
