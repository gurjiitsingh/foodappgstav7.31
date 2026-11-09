package  com.it10x.foodappgstav7_31.auth



import android.content.Context

object PosSessionManager {

    private const val PREF_NAME = "pos_session"

    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_EMPLOYEE_ID = "employee_id"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_MOBILE = "mobile"
    private const val KEY_ROLE = "role"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isLoggedIn(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_LOGGED_IN, false)
    }

    fun login(
        context: Context,
        userId: String,
        employeeId: String,
        fullName: String,
        mobile: String,
        role: String
    ) {
        prefs(context).edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_EMPLOYEE_ID, employeeId)
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_MOBILE, mobile)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun logout(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun getUserId(context: Context): String? {
        return prefs(context).getString(KEY_USER_ID, null)
    }

    fun getEmployeeId(context: Context): String? {
        return prefs(context).getString(KEY_EMPLOYEE_ID, null)
    }

    fun getFullName(context: Context): String? {
        return prefs(context).getString(KEY_FULL_NAME, null)
    }

    fun getMobile(context: Context): String? {
        return prefs(context).getString(KEY_MOBILE, null)
    }

    fun getRole(context: Context): String? {
        return prefs(context).getString(KEY_ROLE, null)
    }
}

