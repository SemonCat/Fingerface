package com.edison.fingerface

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Switch

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dialog = AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle(R.string.app_name)
            .setView(R.layout.settings_dialog)
            .setPositiveButton(android.R.string.ok, null)
            .setOnDismissListener { finish() }
            .show()
        val prefs = PreferenceProvider.get(this)
        val confirm = prefs.getBoolean(REQUIRE_CONFIRM, false)
        val switch = dialog.findViewById<Switch>(R.id.confirm_switch)
        switch.isChecked = confirm
        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(REQUIRE_CONFIRM, isChecked).apply()
        }
    }

    companion object {
        const val REQUIRE_CONFIRM = "confirm"
    }
}
