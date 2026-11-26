package com.prepflow.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.prepflow.app.domain.repository.WidgetUpdater

class GlanceWidgetUpdater(
    private val context: Context
) : WidgetUpdater {

    override suspend fun refreshWidgets() {
        runCatching {
            PrepFlowWidget().updateAll(context.applicationContext)
        }
    }
}
