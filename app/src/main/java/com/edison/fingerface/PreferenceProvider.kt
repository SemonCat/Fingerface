package com.edison.fingerface

import android.content.Context
import com.crossbowffs.remotepreferences.RemotePreferenceFile
import com.crossbowffs.remotepreferences.RemotePreferenceProvider
import com.crossbowffs.remotepreferences.RemotePreferences

class PreferenceProvider :
    RemotePreferenceProvider(AUTHORITY, arrayOf(RemotePreferenceFile(PREF_NAME, true))) {

    override fun checkAccess(prefFileName: String, prefKey: String, write: Boolean): Boolean {
        // Read-only for remote processes
        return !write
    }

    companion object {
        private const val AUTHORITY = BuildConfig.APPLICATION_ID + ".prefs"
        private const val PREF_NAME = "xposed_prefs"

        fun getRemote(context: Context) = RemotePreferences(context, AUTHORITY, PREF_NAME)
        fun get(context: Context) = context.createDeviceProtectedStorageContext()
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)!!

    }

}
