package com.example.buss

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesHelper {
    private const val PREFS_NAME = "buss_prefs"
    private const val KEY_FAVOURITES = "favourites"
    private const val KEY_USER_NAME = "user_name"

    private fun getPrefs(ctx: Context): SharedPreferences {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveFavourites(ctx: Context, favourites: Set<String>) {
        getPrefs(ctx).edit().putStringSet(KEY_FAVOURITES, favourites).apply()
    }

    fun getFavourites(ctx: Context): Set<String> {
        return getPrefs(ctx).getStringSet(KEY_FAVOURITES, emptySet()) ?: emptySet()
    }

    fun getUserName(ctx: Context): String {
        return getPrefs(ctx).getString(KEY_USER_NAME, "User") ?: "User"
    }

    fun saveUserName(ctx: Context, name: String) {
        getPrefs(ctx).edit().putString(KEY_USER_NAME, name).apply()
    }
}
