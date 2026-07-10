package com.blueberryjoy.history

import android.app.Application
import android.content.Context

class App : Application() {

    companion object {
        private lateinit var instance: App
        val appContext: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }
}
