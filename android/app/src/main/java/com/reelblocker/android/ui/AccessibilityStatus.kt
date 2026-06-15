package com.reelblocker.android.ui

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import com.reelblocker.android.service.ReelBlockerAccessibilityService

/**
 * Reads whether our accessibility service is enabled in system settings. This is the source of truth
 * for onboarding, independent of whether the service process is currently bound.
 */
object AccessibilityStatus {

    fun isServiceEnabled(context: Context): Boolean {
        val expectedComponent =
            "${context.packageName}/${ReelBlockerAccessibilityService::class.java.name}"

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            if (colonSplitter.next().equals(expectedComponent, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
