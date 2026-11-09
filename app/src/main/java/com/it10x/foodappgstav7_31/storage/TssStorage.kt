package com.it10x.foodappgstav7_31.storage

import android.content.Context
import android.content.SharedPreferences

object TssStorage {

    private const val PREFS_NAME = "tss_prefs"
    private const val KEY_TSS_ID = "tss_id"
    private const val KEY_PUK = "tss_puk"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ✅ TSS ID
    fun saveTssId(context: Context, tssId: String) {
        prefs(context).edit().putString(KEY_TSS_ID, tssId).apply()
    }

    fun getTssId(context: Context): String? {
        return prefs(context).getString(KEY_TSS_ID, null)
    }

    // ✅ PUK storage
    fun savePuk(context: Context, puk: String) {
        prefs(context).edit().putString(KEY_PUK, puk).apply()
    }

    fun getPuk(context: Context): String? {
        return prefs(context).getString(KEY_PUK, null)
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun saveClientId(context: Context, clientId: String) {
        val prefs = context.getSharedPreferences("fiskaly_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("client_id", clientId).apply()
    }

    fun getClientId(context: Context): String? {
        val prefs = context.getSharedPreferences("fiskaly_prefs", Context.MODE_PRIVATE)
        return prefs.getString("client_id", null)
    }

}