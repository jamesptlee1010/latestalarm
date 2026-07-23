package com.james.mathwakealarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import kotlin.math.min

class AlarmService : Service() {
    companion object {
        const val ACTION_STOP = "com.james.mathwakealarm.STOP_ALARM"
        const val ACTION_PAUSE_AUDIO = "com.james.mathwakealarm.PAUSE_AUDIO"
        const val EXTRA_PAUSE_MS = "pause_ms"
        private const val CHANNEL_ID = "tazalarm_alarm_v210"
        private const val NOTIFICATION_ID = 4210
    }

    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null
    private var activeAlarmId: String? = null
    private var volumeStep = 0

    private val volumeRamp = object : Runnable {
        override fun run() {
            volumeStep += 1
            val volume = min(1f, 0.05f + volumeStep * 0.105f)
            mediaPlayer?.setVolume(volume, volume)
            if (volume < 1f) handler.postDelayed(this, 10_000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppRepository.initialise(this)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopAlarm()
            ACTION_PAUSE_AUDIO -> pauseAudio(intent.getLongExtra(EXTRA_PAUSE_MS, 5_000L))
            AlarmScheduler.ACTION_START_ALARM -> {
                val id = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: return START_NOT_STICKY
                startAlarm(id)
            }
            null -> AppRepository.activeAlarmId()?.let { startAlarm(it, restoring = true) }
        }
        return START_STICKY
    }

    private fun startAlarm(alarmId: String, restoring: Boolean = false) {
        if (activeAlarmId == alarmId && mediaPlayer?.isPlaying == true) return
        val alarm = AppRepository.alarm(alarmId) ?: run {
            stopSelf()
            return
        }
        if (!alarm.enabled && !restoring) {
            stopSelf()
            return
        }

        activeAlarmId = alarmId
        val startedAt = if (restoring) {
            AppRepository.activeAlarmStartedAt().takeIf { it > 0L } ?: System.currentTimeMillis()
        } else System.currentTimeMillis()
        AppRepository.markActiveAlarm(alarmId, startedAt)
        AppRepository.logReliability(alarmId, if (restoring) "Alarm service restored" else "Exact alarm delivered")
        AlarmScheduler.cancelBackup(this, alarmId)

        acquireWakeLock()
        startForeground(NOTIFICATION_ID, buildNotification(alarm))
        AppRepository.logReliability(alarmId, "Foreground service started")
        startAudio(alarm.vibrate)
        AppRepository.logReliability(alarmId, "Audio started")

        val fullScreen = Intent(this, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra("started_at", startedAt)
        }
        startActivity(fullScreen)
        AppRepository.logReliability(alarmId, "Alarm screen displayed")
        AlarmScheduler.schedule(this, alarm.copy(skipOccurrenceAt = 0L))
        if (alarm.skipOccurrenceAt > 0L) AppRepository.clearSkipOccurrence(alarm.id)
    }

    private fun buildNotification(alarm: AlarmConfig) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("TAZALARM — ${alarm.label}")
        .setContentText("Complete your wake-up routine to stop the alarm.")
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setOngoing(true)
        .setAutoCancel(false)
        .setFullScreenIntent(
            PendingIntent.getActivity(
                this,
                alarm.id.hashCode(),
                Intent(this, AlarmActivity::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarm.id)
                    putExtra("started_at", AppRepository.activeAlarmStartedAt())
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            ),
            true
        )
        .build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.alarm_channel_description)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 700, 300, 700)
                setBypassDnd(true)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        wakeLock = getSystemService<PowerManager>()?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TAZALARM:ActiveAlarm"
        )?.apply { acquire(20 * 60 * 1000L) }
    }

    private fun startAudio(shouldVibrate: Boolean) {
        handler.removeCallbacks(volumeRamp)
        mediaPlayer?.release()
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mediaPlayer = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmService, uri)
                isLooping = true
                prepare()
                setVolume(0.05f, 0.05f)
                start()
            }
        }.getOrNull()
        volumeStep = 0
        handler.postDelayed(volumeRamp, 10_000L)

        if (shouldVibrate) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(VibratorManager::class.java).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            val pattern = longArrayOf(0, 700, 300, 700, 300)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        }
        val audioManager = getSystemService(AudioManager::class.java)
        audioManager?.mode = AudioManager.MODE_NORMAL
    }

    private fun pauseAudio(durationMs: Long) {
        val player = mediaPlayer ?: return
        val previous = min(1f, 0.05f + volumeStep * 0.105f)
        player.setVolume(0f, 0f)
        handler.postDelayed({ mediaPlayer?.setVolume(previous, previous) }, durationMs.coerceIn(1_000L, 15_000L))
    }

    private fun stopAlarm() {
        activeAlarmId?.let { AppRepository.logReliability(it, "Alarm stopped after routine completion") }
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.runCatching { stop() }
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
        activeAlarmId = null
        AppRepository.clearActiveAlarm()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        vibrator?.cancel()
        wakeLock?.let { if (it.isHeld) it.release() }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
