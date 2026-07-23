package com.james.mathwakealarm

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

object AppRepository {
    private const val PREFS = "tazalarm_state_v210"
    private const val STATE_KEY = "app_state"
    private const val ACTIVE_ALARM_KEY = "active_alarm_id"
    private const val ACTIVE_STARTED_KEY = "active_alarm_started"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private lateinit var appContext: Context
    private val mutableState = mutableStateOf(AppState())
    val state: State<AppState> = mutableState

    @Synchronized
    fun initialise(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
        val raw = prefs().getString(STATE_KEY, null)
        mutableState.value = raw?.let {
            runCatching { json.decodeFromString<AppState>(it) }.getOrNull()
        } ?: AppState()
    }

    private fun prefs() = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @Synchronized
    private fun update(transform: (AppState) -> AppState) {
        val next = transform(mutableState.value)
        mutableState.value = next
        prefs().edit().putString(STATE_KEY, json.encodeToString(next)).apply()
    }

    fun completeOnboarding(name: String, firstAlarm: AlarmConfig) = completeOnboarding(name, listOf(firstAlarm))

    fun completeOnboarding(name: String, alarms: List<AlarmConfig>) = update { current ->
        current.copy(
            userName = name.trim().ifBlank { "James" },
            onboardingComplete = true,
            alarms = alarms.ifEmpty { listOf(defaultAlarm()) }
        )
    }

    fun setName(name: String) = update { it.copy(userName = name.trim().ifBlank { "James" }) }

    fun setTheme(theme: ThemeMode) = update { it.copy(themeMode = theme) }

    fun alarm(id: String?): AlarmConfig? = mutableState.value.alarms.firstOrNull { it.id == id }

    fun upsertAlarm(alarm: AlarmConfig) = update { current ->
        val exists = current.alarms.any { it.id == alarm.id }
        val alarms = if (exists) {
            current.alarms.map { if (it.id == alarm.id) alarm else it }
        } else {
            current.alarms + alarm
        }
        current.copy(alarms = alarms)
    }

    fun addAlarm(copyFrom: AlarmConfig? = null): AlarmConfig {
        val created = (copyFrom ?: defaultAlarm()).copy(
            id = UUID.randomUUID().toString(),
            label = if (copyFrom == null) "New Alarm" else "${copyFrom.label} Copy",
            skipOccurrenceAt = 0L
        )
        upsertAlarm(created)
        return created
    }

    fun deleteAlarm(id: String) = update { current ->
        current.copy(alarms = current.alarms.filterNot { it.id == id })
    }

    fun toggleAlarm(id: String, enabled: Boolean) = update { current ->
        current.copy(alarms = current.alarms.map {
            if (it.id == id) it.copy(enabled = enabled) else it
        })
    }

    fun setSkipOccurrence(id: String, occurrenceAt: Long) = update { current ->
        current.copy(alarms = current.alarms.map {
            if (it.id == id) it.copy(skipOccurrenceAt = occurrenceAt) else it
        })
    }

    fun clearSkipOccurrence(id: String) = setSkipOccurrence(id, 0L)

    fun saveRun(run: AlarmRun) = update { current ->
        current.copy(runs = (listOf(run) + current.runs).take(180))
    }

    fun logReliability(alarmId: String, stage: String) = update { current ->
        current.copy(
            reliabilityEvents = (
                listOf(ReliabilityEvent(alarmId, stage)) + current.reliabilityEvents
            ).take(500)
        )
    }

    fun markActiveAlarm(alarmId: String, startedAt: Long) {
        prefs().edit()
            .putString(ACTIVE_ALARM_KEY, alarmId)
            .putLong(ACTIVE_STARTED_KEY, startedAt)
            .apply()
    }

    fun clearActiveAlarm() {
        prefs().edit().remove(ACTIVE_ALARM_KEY).remove(ACTIVE_STARTED_KEY).apply()
    }

    fun activeAlarmId(): String? = prefs().getString(ACTIVE_ALARM_KEY, null)
    fun activeAlarmStartedAt(): Long = prefs().getLong(ACTIVE_STARTED_KEY, 0L)
}
