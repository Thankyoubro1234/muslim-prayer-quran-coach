package com.futureready.salahcoach.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.futureready.salahcoach.R
import com.futureready.salahcoach.PrayerPilotApp
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class MemorizeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_memorize, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().applicationContext as PrayerPilotApp
        val container = view.findViewById<LinearLayout>(R.id.juz_list_container)
        val totalVerses = view.findViewById<TextView>(R.id.memorize_total)
        val progressBar = view.findViewById<ProgressBar>(R.id.memorize_overall_progress)
        val progressLabel = view.findViewById<TextView>(R.id.memorize_overall_label)

        lifecycleScope.launch {
            val memorized = app.repository.getMemorizedCount()
            val pct = (memorized.toFloat() / 6236f * 100f).toInt()
            totalVerses.text = "$memorized of 6236 verses"
            progressBar.progress = pct
            progressLabel.text = "$pct% of the Quran memorized"

            // Build 30 juz cards
            container.removeAllViews()
            for (juz in 1..30) {
                val card = layoutInflater.inflate(R.layout.item_juz, container, false)
                val title = card.findViewById<TextView>(R.id.juz_title)
                val sub = card.findViewById<TextView>(R.id.juz_sub)
                val pBar = card.findViewById<ProgressBar>(R.id.juz_progress)
                title.text = "Juz $juz"
                val (jMem, jTotal) = app.repository.getJuzProgress(juz)
                sub.text = "$jMem / $jTotal verses"
                pBar.progress = if (jTotal > 0) (jMem.toFloat() / jTotal * 100).toInt() else 0
                card.setOnClickListener {
                    lifecycleScope.launch {
                        // Toggle: if 0 mark all, if all mark 0
                        val current = app.repository.getJuzProgress(juz)
                        if (current.first == 0) {
                            app.repository.setJuzMemorized(juz, current.second)
                        } else {
                            app.repository.setJuzMemorized(juz, 0)
                        }
                        // Reload
                        view.post { onViewCreated(view, null) }
                    }
                }
                container.addView(card)
            }
        }
    }
}
