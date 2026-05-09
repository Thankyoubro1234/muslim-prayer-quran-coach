package com.futureready.salahcoach.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.futureready.salahcoach.R
import com.futureready.salahcoach.PrayerPilotApp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CoachFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_coach, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().applicationContext as PrayerPilotApp

        val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        view.findViewById<android.widget.TextView>(R.id.coach_today)?.text = sdf.format(Date())

        // Random ayah of the day from a small curated list
        val ayahs = listOf(
            "And whoever puts their trust in Allah, He will be enough for them. Surah At-Talaq 65:3",
            "Indeed, with hardship comes ease. Surah Ash-Sharh 94:6",
            "Allah does not burden a soul beyond what it can bear. Surah Al-Baqarah 2:286",
            "And He found you lost and guided you. Surah Ad-Duha 93:7",
            "So remember Me; I will remember you. Surah Al-Baqarah 2:152",
            "Indeed, the patient will be given their reward without account. Surah Az-Zumar 39:10"
        )
        val day = (System.currentTimeMillis() / (1000L * 60 * 60 * 24)).toInt()
        val ayah = ayahs[(day % ayahs.size + ayahs.size) % ayahs.size]
        view.findViewById<android.widget.TextView>(R.id.coach_ayah)?.text = ayah

        // Show streak (placeholder; pulls from repository)
        lifecycleScope.launch {
            val streak = app.repository.getReadingStreak()
            view.findViewById<android.widget.TextView>(R.id.coach_streak)?.text = "$streak day reading streak"

            val memorized = app.repository.getMemorizedCount()
            view.findViewById<android.widget.TextView>(R.id.coach_memorized)?.text =
                "$memorized verses memorized"

            val nextPrayer = app.repository.getNextPrayer()
            view.findViewById<android.widget.TextView>(R.id.coach_next_prayer)?.text =
                nextPrayer ?: "Set your location to see prayer times"
        }
    }
}
