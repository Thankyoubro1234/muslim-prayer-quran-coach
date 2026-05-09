package com.futureready.salahcoach.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.futureready.salahcoach.R
import com.futureready.salahcoach.fragments.SettingsFragment

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment()).commit()
        }
    }
}
