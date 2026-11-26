package com.prepflow.app.di

import android.content.Context
import com.prepflow.app.data.local.PrepFlowDatabase
import com.prepflow.app.data.repository.PrepFlowRepositoryImpl
import com.prepflow.app.domain.repository.NoOpWidgetUpdater
import com.prepflow.app.domain.repository.PrepFlowRepository
import com.prepflow.app.domain.repository.WidgetUpdater

class AppContainer(context: Context) {

    val database: PrepFlowDatabase = PrepFlowDatabase.getInstance(context)

    @Volatile
    var widgetUpdater: WidgetUpdater = NoOpWidgetUpdater

    val repository: PrepFlowRepository = PrepFlowRepositoryImpl(
        database = database,
        widgetUpdaterProvider = { widgetUpdater }
    )
}
