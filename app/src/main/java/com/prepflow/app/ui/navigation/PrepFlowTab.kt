package com.prepflow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Today
import androidx.compose.ui.graphics.vector.ImageVector

enum class PrepFlowTab(
    val route: String,
    val labelTr: String,
    val icon: ImageVector
) {
    DASHBOARD("dashboard", "Bugün", Icons.Rounded.Today),
    HOMEWORK("homework", "Ödevler", Icons.Rounded.Assignment),
    SCHEDULE("schedule", "Program", Icons.Rounded.CalendarMonth),
    ACADEMIC("academic", "Akademik", Icons.Rounded.School)
}
