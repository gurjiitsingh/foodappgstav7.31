package com.it10x.foodappgstav7_31.storage

import android.content.Context
import android.content.SharedPreferences

object TransactionStorage {

    private const val PREFS_NAME = "txn_prefs"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ✅ SAVE PER SESSION
    fun saveTransactionId(
        context: Context,
        sessionId: String,
        txnId: String
    ) {
        prefs(context).edit().putString(sessionId, txnId).apply()
    }

    // ✅ GET PER SESSION
    fun getTransactionId(
        context: Context,
        sessionId: String
    ): String? {
        return prefs(context).getString(sessionId, null)
    }

    // ✅ CLEAR ONLY THIS SESSION
    fun clear(
        context: Context,
        sessionId: String
    ) {
        prefs(context).edit().remove(sessionId).apply()
    }
}