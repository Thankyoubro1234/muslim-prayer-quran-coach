package com.futureready.salahcoach.data

import android.content.Context
import android.location.Location
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.futureready.salahcoach.prayer.PrayTimes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.dataStore by preferencesDataStore("prayerpilot_prefs")

class PrayerRepository(private val ctx: Context) {
    val db = AppDatabase.get(ctx)
    private val K_LAT = doublePreferencesKey("lat")
    private val K_LNG = doublePreferencesKey("lng")
    private val K_METHOD = stringPreferencesKey("method")
    private val K_ASR = stringPreferencesKey("asr")
    private val K_HIGHLAT = stringPreferencesKey("highlat")
    private val K_ELEV = doublePreferencesKey("elev")
    private val K_OFFSETS = stringPreferencesKey("offsets")
    private val K_ADHAN_VOICE = stringPreferencesKey("adhan_voice")
    private val K_AUTO_DND = booleanPreferencesKey("auto_dnd")
    private val K_PRE_REMIND = intPreferencesKey("pre_remind_min")
    private val K_NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
    private val K_NOTIF_MODE = stringPreferencesKey("notif_mode")
    private val K_THEME = stringPreferencesKey("theme")
    private val K_LAST_SURAH_ID = intPreferencesKey("last_surah_id")
    private val K_LAST_SURAH_NAME = stringPreferencesKey("last_surah_name")
    private val K_QURAN_FONT_SIZE = intPreferencesKey("quran_font_size")

    private val K_FIRST_RUN = booleanPreferencesKey("first_run")

    suspend fun setLocation(loc: Location) {
        ctx.dataStore.edit { it[K_LAT] = loc.latitude; it[K_LNG] = loc.longitude; it[K_ELEV] = loc.altitude }
    }
    suspend fun getLocation(): Pair<Double, Double>? {
        val p = ctx.dataStore.data.first()
        val lat = p[K_LAT]; val lng = p[K_LNG]
        return if (lat != null && lng != null) lat to lng else null
    }
    val themeFlow: Flow<String> = ctx.dataStore.data.map { it[K_THEME] ?: "system" }

    suspend fun isFirstRun(): Boolean = ctx.dataStore.data.first()[K_FIRST_RUN] != false
    suspend fun completeFirstRun() = ctx.dataStore.edit { it[K_FIRST_RUN] = false }

    suspend fun computeTimes(date: Calendar = Calendar.getInstance()): PrayTimes.Times? {
        val (lat, lng) = getLocation() ?: return null
        val p = ctx.dataStore.data.first()
        val method = PrayTimes.Method.valueOf(p[K_METHOD] ?: "ISNA")
        val pt = PrayTimes(method).apply {
            asrJuristic = PrayTimes.AsrJuristic.valueOf(p[K_ASR] ?: "STANDARD")
            highLatRule = PrayTimes.HighLatRule.valueOf(p[K_HIGHLAT] ?: "ANGLE_BASED")
            elevation = p[K_ELEV] ?: 0.0
            (p[K_OFFSETS] ?: "0,0,0,0,0,0").split(",").mapIndexed { i, s ->
                manualOffsetsMinutes[i] = s.trim().toIntOrNull() ?: 0
            }
        }
        return pt.getTimes(date, lat, lng)
    }

    suspend fun setMethod(m: String) = ctx.dataStore.edit { it[K_METHOD] = m }
    suspend fun setAsr(a: String) = ctx.dataStore.edit { it[K_ASR] = a }
    suspend fun setHighLat(h: String) = ctx.dataStore.edit { it[K_HIGHLAT] = h }
    suspend fun setOffsets(s: String) = ctx.dataStore.edit { it[K_OFFSETS] = s }
    suspend fun setAdhanVoice(s: String) = ctx.dataStore.edit { it[K_ADHAN_VOICE] = s }
    suspend fun setAutoDnd(b: Boolean) = ctx.dataStore.edit { it[K_AUTO_DND] = b }
    suspend fun setPreRemind(min: Int) = ctx.dataStore.edit { it[K_PRE_REMIND] = min }
    suspend fun setNotificationsEnabled(b: Boolean) = ctx.dataStore.edit { it[K_NOTIF_ENABLED] = b }
    suspend fun setTheme(t: String) = ctx.dataStore.edit { it[K_THEME] = t }

    suspend fun getAdhanVoice(): String = ctx.dataStore.data.first()[K_ADHAN_VOICE] ?: "makkah"
    suspend fun getNotifMode(): String = ctx.dataStore.data.first()[K_NOTIF_MODE] ?: "full"
    suspend fun getAutoDnd(): Boolean = ctx.dataStore.data.first()[K_AUTO_DND] ?: false
    suspend fun getPreRemind(): Int = ctx.dataStore.data.first()[K_PRE_REMIND] ?: 0
    suspend fun isNotifEnabled(): Boolean = ctx.dataStore.data.first()[K_NOTIF_ENABLED] != false

