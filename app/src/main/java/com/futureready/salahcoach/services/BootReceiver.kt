package com.futureready.salahcoach.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.futureready.salahcoach.PrayerPilotApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val app = ctx.applicationContext as PrayerPilotApp
        GlobalScope.launch {
            val t = app.repository.computeTimes() ?: return@launch
            PrayerScheduler.scheduleAll(ctx, t)
        }
    }
}
