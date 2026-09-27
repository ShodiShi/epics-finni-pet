package ru.finnipet.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "finni_pet_state")
private val STATE_KEY = stringPreferencesKey("game_state_json")

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

private fun decode(raw: String?): GameState =
    raw?.let { runCatching { json.decodeFromString<GameState>(it) }.getOrNull() } ?: GameState()

class GameRepository(private val context: Context) {

    val state: Flow<GameState> = context.dataStore.data.map { prefs -> decode(prefs[STATE_KEY]) }

    /** Applies [transform] atomically and returns (before, after). Unchanged states are not written. */
    suspend fun update(transform: (GameState) -> GameState): Pair<GameState, GameState> {
        var before = GameState()
        var after = GameState()
        context.dataStore.edit { prefs ->
            before = decode(prefs[STATE_KEY])
            after = transform(before)
            if (after != before) prefs[STATE_KEY] = json.encodeToString(GameState.serializer(), after)
        }
        return before to after
    }

    suspend fun reset() {
        context.dataStore.edit { prefs -> prefs[STATE_KEY] = json.encodeToString(GameState.serializer(), GameState()) }
    }
}