    suspend fun setLastReadSurah(id: Int, name: String) = ctx.dataStore.edit {
        it[K_LAST_SURAH_ID] = id
        it[K_LAST_SURAH_NAME] = name
    }
    suspend fun getLastReadSurah(): Pair<Int, String>? {
        val p = ctx.dataStore.data.first()
        val id = p[K_LAST_SURAH_ID]; val nm = p[K_LAST_SURAH_NAME]
        return if (id != null && nm != null) id to nm else null
    }
    suspend fun getQuranFontSize(): Int = ctx.dataStore.data.first()[K_QURAN_FONT_SIZE] ?: 22
    suspend fun setQuranFontSize(sz: Int) = ctx.dataStore.edit { it[K_QURAN_FONT_SIZE] = sz }


    // === Coach + Memorize extensions ===
    private val K_MEMORIZED_JUZ = androidx.datastore.preferences.core.stringPreferencesKey("memorized_juz_map")
    private val K_READING_LAST_DATE = androidx.datastore.preferences.core.longPreferencesKey("reading_last_date")
    private val K_READING_STREAK = androidx.datastore.preferences.core.intPreferencesKey("reading_streak")

    private val JUZ_VERSE_COUNTS = intArrayOf(
    148, 111, 125, 131, 124, 111, 149, 142, 159, 127,
    151, 170, 154, 227, 185, 269, 190, 202, 339, 171,
    178, 169, 357, 175, 246, 195, 399, 137, 431, 564
    )

    suspend fun getJuzProgress(juz: Int): Pair<Int, Int> {
    val map = ctx.dataStore.data.first()[K_MEMORIZED_JUZ] ?: ""
    val parts = map.split(",").mapNotNull {
        val kv = it.split("=")
        if (kv.size == 2) kv[0].toIntOrNull()?.let { k -> k to (kv[1].toIntOrNull() ?: 0) } else null
    }.toMap()
    val total = JUZ_VERSE_COUNTS.getOrNull(juz - 1) ?: 0
    return Pair(parts[juz] ?: 0, total)
    }

    suspend fun setJuzMemorized(juz: Int, count: Int) {
    val map = ctx.dataStore.data.first()[K_MEMORIZED_JUZ] ?: ""
    val parts = map.split(",").mapNotNull {
        val kv = it.split("=")
        if (kv.size == 2) kv[0].toIntOrNull()?.let { k -> k to (kv[1].toIntOrNull() ?: 0) } else null
    }.toMutableMap()
    parts[juz] = count
    val newMap = parts.entries.joinToString(",") { "${it.key}=${it.value}" }
    ctx.dataStore.edit { it[K_MEMORIZED_JUZ] = newMap }
    }

    suspend fun getMemorizedCount(): Int {
    val map = ctx.dataStore.data.first()[K_MEMORIZED_JUZ] ?: ""
    return map.split(",").mapNotNull {
        val kv = it.split("=")
        if (kv.size == 2) kv[1].toIntOrNull() else null
    }.sum()
    }

    suspend fun getReadingStreak(): Int {
    val streak = ctx.dataStore.data.first()[K_READING_STREAK] ?: 0
    val last = ctx.dataStore.data.first()[K_READING_LAST_DATE] ?: 0L
    val now = System.currentTimeMillis()
    val daysSince = (now - last) / (1000L * 60 * 60 * 24)
    return if (daysSince > 1) 0 else streak
    }

    suspend fun markReadToday() {
    val now = System.currentTimeMillis()
    val last = ctx.dataStore.data.first()[K_READING_LAST_DATE] ?: 0L
    val streak = ctx.dataStore.data.first()[K_READING_STREAK] ?: 0
    val daysSince = (now - last) / (1000L * 60 * 60 * 24)
    val newStreak = if (daysSince == 1L) streak + 1 else if (daysSince == 0L) streak else 1
    ctx.dataStore.edit {
        it[K_READING_STREAK] = newStreak
        it[K_READING_LAST_DATE] = now
    }
    }

    suspend fun getNextPrayer(): String? {
    val times = computeTimes() ?: return null
    val now = Calendar.getInstance()
    val nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    fun toMin(h: Double): Int = (h * 60).toInt()
    val prayers = listOf(
        "Fajr" to toMin(times.fajr),
        "Dhuhr" to toMin(times.dhuhr),
        "Asr" to toMin(times.asr),
        "Maghrib" to toMin(times.maghrib),
        "Isha" to toMin(times.isha)
    )
    val next = prayers.firstOrNull { it.second > nowMin } ?: return "Fajr at ${minToHm(prayers[0].second)} tomorrow"
    val deltaMin = next.second - nowMin
    val hm = "${deltaMin / 60}h ${deltaMin % 60}m"
    return "${next.first} in $hm  -  ${minToHm(next.second)}"
    }

    private fun minToHm(min: Int): String {
    val h = min / 60
    val m = min % 60
    val ampm = if (h >= 12) "PM" else "AM"
    val hh = ((h + 11) % 12) + 1
    return "%d:%02d %s".format(hh, m, ampm)
    }
}
