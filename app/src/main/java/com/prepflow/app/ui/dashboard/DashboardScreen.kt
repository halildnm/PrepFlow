package com.prepflow.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prepflow.app.data.local.entity.ExamEntity
import com.prepflow.app.data.local.entity.HomeworkWithDetails
import com.prepflow.app.data.local.entity.ResourceEntity
import com.prepflow.app.domain.model.SkillTagDefaults
import com.prepflow.app.domain.repository.PrepFlowRepository
import com.prepflow.app.ui.components.DateTimeUtils
import com.prepflow.app.ui.components.EmptyStateGlassCard
import com.prepflow.app.ui.components.GlassCard
import com.prepflow.app.ui.components.GlassFloatingActionButton
import com.prepflow.app.ui.components.PillBadge
import com.prepflow.app.ui.components.PriorityPillBadge
import com.prepflow.app.ui.components.SectionHeaderRow
import com.prepflow.app.ui.components.SkillPillBadge
import com.prepflow.app.ui.navigation.PrepFlowTab
import com.prepflow.app.ui.theme.StatusMutedSlate
import com.prepflow.app.ui.theme.StatusProgressBlue
import com.prepflow.app.ui.theme.StatusSuccessGreen
import com.prepflow.app.ui.theme.StatusUrgentRed
import com.prepflow.app.ui.theme.StatusWarningAmber

@Composable
fun DashboardScreen(
    repository: PrepFlowRepository,
    onNavigateToTab: (PrepFlowTab) -> Unit,
    onOpenHomeworkAdd: () -> Unit,
    onOpenHomeworkDetail: (Long) -> Unit,
    contentPadding: PaddingValues
) {
    val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(repository))
    val state by vm.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "header") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = state.formattedDate,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${state.greeting}!",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PillBadge(
                            text = "${state.todaySessions.size} ders bugün",
                            accentColor = MaterialTheme.colorScheme.primary
                        )
                        PillBadge(
                            text = "${state.activeHomeworkCount} açık ödev",
                            accentColor = if (state.overdueCount > 0) StatusUrgentRed else StatusSuccessGreen
                        )
                        if (state.overdueCount > 0) {
                            PillBadge(
                                text = "${state.overdueCount} gecikmiş",
                                accentColor = StatusUrgentRed
                            )
                        }
                    }
                }
            }

            if (state.isCompletelyEmpty) {
                item(key = "demo_banner") {
                    GlassCard(
                        highlighted = true,
                        onClick = { vm.loadDemoData() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hızlı Başlangıç: Örnek Veri Yükle",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Ders programı, ödevler, sınav ve kaynakları tek dokunuşla doldurup dene.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item(key = "today_schedule_header") {
                SectionHeaderRow(
                    title = "Günün Dersleri",
                    actionLabel = "Haftalık",
                    onActionClick = { onNavigateToTab(PrepFlowTab.SCHEDULE) }
                )
            }

            if (state.todaySessions.isEmpty()) {
                item(key = "today_schedule_empty") {
                    EmptyStateGlassCard(
                        icon = Icons.Rounded.CalendarMonth,
                        title = "Bugün planlı ders yok",
                        subtitle = "Haftalık program sekmesinden derslerini ekleyebilirsin.",
                        actionLabel = "Programa Git",
                        onActionClick = { onNavigateToTab(PrepFlowTab.SCHEDULE) }
                    )
                }
            } else {
                items(
                    items = state.todaySessions,
                    key = { "session_${it.session.id}" }
                ) { item ->
                    TodaySessionCard(
                        item = item,
                        customTagColors = state.customTagColors,
                        onClick = { onNavigateToTab(PrepFlowTab.SCHEDULE) }
                    )
                }
            }

            state.nearestExam?.let { exam ->
                item(key = "nearest_exam_header") {
                    SectionHeaderRow(
                        title = "Yaklaşan Sınav",
                        actionLabel = "Tüm Sınavlar",
                        onActionClick = { onNavigateToTab(PrepFlowTab.ACADEMIC) }
                    )
                }
                item(key = "nearest_exam_card") {
                    NearestExamCountdownCard(
                        exam = exam,
                        onClick = { onNavigateToTab(PrepFlowTab.ACADEMIC) }
                    )
                }
            }

            item(key = "homework_header") {
                SectionHeaderRow(
                    title = "Yaklaşan ve Acil Ödevler",
                    actionLabel = "Tümü",
                    onActionClick = { onNavigateToTab(PrepFlowTab.HOMEWORK) }
                )
            }

            if (state.urgentAndUpcomingHomeworks.isEmpty()) {
                item(key = "homework_empty") {
                    EmptyStateGlassCard(
                        icon = Icons.Rounded.TaskAlt,
                        title = "Bekleyen ödevin yok",
                        subtitle = "Harika gidiyorsun! Yeni bir ödev eklemek için + butonuna dokun.",
                        actionLabel = "Ödev Ekle",
                        onActionClick = onOpenHomeworkAdd
                    )
                }
            } else {
                items(
                    items = state.urgentAndUpcomingHomeworks,
                    key = { "dash_hw_${it.homework.id}" }
                ) { details ->
                    DashboardHomeworkCard(
                        details = details,
                        customTagColors = state.customTagColors,
                        onMarkDone = { vm.markHomeworkDone(details.homework.id) },
                        onClick = { onOpenHomeworkDetail(details.homework.id) }
                    )
                }
            }

            item(key = "resources_header") {
                SectionHeaderRow(
                    title = "Kaynak İlerlemesi",
                    actionLabel = "Yönet",
                    onActionClick = { onNavigateToTab(PrepFlowTab.ACADEMIC) }
                )
            }

            if (state.resourcesPreview.isEmpty()) {
                item(key = "resources_empty") {
                    EmptyStateGlassCard(
                        icon = Icons.Rounded.Book,
                        title = "Henüz takip edilen kaynak yok",
                        subtitle = "Main Course, Workbook veya online platform ilerlemeni buradan takip et.",
                        actionLabel = "Kaynak Ekle",
                        onActionClick = { onNavigateToTab(PrepFlowTab.ACADEMIC) }
                    )
                }
            } else {
                item(key = "resources_card") {
                    GlassCard(onClick = { onNavigateToTab(PrepFlowTab.ACADEMIC) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.resourcesPreview.forEach { res ->
                                MiniResourceProgressRow(
                                    resource = res,
                                    customTagColors = state.customTagColors
                                )
                            }
                        }
                    }
                }
            }
        }

        GlassFloatingActionButton(
            icon = Icons.Rounded.Add,
            contentDescription = "Hızlı Ödev Ekle",
            onClick = onOpenHomeworkAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 18.dp,
                    bottom = (contentPadding.calculateBottomPadding() - 12.dp).coerceAtLeast(88.dp)
                )
        )
    }
}

