package com.reelblocker.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    serviceEnabled: Boolean,
    masterEnabled: Boolean,
    blockReels: Boolean,
    blockExplore: Boolean,
    onMasterChanged: (Boolean) -> Unit,
    onBlockReelsChanged: (Boolean) -> Unit,
    onBlockExploreChanged: (Boolean) -> Unit,
    onFixService: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Reel Blocker",
            style = MaterialTheme.typography.headlineMedium
        )

        StatusCard(serviceEnabled = serviceEnabled, onFixService = onFixService)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ToggleRow(
                    title = "Blocking enabled",
                    subtitle = "Master switch for all blocking",
                    checked = masterEnabled,
                    onCheckedChange = onMasterChanged
                )
                Divider()
                ToggleRow(
                    title = "Block Reels",
                    subtitle = "Hide the Reels tab and bounce out of the Reels viewer",
                    checked = blockReels,
                    enabled = masterEnabled,
                    onCheckedChange = onBlockReelsChanged
                )
                Divider()
                ToggleRow(
                    title = "Block Explore",
                    subtitle = "Hide the Search/Explore tab and bounce out of Explore",
                    checked = blockExplore,
                    enabled = masterEnabled,
                    onCheckedChange = onBlockExploreChanged
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Good to know",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Create, Home, Profile, and DMs stay fully usable.")
                Text("Instagram updates can occasionally change their UI; if blocking stops working, update the app.")
            }
        }
    }
}

@Composable
private fun StatusCard(serviceEnabled: Boolean, onFixService: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (serviceEnabled) "Service active" else "Service disabled",
                style = MaterialTheme.typography.titleMedium,
                color = if (serviceEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            if (serviceEnabled) {
                Text("Reel Blocker is watching Instagram.")
            } else {
                Text("The accessibility service is off. Re-enable it to resume blocking.")
                TextButton(
                    onClick = onFixService,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Open Accessibility Settings")
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}
