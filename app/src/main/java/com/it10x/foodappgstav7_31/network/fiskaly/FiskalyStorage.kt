package com.it10x.foodappgstav7_31.network.fiskaly

import android.content.Context

object FiskalyStorage {

    private const val PREF = "fiskaly_prefs"
    private const val KEY_TSS_ID = "tss_id"
    private const val KEY_PUK = "puk"

    fun save(context: Context, tssId: String, puk: String) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TSS_ID, tssId)
            .putString(KEY_PUK, puk)
            .apply()
    }

    fun getTssId(context: Context): String? {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_TSS_ID, null)
    }

    fun getPuk(context: Context): String? {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_PUK, null)
    }
}