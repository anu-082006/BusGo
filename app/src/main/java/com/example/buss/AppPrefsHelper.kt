package com.example.buss

import android.content.Context
import android.content.SharedPreferences

/**
 * Handles auth-related SharedPreferences (login, signup, session).
 * Note: Keep "BusGoPrefs" as the prefs name to match M1's original code.
 */
object AppPrefsHelper {

    private const val PREF_NAME = "BusGoPrefs"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val KEY_NAME = "name"
    private const val KEY_MOBILE = "mobile"
    private const val KEY_GENDER = "gender"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(context: Context, name: String, email: String, password: String, mobile: String, gender: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PASSWORD, password)
        editor.putString(KEY_MOBILE, mobile)
        editor.putString(KEY_GENDER, gender)
        editor.apply()
    }

    fun getName(context: Context): String {
        return getPrefs(context).getString(KEY_NAME, "") ?: ""
    }

    fun getEmail(context: Context): String? {
        return getPrefs(context).getString(KEY_EMAIL, null)
    }

    fun getPassword(context: Context): String? {
        return getPrefs(context).getString(KEY_PASSWORD, null)
    }

    fun getMobile(context: Context): String {
        return getPrefs(context).getString(KEY_MOBILE, "") ?: ""
    }

    fun getGender(context: Context): String {
        return getPrefs(context).getString(KEY_GENDER, "") ?: ""
    }

    fun saveLoginSession(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
