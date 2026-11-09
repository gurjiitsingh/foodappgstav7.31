package com.it10x.foodappgstav7_31.firebase

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore("client_prefs")

object ClientIdStore {

    private val KEY_CLIENT_ID = stringPreferencesKey("client_id")

    // 🔥 NEW KEYS
    private val KEY_API_KEY = stringPreferencesKey("api_key")
    private val KEY_APP_ID = stringPreferencesKey("application_id")
    private val KEY_PROJECT_ID = stringPreferencesKey("project_id")

    // =========================
    // CLIENT ID
    // =========================
    fun save(context: Context, clientId: String) = runBlocking {
        context.dataStore.edit {
            it[KEY_CLIENT_ID] = clientId
        }
    }

    fun get(context: Context): String? = runBlocking {
        context.dataStore.data.first()[KEY_CLIENT_ID]
    }

    // =========================
    // 🔥 SAVE CONFIG
    // =========================
    fun saveConfig(
        context: Context,
        apiKey: String,
        applicationId: String,
        projectId: String
    ) = runBlocking {
        context.dataStore.edit {
            it[KEY_API_KEY] = apiKey
            it[KEY_APP_ID] = applicationId
            it[KEY_PROJECT_ID] = projectId
        }
    }

    // =========================
    // 🔥 GET CONFIG
    // =========================
    fun getConfig(context: Context): ClientFirebaseConfig? = runBlocking {
        val prefs = context.dataStore.data.first()

        val apiKey = prefs[KEY_API_KEY]
        val appId = prefs[KEY_APP_ID]
        val projectId = prefs[KEY_PROJECT_ID]

        if (apiKey != null && appId != null && projectId != null) {
            ClientFirebaseConfig(
                apiKey = apiKey,
                applicationId = appId,
                projectId = projectId
            )
        } else {
            null
        }
    }

    // =========================
    // CLEAR
    // =========================
    fun clear(context: Context) = runBlocking {
        context.dataStore.edit {
            it.clear()
        }
    }


}

