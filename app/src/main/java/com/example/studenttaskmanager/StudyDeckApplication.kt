package com.example.studenttaskmanager

import android.app.Application
import com.example.studenttaskmanager.di.AppContainer
import com.example.studenttaskmanager.reminder.NotificationHelper

class StudyDeckApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannel(this)
    }
}
