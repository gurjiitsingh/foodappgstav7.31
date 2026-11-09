package com.it10x.foodappgstav7_31.storage

import android.content.Context
import android.content.SharedPreferences

object ClientStorage {

    private const val PREFS_NAME = "client_prefs"
    private const val KEY_CLIENT_ID = "client_id"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ✅ SAVE
    fun saveClientId(context: Context, clientId: String) {
        prefs(context).edit().putString(KEY_CLIENT_ID, clientId).apply()
    }

    // ✅ GET
    fun getClientId(context: Context): String? {
        return prefs(context).getString(KEY_CLIENT_ID, null)
    }

    // ✅ CLEAR (optional)
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}