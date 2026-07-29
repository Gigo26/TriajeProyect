package com.moviles.triaje.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_LANGUAGE = "language" // "es" or "en"
        const val KEY_DATA_SAVER = "data_saver"
        
        const val KEY_NOTIF_PUSH = "notif_push"
        const val KEY_NOTIF_EMAIL = "notif_email"
        const val KEY_NOTIF_UPDATES = "notif_updates"
        const val KEY_NOTIF_MESSAGES = "notif_messages"
        const val KEY_NOTIF_OFFERS = "notif_offers"
    }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "es") ?: "es"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var isDataSaver: Boolean
        get() = prefs.getBoolean(KEY_DATA_SAVER, false)
        set(value) = prefs.edit().putBoolean(KEY_DATA_SAVER, value).apply()

    var notifPush: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_PUSH, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_PUSH, value).apply()

    var notifEmail: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_EMAIL, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_EMAIL, value).apply()

    var notifUpdates: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_UPDATES, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_UPDATES, value).apply()

    var notifMessages: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_MESSAGES, false)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_MESSAGES, value).apply()

    var notifOffers: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_OFFERS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_OFFERS, value).apply()
}