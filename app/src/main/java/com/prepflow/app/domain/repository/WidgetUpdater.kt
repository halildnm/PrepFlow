package com.prepflow.app.domain.repository

interface WidgetUpdater {
    suspend fun refreshWidgets()
}

object NoOpWidgetUpdater : WidgetUpdater {
    override suspend fun refreshWidgets() = Unit
}
