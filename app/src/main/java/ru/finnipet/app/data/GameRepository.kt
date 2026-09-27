package ru.finnipet.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "finni_pet_state")
private val STATE_KEY = stringPreferencesKey("game_state_json")

private val json = Json { ignoreUnknownKeys = true }

class GameRepository(private val context: Context) {

    val state: Flow<GameState> = context.dataStore.data.map { prefs ->
        val raw = prefs[STATE_KEY]
        if (raw == null) {
            GameState()
        } else {
            try {
                json.decodeFromString<GameState>(raw)
            } catch (e: Exception) {
                GameState()
            }
        }
    }

    suspend fun update(transform: (GameState) -> GameState) {
        context.dataStore.edit { prefs ->
            val current = prefs[STATE_KEY]?.let {
                try {
                    json.decodeFromString<GameState>(it)
                } catch (e: Exception) {
                    GameState()
                }
            } ?: GameState()
            prefs[STATE_KEY] = json.encodeToString(transform(current))
        }
    }

    suspend fun reset() {
        context.dataStore.edit { prefs ->
            prefs[STATE_KEY] = json.encodeToString(GameState())
        }
    }
}
