package com.futureready.salahcoach.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.futureready.salahcoach.R

class PrayerDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prayer_detail)
        title = intent.getStringExtra("prayer_name") ?: "Prayer"
    }
}
