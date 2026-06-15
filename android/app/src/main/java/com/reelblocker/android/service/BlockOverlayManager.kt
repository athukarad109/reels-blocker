package com.reelblocker.android.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView

/**
 * Owns every window this service draws on top of Instagram.
 *
 * Two kinds of overlays:
 *  - A single full-screen [blockView] shown while a blocked surface (Reels/Explore) is on screen.
 *  - Small [tabCovers] placed over the Reels/Explore bottom-nav buttons so they look hidden and
 *    swallow taps.
 *
 * All windows use TYPE_ACCESSIBILITY_OVERLAY, which an accessibility service may add without the
 * SYSTEM_ALERT_WINDOW permission.
 */
class BlockOverlayManager(private val service: AccessibilityService) {

    private val windowManager =
        service.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager

    private var blockView: View? = null
    private val tabCovers = mutableMapOf<TabId, View>()

    enum class TabId { REELS, EXPLORE }

    fun showFullBlock(message: String) {
        val existing = blockView
        if (existing != null) {
            (existing as? FrameLayout)?.let { frame ->
                (frame.getChildAt(0) as? TextView)?.text = message
            }
            return
        }

        val view = FrameLayout(service).apply {
            setBackgroundColor(BLOCK_BACKGROUND)
            isClickable = true
            isFocusable = false
            addView(
                TextView(service).apply {
                    text = message
                    setTextColor(Color.WHITE)
                    textSize = 18f
                    val pad = (24 * service.resources.displayMetrics.density).toInt()
                    setPadding(pad, pad, pad, pad)
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        runCatching { windowManager.addView(view, params) }
            .onSuccess { blockView = view }
    }

    fun hideFullBlock() {
        val view = blockView ?: return
        runCatching { windowManager.removeView(view) }
        blockView = null
    }

    fun coverTab(tab: TabId, bounds: Rect) {
        val params = WindowManager.LayoutParams(
            bounds.width(),
            bounds.height(),
            bounds.left,
            bounds.top,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.OPAQUE
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        val existing = tabCovers[tab]
        if (existing != null) {
            runCatching { windowManager.updateViewLayout(existing, params) }
            return
        }

        val cover = View(service).apply {
            setBackgroundColor(TAB_COVER_COLOR)
            isClickable = true
            isFocusable = false
        }

        runCatching { windowManager.addView(cover, params) }
            .onSuccess { tabCovers[tab] = cover }
    }

    fun removeTabCover(tab: TabId) {
        val view = tabCovers.remove(tab) ?: return
        runCatching { windowManager.removeView(view) }
    }

    fun removeAllTabCovers() {
        TabId.entries.forEach { removeTabCover(it) }
    }

    fun tearDown() {
        hideFullBlock()
        removeAllTabCovers()
    }

    private companion object {
        const val BLOCK_BACKGROUND = 0xF21A1A1A.toInt()
        const val TAB_COVER_COLOR = Color.BLACK
    }
}
