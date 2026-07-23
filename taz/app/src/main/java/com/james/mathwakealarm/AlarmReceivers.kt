package com.james.mathwakealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BackupAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: return
        AppRepository.initialise(context)
        AppRepository.logReliability(alarmId, "Backup alarm delivered")
        val service = Intent(context, AlarmService::class.java).apply {
            action = AlarmScheduler.ACTION_START_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        ContextCompat.startForegroundService(context, service)
    }
}

class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppRepository.initialise(context)
        AlarmScheduler.scheduleAll(context)
        AppRepository.state.value.alarms.forEach {
            AppRepository.logReliability(it.id, "Rescheduled after ${intent.action ?: "system event"}")
        }
    }
}
