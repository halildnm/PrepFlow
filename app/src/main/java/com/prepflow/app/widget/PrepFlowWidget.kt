package com.prepflow.app.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.prepflow.app.MainActivity
import com.prepflow.app.R
import com.prepflow.app.data.local.PrepFlowDatabase
import com.prepflow.app.data.local.entity.HomeworkEntity
import com.prepflow.app.data.local.entity.ScheduleSessionEntity
import com.prepflow.app.ui.components.DateTimeUtils
import com.prepflow.app.ui.theme.PrepDarkBackground
import com.prepflow.app.ui.theme.PrepDarkSurface
import com.prepflow.app.ui.theme.PrepOnBackground
import com.prepflow.app.ui.theme.PrepOnSurface
import com.prepflow.app.ui.theme.PrepOnSurfaceVariant
import com.prepflow.app.ui.theme.PrepPrimary
import com.prepflow.app.ui.theme.PrepSecondary
import com.prepflow.app.ui.theme.StatusSuccessGreen
import com.prepflow.app.ui.theme.StatusUrgentRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

private data class WidgetSnapshot(
    val nextSession: ScheduleSessionEntity?,
    val isOngoingSession: Boolean,
    val urgentHomeworks: List<HomeworkEntity>,
    val dateLabel: String
)

class PrepFlowWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = withContext(Dispatchers.IO) {
            loadWidgetSnapshot(context)
        }

        val darkScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context)
        } else {
            darkColorScheme(
                primary = PrepPrimary,
                secondary = PrepSecondary,
                background = PrepDarkBackground,
                surface = PrepDarkSurface,
                onBackground = PrepOnBackground,
                onSurface = PrepOnSurface,
                onSurfaceVariant = PrepOnSurfaceVariant
            )
        }
        val colorProviders = ColorProviders(light = darkScheme, dark = darkScheme)

        provideContent {
            GlanceTheme(colors = colorProviders) {
                PrepFlowWidgetContent(
                    context = context,
                    snapshot = snapshot
                )
            }
        }
    }

    private suspend fun loadWidgetSnapshot(context: Context): WidgetSnapshot {
        val db = PrepFlowDatabase.getInstance(context)
        val today = LocalDate.now()
        val nowTime = LocalTime.now()
        val todaySessions = db.scheduleDao()
            .getSessionsForDaySnapshot(today.dayOfWeek.value)
            .sortedBy { it.startTime }

        var ongoing: ScheduleSessionEntity? = null
        var nextUpcoming: ScheduleSessionEntity? = null

        for (session in todaySessions) {
            val start = DateTimeUtils.parseLocalTimeOrNull(session.startTime)
            val end = DateTimeUtils.parseLocalTimeOrNull(session.endTime)
            if (start != null && end != null && !nowTime.isBefore(start) && nowTime.isBefore(end)) {
                ongoing = session
                break
            }
            if (nextUpcoming == null && (end == null || nowTime.isBefore(end))) {
                nextUpcoming = session
            }
        }

        val urgentHomeworks = db.homeworkDao().getUpcomingActiveHomeworks(limit = 3)

        return WidgetSnapshot(
            nextSession = ongoing ?: nextUpcoming,
            isOngoingSession = ongoing != null,
            urgentHomeworks = urgentHomeworks,
            dateLabel = DateTimeUtils.formatTodayHeader(today)
        )
    }
}

@Composable
private fun PrepFlowWidgetContent(
    context: Context,
    snapshot: WidgetSnapshot
) {
    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val quickAddIntent = Intent(context, MainActivity::class.java).apply {
        putExtra(MainActivity.EXTRA_WIDGET_ACTION, MainActivity.ACTION_QUICK_ADD_HOMEWORK)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }

    val textPrimary = ColorProvider(Color(0xFFECF1FB))
    val textSecondary = ColorProvider(Color(0xFFA2AEC6))
    val accentPrimary = GlanceTheme.colors.primary
    val urgentColor = ColorProvider(StatusUrgentRed)
    val ongoingColor = ColorProvider(StatusSuccessGreen)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_glass_bg))
            .clickable(actionStartActivity(openAppIntent))
            .padding(14.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "PrepFlow",
                    style = TextStyle(
                        color = accentPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Text(
                    text = snapshot.dateLabel,
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }

            Box(
                modifier = GlanceModifier
                    .size(32.dp)
                    .background(ImageProvider(R.drawable.widget_add_btn_bg))
                    .clickable(actionStartActivity(quickAddIntent)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        color = accentPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ImageProvider(R.drawable.widget_glass_accent_card_bg))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            val session = snapshot.nextSession
            if (session != null) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (snapshot.isOngoingSession) "ŞU ANKİ DERS" else "SIRADAKİ DERS",
                        style = TextStyle(
                            color = if (snapshot.isOngoingSession) ongoingColor else accentPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "${session.startTime}–${session.endTime}",
                        style = TextStyle(
                            color = textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = session.title,
                    style = TextStyle(
                        color = textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                if (session.roomOrNote.isNotBlank()) {
                    Text(
                        text = "${session.skillTag} • ${session.roomOrNote}",
                        style = TextStyle(
                            color = textSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            } else {
                Text(
                    text = "SIRADAKİ DERS",
                    style = TextStyle(
                        color = accentPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = "Bugün kalan dersin yok",
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        Text(
            text = "YAKLAŞAN ÖDEVLER",
            style = TextStyle(
                color = textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        if (snapshot.urgentHomeworks.isEmpty()) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ImageProvider(R.drawable.widget_glass_card_bg))
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            ) {
                Text(
                    text = "Bekleyen ödevin yok. Yeni eklemek için + dokun.",
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        } else {
            val nowMillis = System.currentTimeMillis()
            snapshot.urgentHomeworks.forEachIndexed { index, hw ->
                val overdue = DateTimeUtils.isOverdue(hw.dueDateTime, nowMillis)
                val remaining = DateTimeUtils.formatRemainingTime(hw.dueDateTime, nowMillis)

                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(ImageProvider(R.drawable.widget_glass_card_bg))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = hw.title,
                            style = TextStyle(
                                color = textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = hw.skillTag,
                            style = TextStyle(
                                color = textSecondary,
                                fontSize = 10.sp
                            ),
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = remaining,
                        style = TextStyle(
                            color = if (overdue) urgentColor else accentPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }

                if (index < snapshot.urgentHomeworks.lastIndex) {
                    Spacer(modifier = GlanceModifier.height(5.dp))
                }
            }
        }
    }
}

class PrepFlowWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrepFlowWidget()
}