@Composable
private fun TodaySessionCard(
    item: TodaySessionUiModel,
    customTagColors: Map<String, Long>,
    onClick: () -> Unit
) {
    val session = item.session
    val isHighlighted = item.state == SessionHighlightState.ONGOING || item.state == SessionHighlightState.NEXT
    val accentColor = when (item.state) {
        SessionHighlightState.ONGOING -> StatusSuccessGreen
        SessionHighlightState.NEXT -> MaterialTheme.colorScheme.primary
        SessionHighlightState.UPCOMING -> null
        SessionHighlightState.FINISHED -> null
    }
    val statusLabel = when (item.state) {
        SessionHighlightState.ONGOING -> "Şu An İşleniyor"
        SessionHighlightState.NEXT -> "Sıradaki Ders"
        SessionHighlightState.UPCOMING -> "Bugün"
        SessionHighlightState.FINISHED -> "Tamamlandı"
    }
    val alphaDim = if (item.state == SessionHighlightState.FINISHED) 0.62f else 1f

    GlassCard(
        highlighted = isHighlighted,
        accentColor = accentColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        (accentColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.14f)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = session.startTime,
                    style = MaterialTheme.typography.titleSmall,
                    color = (accentColor ?: MaterialTheme.colorScheme.primary).copy(alpha = alphaDim),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = session.endTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alphaDim)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkillPillBadge(skillTag = session.skillTag, customTagColors = customTagColors)
                    if (isHighlighted) {
                        PillBadge(
                            text = statusLabel,
                            accentColor = accentColor ?: StatusProgressBlue
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alphaDim),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (session.roomOrNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = session.roomOrNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alphaDim),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun NearestExamCountdownCard(
    exam: ExamEntity,
    onClick: () -> Unit
) {
    val countdown = DateTimeUtils.formatExamCountdown(exam.date)
    GlassCard(
        highlighted = true,
        accentColor = StatusWarningAmber,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Event,
                        contentDescription = null,
                        tint = StatusWarningAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = DateTimeUtils.formatShortDate(exam.date) + " • %${exam.weightPercent.toInt()} ağırlık",
                        style = MaterialTheme.typography.labelMedium,
                        color = StatusWarningAmber
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = exam.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (exam.coveredUnits.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = exam.coveredUnits,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(StatusWarningAmber.copy(alpha = 0.18f))
                    .border(1.dp, StatusWarningAmber.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = countdown,
                    style = MaterialTheme.typography.titleSmall,
                    color = StatusWarningAmber,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DashboardHomeworkCard(
    details: HomeworkWithDetails,
    customTagColors: Map<String, Long>,
    onMarkDone: () -> Unit,
    onClick: () -> Unit
) {
    val hw = details.homework
    val isOverdue = DateTimeUtils.isOverdue(hw.dueDateTime)
    val remainingText = DateTimeUtils.formatRemainingTime(hw.dueDateTime)

    GlassCard(
        accentColor = if (isOverdue) StatusUrgentRed else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isOverdue) StatusUrgentRed.copy(alpha = 0.16f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isOverdue) StatusUrgentRed else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .clickable(onClick = onMarkDone),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Tamamlandı işaretle",
                    tint = if (isOverdue) StatusUrgentRed else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hw.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkillPillBadge(skillTag = hw.skillTag, customTagColors = customTagColors)
                    PriorityPillBadge(priority = hw.priority)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = if (isOverdue) StatusUrgentRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${DateTimeUtils.formatDateTime(hw.dueDateTime)} • $remainingText",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isOverdue) StatusUrgentRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (details.totalSubtaskCount > 0) {
                        Text(
                            text = "${details.completedSubtaskCount}/${details.totalSubtaskCount}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniResourceProgressRow(
    resource: ResourceEntity,
    customTagColors: Map<String, Long>
) {
    val barColor = Color(SkillTagDefaults.resolveColorHex(resource.skillTag, customTagColors))
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = resource.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${resource.currentCount}/${resource.totalCount} ${resource.unitType.pluralShortTr}",
                style = MaterialTheme.typography.labelMedium,
                color = StatusMutedSlate
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { resource.progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = barColor,
            trackColor = Color.White.copy(alpha = 0.08f),
            strokeCap = StrokeCap.Round
        )
    }
}
