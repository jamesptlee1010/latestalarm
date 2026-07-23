package com.james.mathwakealarm

import android.app.Application

class TazAlarmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppRepository.initialise(this)
        AlarmScheduler.scheduleAll(this)
    }
}
