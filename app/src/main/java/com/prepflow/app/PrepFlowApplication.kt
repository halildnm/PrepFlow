package com.prepflow.app

import android.app.Application
import com.prepflow.app.di.AppContainer
import com.prepflow.app.widget.GlanceWidgetUpdater

class PrepFlowApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this).apply {
            widgetUpdater = GlanceWidgetUpdater(this@PrepFlowApplication)
        }
    }
}
