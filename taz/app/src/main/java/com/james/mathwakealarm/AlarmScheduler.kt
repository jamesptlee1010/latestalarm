package com.james.mathwakealarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.DayOfWeek
import java.time.ZonedDateTime

object AlarmScheduler {
    const val ACTION_START_ALARM = "com.james.mathwakealarm.START_ALARM"
    const val ACTION_BACKUP_ALARM = "com.james.mathwakealarm.BACKUP_ALARM"
    const val EXTRA_ALARM_ID = "alarm_id"
    private const val BACKUP_DELAY_MS = 10_000L

    fun scheduleAll(context: Context) {
        AppRepository.state.value.alarms.forEach { alarm ->
            if (alarm.enabled) schedule(context, alarm) else cancel(context, alarm.id)
        }
    }

    fun schedule(context: Context, alarm: AlarmConfig) {
        cancel(context, alarm.id)
        if (!alarm.enabled || alarm.days.isEmpty()) return
        val next = nextOccurrence(alarm)
        val manager = context.getSystemService(AlarmManager::class.java)
        val primary = primaryPendingIntent(context, alarm.id)
        val backup = backupPendingIntent(context, alarm.id)

        setBestAlarm(manager, next.toInstant().toEpochMilli(), primary)
        setBestAlarm(manager, next.toInstant().toEpochMilli() + BACKUP_DELAY_MS, backup)
        AppRepository.logReliability(alarm.id, "Alarm scheduled for $next")
    }

    fun scheduleTest(context: Context, alarmId: String, delayMillis: Long = 120_000L) {
        val manager = context.getSystemService(AlarmManager::class.java)
        val trigger = System.currentTimeMillis() + delayMillis
        setBestAlarm(manager, trigger, primaryPendingIntent(context, alarmId))
        setBestAlarm(manager, trigger + BACKUP_DELAY_MS, backupPendingIntent(context, alarmId))
        AppRepository.logReliability(alarmId, "Screen-off test scheduled for ${delayMillis / 1000} seconds")
    }

    fun cancel(context: Context, alarmId: String) {
        val manager = context.getSystemService(AlarmManager::class.java)
        manager.cancel(primaryPendingIntent(context, alarmId))
        manager.cancel(backupPendingIntent(context, alarmId))
    }

    fun cancelBackup(context: Context, alarmId: String) {
        context.getSystemService(AlarmManager::class.java)
            .cancel(backupPendingIntent(context, alarmId))
    }

    fun nextOccurrence(alarm: AlarmConfig, now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime {
        val validDays = alarm.days.mapNotNull { runCatching { DayOfWeek.of(it) }.getOrNull() }.toSet()
        var candidate = now.withSecond(0).withNano(0)
            .withHour(alarm.hour.coerceIn(0, 23))
            .withMinute(alarm.minute.coerceIn(0, 59))
        if (!candidate.isAfter(now)) candidate = candidate.plusDays(1)

        repeat(15) {
            if (candidate.dayOfWeek in validDays) {
                val millis = candidate.toInstant().toEpochMilli()
                val isSkipped = alarm.skipOccurrenceAt > 0L &&
                    kotlin.math.abs(millis - alarm.skipOccurrenceAt) < 60_000L
                if (!isSkipped) return candidate
            }
            candidate = candidate.plusDays(1)
        }
        return candidate
    }

    private fun setBestAlarm(manager: AlarmManager, triggerAt: Long, operation: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
            }
        } catch (_: SecurityException) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        }
    }

    private fun primaryPendingIntent(context: Context, alarmId: String): PendingIntent {
        val intent = Intent(context, AlarmService::class.java).apply {
            action = ACTION_START_ALARM
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getForegroundService(
            context,
            stableRequestCode(alarmId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun backupPendingIntent(context: Context, alarmId: String): PendingIntent {
        val intent = Intent(context, BackupAlarmReceiver::class.java).apply {
            action = ACTION_BACKUP_ALARM
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            stableRequestCode(alarmId) xor 0x40000000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun stableRequestCode(id: String): Int = id.hashCode() and 0x3fffffff
}
