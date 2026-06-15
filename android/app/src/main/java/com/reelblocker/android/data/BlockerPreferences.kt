package com.reelblocker.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reel_blocker_prefs")

/**
 * Persisted user settings. The accessibility service observes these so changes take effect live.
 */
class BlockerPreferences(private val context: Context) {

    data class Settings(
        val enabled: Boolean,
        val blockReels: Boolean,
        val blockExplore: Boolean
    )

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            enabled = prefs[KEY_ENABLED] ?: true,
            blockReels = prefs[KEY_BLOCK_REELS] ?: true,
            blockExplore = prefs[KEY_BLOCK_EXPLORE] ?: true
        )
    }

    suspend fun setEnabled(value: Boolean) = update(KEY_ENABLED, value)
    suspend fun setBlockReels(value: Boolean) = update(KEY_BLOCK_REELS, value)
    suspend fun setBlockExplore(value: Boolean) = update(KEY_BLOCK_EXPLORE, value)

    private suspend fun update(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }

    private companion object {
        val KEY_ENABLED = booleanPreferencesKey("enabled")
        val KEY_BLOCK_REELS = booleanPreferencesKey("block_reels")
        val KEY_BLOCK_EXPLORE = booleanPreferencesKey("block_explore")
    }
}
