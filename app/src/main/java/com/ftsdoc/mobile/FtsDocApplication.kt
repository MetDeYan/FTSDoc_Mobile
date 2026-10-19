package com.ftsdoc.mobile

import android.app.Application
import com.ftsdoc.mobile.di.AppContainer

class FtsDocApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
