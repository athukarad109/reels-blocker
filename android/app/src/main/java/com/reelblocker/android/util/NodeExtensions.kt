package com.reelblocker.android.util

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Helpers for traversing and inspecting the Instagram accessibility node tree.
 *
 * Instagram obfuscates and frequently changes its view hierarchy, so detection relies on a mix of
 * resource-id suffixes and content descriptions. Keep matching logic permissive and centralized here.
 */
object NodeExtensions {

    /** Returns the on-screen bounds of a node, or null if it has no usable size. */
    fun AccessibilityNodeInfo.boundsOrNull(): Rect? {
        val rect = Rect()
        getBoundsInScreen(rect)
        return if (rect.width() > 0 && rect.height() > 0) rect else null
    }

    /**
     * A node only counts as "really shown" when it is reported visible to the user AND occupies a
     * non-zero area. Instagram leaves some pager nodes attached with zero size (see Scrolless PR #69),
     * which would otherwise cause false positives.
     */
    fun AccessibilityNodeInfo.isTrulyVisible(): Boolean {
        return isVisibleToUser && boundsOrNull() != null
    }

    /** The resource id without the package prefix, e.g. "clips_viewer_view_pager". */
    fun AccessibilityNodeInfo.viewIdSuffix(): String? {
        val id = viewIdResourceName ?: return null
        val slash = id.indexOf('/')
        return if (slash >= 0) id.substring(slash + 1) else id
    }

    /**
     * Depth-first search returning the first node matching [predicate]. The caller owns the returned
     * node reference but, in practice, nodes from the event tree are recycled by the framework; we
     * intentionally avoid manual recycle() to stay compatible with modern API levels.
     */
    fun AccessibilityNodeInfo.findFirst(
        maxDepth: Int = 60,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        if (predicate(this)) return this
        if (maxDepth <= 0) return null
        for (i in 0 until childCount) {
            val child = getChild(i) ?: continue
            val match = child.findFirst(maxDepth - 1, predicate)
            if (match != null) return match
        }
        return null
    }

    /** Returns true if any node in the subtree matches [predicate]. */
    fun AccessibilityNodeInfo.anyMatch(
        maxDepth: Int = 60,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): Boolean = findFirst(maxDepth, predicate) != null

    /** Case-insensitive containment check against a node's content description. */
    fun AccessibilityNodeInfo.descContains(vararg needles: String): Boolean {
        val desc = contentDescription?.toString()?.lowercase() ?: return false
        return needles.any { desc.contains(it.lowercase()) }
    }

    /** Case-insensitive exact-ish match against a node's content description (trimmed). */
    fun AccessibilityNodeInfo.descEquals(vararg candidates: String): Boolean {
        val desc = contentDescription?.toString()?.trim()?.lowercase() ?: return false
        return candidates.any { desc == it.lowercase() }
    }
}
