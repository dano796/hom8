package com.homeflow.app.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("homeflow_session", Context.MODE_PRIVATE)

    var userId: String
        get() = prefs.getString(KEY_USER_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var hogarId: String
        get() = prefs.getString(KEY_HOGAR_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_HOGAR_ID, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "User") ?: "User"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userInitials: String
        get() = prefs.getString(KEY_USER_INITIALS, "U") ?: "U"
        set(value) = prefs.edit().putString(KEY_USER_INITIALS, value).apply()

    var partnerName: String
        get() = prefs.getString(KEY_PARTNER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARTNER_NAME, value).apply()

    var partnerInitials: String
        get() = prefs.getString(KEY_PARTNER_INITIALS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PARTNER_INITIALS, value).apply()

    val isLoggedIn: Boolean
        get() = userId.isNotEmpty()

    /** Local auth fallback: stores password hash keyed by email */
    fun getLocalPasswordHash(email: String): String? =
        prefs.getString("local_pw_${email.lowercase()}", null)

    fun setLocalPasswordHash(email: String, hash: String) =
        prefs.edit().putString("local_pw_${email.lowercase()}", hash).apply()

    fun clear() = prefs.edit().clear().apply()

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_HOGAR_ID = "hogar_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_INITIALS = "user_initials"
        private const val KEY_PARTNER_NAME = "partner_name"
        private const val KEY_PARTNER_INITIALS = "partner_initials"
    }
}
