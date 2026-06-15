package com.reelblocker.android.service

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.reelblocker.android.util.NodeExtensions.boundsOrNull
import com.reelblocker.android.util.NodeExtensions.descContains
import com.reelblocker.android.util.NodeExtensions.descEquals
import com.reelblocker.android.util.NodeExtensions.findFirst
import com.reelblocker.android.util.NodeExtensions.isTrulyVisible
import com.reelblocker.android.util.NodeExtensions.viewIdSuffix

/**
 * Inspects Instagram's current window content to decide whether the Reels viewer or the Explore /
 * Search surface is on screen, and to locate the bottom-navigation buttons so they can be covered.
 *
 * All Instagram-specific selectors live here so they are easy to update when Instagram ships UI
 * changes. Matching is intentionally permissive (resource-id suffix OR content-description).
 */
object InstagramDetector {

    const val INSTAGRAM_PACKAGE = "com.instagram.android"

    // Resource id suffixes (most stable signal we have).
    private val REELS_VIEW_IDS = listOf(
        "clips_viewer_view_pager",
        "clips_viewer_root",
        "clips_video_container"
    )
    private val EXPLORE_VIEW_IDS = listOf(
        "search_tab_grid",
        "explore_grid",
        "recycler_view_explore"
    )

    // Content descriptions used on the bottom navigation tab buttons (English locale).
    private val REELS_TAB_DESCS = listOf("Reels")
    private val EXPLORE_TAB_DESCS = listOf("Search and explore", "Explore", "Search")

    data class Result(
        val isReels: Boolean,
        val isExplore: Boolean,
        val reelsTabBounds: Rect?,
        val exploreTabBounds: Rect?
    ) {
        val isBlockedSurface: Boolean get() = isReels || isExplore
    }

    fun analyze(root: AccessibilityNodeInfo?): Result {
        if (root == null) {
            return Result(false, false, null, null)
        }

        val isReels = detectReels(root)
        val isExplore = detectExplore(root)
        val reelsTabBounds = findTabBounds(root, REELS_TAB_DESCS)
        val exploreTabBounds = findTabBounds(root, EXPLORE_TAB_DESCS)

        return Result(
            isReels = isReels,
            isExplore = isExplore,
            reelsTabBounds = reelsTabBounds,
            exploreTabBounds = exploreTabBounds
        )
    }

    private fun detectReels(root: AccessibilityNodeInfo): Boolean {
        val byId = root.findFirst { node ->
            val suffix = node.viewIdSuffix()
            suffix != null && REELS_VIEW_IDS.contains(suffix) && node.isTrulyVisible()
        }
        if (byId != null) return true

        // Fallback: a fullscreen reels viewer typically exposes the "Reels" tab as selected and shows
        // like/comment controls. We keep this conservative to avoid blocking the Home feed.
        return false
    }

    private fun detectExplore(root: AccessibilityNodeInfo): Boolean {
        val byId = root.findFirst { node ->
            val suffix = node.viewIdSuffix()
            suffix != null && EXPLORE_VIEW_IDS.contains(suffix) && node.isTrulyVisible()
        }
        if (byId != null) return true

        // Fallback: the Explore landing shows a search entry point combined with a results grid.
        val hasSearchBar = root.findFirst { node ->
            node.isTrulyVisible() && (
                node.descContains("Search") ||
                    node.viewIdSuffix()?.contains("search") == true
                )
        } != null
        val hasResultsGrid = root.findFirst { node ->
            node.isTrulyVisible() &&
                node.className?.toString()?.contains("RecyclerView") == true &&
                (node.viewIdSuffix()?.contains("explore") == true ||
                    node.viewIdSuffix()?.contains("search") == true)
        } != null

        return hasSearchBar && hasResultsGrid
    }

    private fun findTabBounds(root: AccessibilityNodeInfo, descs: List<String>): Rect? {
        val node = root.findFirst { candidate ->
            candidate.isTrulyVisible() &&
                candidate.isClickable() &&
                (candidate.descEquals(*descs.toTypedArray()) || candidate.descContains(*descs.toTypedArray()))
        } ?: return null
        return node.boundsOrNull()
    }

    private fun AccessibilityNodeInfo.isClickable(): Boolean = isClickable || isFocusable
}
