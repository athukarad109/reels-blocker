package com.reelblocker.android

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelblocker.android.data.BlockerPreferences
import com.reelblocker.android.ui.AccessibilityStatus
import com.reelblocker.android.ui.HomeScreen
import com.reelblocker.android.ui.OnboardingScreen
import com.reelblocker.android.ui.theme.ReelBlockerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferences: BlockerPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = BlockerPreferences(applicationContext)

        setContent {
            ReelBlockerTheme {
                val scope = rememberCoroutineScope()

                var serviceEnabled by remember { mutableStateOf(AccessibilityStatus.isServiceEnabled(this)) }

                // Re-check the system setting whenever the activity resumes (user returns from Settings).
                androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
                    serviceEnabled = AccessibilityStatus.isServiceEnabled(this@MainActivity)
                    onPauseOrDispose { }
                }

                val settings by preferences.settings.collectAsStateWithLifecycle(
                    initialValue = BlockerPreferences.Settings(
                        enabled = true,
                        blockReels = true,
                        blockExplore = true
                    )
                )

                if (!serviceEnabled) {
                    OnboardingScreen(onOpenAccessibilitySettings = ::openAccessibilitySettings)
                } else {
                    HomeScreen(
                        serviceEnabled = serviceEnabled,
                        masterEnabled = settings.enabled,
                        blockReels = settings.blockReels,
                        blockExplore = settings.blockExplore,
                        onMasterChanged = { scope.launch { preferences.setEnabled(it) } },
                        onBlockReelsChanged = { scope.launch { preferences.setBlockReels(it) } },
                        onBlockExploreChanged = { scope.launch { preferences.setBlockExplore(it) } },
                        onFixService = ::openAccessibilitySettings
                    )
                }
            }
        }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
}
