package com.projects.allnotificationblocker.blockthemall.Activities.premium

import android.content.Context
import androidx.core.content.edit

// PrefSub updates
object PrefSub {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_LAST_DIALOG_TIME = "last_dialog_time"
    private const val KEY_LAST_FAILED_AD_TIME = "last_failed_ad_time"
    private const val KEY_IS_PREMIUM = "is_premium"
    private const val KEY_DIALOG_TEMP_DISABLED_UNTIL = "dialog_temp_disabled_until"

    fun saveDialogTime(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putLong(KEY_LAST_DIALOG_TIME, System.currentTimeMillis()) }
    }

    fun getLastDialogTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_DIALOG_TIME, 0)
    }

    fun saveFailedAdTime(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putLong(KEY_LAST_FAILED_AD_TIME, System.currentTimeMillis()) }
    }

    fun getLastFailedAdTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_FAILED_AD_TIME, 0)
    }

    fun isPremium(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_PREMIUM, false)
    }

    fun setPremium(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_IS_PREMIUM, value) }
    }

    fun disableDialogTemporarily(context: Context, durationMillis: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val until = System.currentTimeMillis() + durationMillis
        prefs.edit { putLong(KEY_DIALOG_TEMP_DISABLED_UNTIL, until) }
    }

    fun clearDialogTemporaryDisable(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove(KEY_DIALOG_TEMP_DISABLED_UNTIL) }
    }

    fun isDialogTemporarilyDisabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val until = prefs.getLong(KEY_DIALOG_TEMP_DISABLED_UNTIL, 0L)
        if (until <= 0L) {
            return false
        }
        if (System.currentTimeMillis() >= until) {
            prefs.edit { remove(KEY_DIALOG_TEMP_DISABLED_UNTIL) }
            return false
        }
        return true
    }

    fun getDialogTemporarilyDisabledUntil(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_DIALOG_TEMP_DISABLED_UNTIL, 0L)
    }
}