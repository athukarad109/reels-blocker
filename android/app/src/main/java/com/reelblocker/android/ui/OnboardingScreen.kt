package com.reelblocker.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onOpenAccessibilitySettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Reel Blocker",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Block Instagram's Reels and Explore tabs while keeping Create and the rest of the app usable.",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "How to enable",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("1. Tap the button below to open Accessibility settings.")
                Text("2. Find \"Reel Blocker\" under Installed/Downloaded services.")
                Text("3. Turn it on and confirm the permission dialog.")
                Text("4. Return here \u2014 the app continues automatically.")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Why accessibility access",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Reel Blocker uses Android's accessibility service to detect when Reels or Explore is on screen and to draw a cover over those tabs. It reads only Instagram's screen content for this purpose and does not collect, store, or share any data."
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onOpenAccessibilitySettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Accessibility Settings")
        }
    }
}
