package com.example.meditationapplication

import android.app.Application
import com.example.meditationapplication.service.ReminderWorker

class MeditationApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderWorker.createChannel(this)
    }
}
