package com.prepflow.app.ui.homework

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.PlaylistAddCheck
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prepflow.app.data.local.entity.HomeworkWithDetails
import com.prepflow.app.domain.model.HomeworkPriority
import com.prepflow.app.domain.model.HomeworkStatus
import com.prepflow.app.domain.model.SkillTagDefaults
import com.prepflow.app.domain.repository.PrepFlowRepository
import com.prepflow.app.ui.components.DateTimeUtils
import com.prepflow.app.ui.components.EmptyStateGlassCard
import com.prepflow.app.ui.components.GlassCard
import com.prepflow.app.ui.components.GlassFloatingActionButton
import com.prepflow.app.ui.components.PriorityPillBadge
import com.prepflow.app.ui.components.SkillPillBadge
import com.prepflow.app.ui.components.StatusPillBadge
import com.prepflow.app.ui.theme.LocalGlassTokens
import com.prepflow.app.ui.theme.StatusSuccessGreen
import com.prepflow.app.ui.theme.StatusUrgentRed
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HomeworkScreen(
    repository: PrepFlowRepository,
    openAddTrigger: Boolean,
    onConsumeOpenAddTrigger: () -> Unit,
    initialSelectedHomeworkId: Long?,
    onConsumeSelectedHomeworkId: () -> Unit,
    contentPadding: PaddingValues
) {
    val vm: HomeworkViewModel = viewModel(factory = HomeworkViewModel.Factory(repository))
    val state by vm.uiState.collectAsState()

    var showEditor by remember { mutableStateOf(false) }
    var editingHomework by remember { mutableStateOf<HomeworkWithDetails?>(null) }
    var detailHomeworkId by remember { mutableStateOf<Long?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(openAddTrigger) {
        if (openAddTrigger) {
            editingHomework = null
            showEditor = true
            onConsumeOpenAddTrigger()
        }
    }

    LaunchedEffect(initialSelectedHomeworkId) {
        if (initialSelectedHomeworkId != null) {
            detailHomeworkId = initialSelectedHomeworkId
            onConsumeSelectedHomeworkId()
        }
    }

    val activeDetail = detailHomeworkId?.let { id ->
        state.allHomeworks.firstOrNull { it.homework.id == id }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hw_header") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Ödevler",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    HomeworkViewModeToggle(
                        selectedMode = state.filter.viewMode,
                        onSelectMode = vm::setViewMode
                    )
                }
            }

            if (state.filter.viewMode == HomeworkViewMode.LIST) {
                item(key = "hw_filters") {
                    HomeworkFilterBar(
                        state = state,
                        onToggleSkill = vm::toggleSkillFilter,
                        onTogglePriority = vm::togglePriorityFilter,
                        onToggleStatus = vm::toggleStatusFilter,
                        onClearFilters = vm::clearAllFilters
                    )
                }

                if (state.filteredListHomeworks.isEmpty()) {
                    item(key = "hw_list_empty") {
                        EmptyStateGlassCard(
                            icon = Icons.Rounded.TaskAlt,
                            title = "Gösterilecek ödev bulunamadı",
                            subtitle = "Filtreleri temizleyebilir veya + butonuna basarak yeni bir ödev ekleyebilirsin.",
                            actionLabel = "Yeni Ödev Ekle",
                            onActionClick = {
                                editingHomework = null
                                showEditor = true
                            }
                        )
                    }
                } else {
                    items(
                        items = state.filteredListHomeworks,
                        key = { "hw_list_${it.homework.id}" }
                    ) { details ->
                        HomeworkCardItem(
                            details = details,
                            customTagColors = state.customTagColors,
                            onCycleStatus = { vm.advanceHomeworkStatus(details.homework) },
                            onClick = { detailHomeworkId = details.homework.id }
                        )
                    }
                }
            } else {
                item(key = "hw_calendar_card") {
                    HomeworkMonthCalendarCard(
                        displayedMonth = state.filter.displayedMonth,
                        selectedDate = state.filter.selectedCalendarDate,
                        dateMap = state.calendarDateMap,
                        customTagColors = state.customTagColors,
                        onPreviousMonth = { vm.changeCalendarMonth(-1) },
                        onNextMonth = { vm.changeCalendarMonth(1) },
                        onSelectDate = vm::selectCalendarDate
                    )
                }

                item(key = "hw_calendar_day_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateTimeUtils.formatTodayHeader(state.filter.selectedCalendarDate),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${state.selectedDayHomeworks.size} ödev",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (state.selectedDayHomeworks.isEmpty()) {
                    item(key = "hw_calendar_day_empty") {
                        EmptyStateGlassCard(
                            icon = Icons.Rounded.CalendarMonth,
                            title = "Seçilen günde ödev yok",
                            subtitle = "Bu tarih için yeni bir ödev planlayabilirsin.",
                            actionLabel = "Bu Güne Ödev Ekle",
                            onActionClick = {
                                editingHomework = null
                                showEditor = true
                            }
                        )
                    }
                } else {
                    items(
                        items = state.selectedDayHomeworks,
                        key = { "hw_cal_${it.homework.id}" }
                    ) { details ->
                        HomeworkCardItem(
                            details = details,
                            customTagColors = state.customTagColors,
                            onCycleStatus = { vm.advanceHomeworkStatus(details.homework) },
                            onClick = { detailHomeworkId = details.homework.id }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = contentPadding.calculateBottomPadding())
        )

        GlassFloatingActionButton(
            icon = Icons.Rounded.Add,
            contentDescription = "Yeni Ödev Ekle",
            onClick = {
                editingHomework = null
                showEditor = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 18.dp,
                    bottom = (contentPadding.calculateBottomPadding() - 12.dp).coerceAtLeast(88.dp)
                )
        )
    }

    if (activeDetail != null) {
        HomeworkDetailSheet(
            details = activeDetail,
            customTagColors = state.customTagColors,
            onDismiss = { detailHomeworkId = null },
            onEdit = {
                editingHomework = activeDetail
                detailHomeworkId = null
                showEditor = true
            },
            onDelete = {
                vm.deleteHomework(activeDetail.homework.id)
                detailHomeworkId = null
            },
            onSelectStatus = { status ->
                vm.setHomeworkStatus(activeDetail.homework.id, status)
            },
            onToggleSubtask = { subId, checked ->
                vm.toggleSubtask(subId, checked)
            },
            onShowMessage = { msg ->
                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
            }
        )
    }

    if (showEditor) {
        val defaultDate = if (state.filter.viewMode == HomeworkViewMode.CALENDAR) {
            state.filter.selectedCalendarDate
        } else {
            LocalDate.now().plusDays(1)
        }
        HomeworkEditorSheet(
            existing = editingHomework,
            initialDate = defaultDate,
            availableSkillTags = state.availableSkillTags,
            onAddCustomSkillTag = vm::addCustomSkillTag,
            onDismiss = {
                showEditor = false
                editingHomework = null
            },
            onSave = { hw, links, subtasks ->
                vm.saveHomework(hw, links, subtasks)
                showEditor = false
                editingHomework = null
            }
        )
    }
}

@Composable
private fun HomeworkViewModeToggle(
    selectedMode: HomeworkViewMode,
    onSelectMode: (HomeworkViewMode) -> Unit
) {
    val glass = LocalGlassTokens.current
    val outerShape = RoundedCornerShape(18.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(glass.cardFill)
            .border(1.dp, glass.cardBorder, outerShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        HomeworkViewMode.entries.forEach { mode ->
            val selected = mode == selectedMode
            val innerShape = RoundedCornerShape(14.dp)
            val icon = if (mode == HomeworkViewMode.LIST) {
                Icons.Rounded.FormatListBulleted
            } else {
                Icons.Rounded.CalendarMonth
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(innerShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                        else Color.Transparent
                    )
                    .border(
                        width = if (selected) 1.dp else 0.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.50f) else Color.Transparent,
                        shape = innerShape
                    )
                    .clickable { onSelectMode(mode) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = mode.labelTr,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HomeworkFilterBar(
    state: HomeworkUiState,
    onToggleSkill: (String?) -> Unit,
    onTogglePriority: (HomeworkPriority?) -> Unit,
    onToggleStatus: (HomeworkStatus?) -> Unit,
    onClearFilters: () -> Unit
) {
    val hasActiveFilter = state.filter.selectedSkillTag != null ||
        state.filter.selectedPriority != null ||
        state.filter.selectedStatus != null

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeworkStatus.entries.forEach { st ->
                FilterChipPill(
                    label = st.labelTr,
                    selected = state.filter.selectedStatus == st,
                    accent = MaterialTheme.colorScheme.secondary,
                    onClick = { onToggleStatus(st) }
                )
            }
            HomeworkPriority.entries.forEach { prio ->
                FilterChipPill(
                    label = prio.labelTr,
                    selected = state.filter.selectedPriority == prio,
                    accent = if (prio == HomeworkPriority.URGENT) StatusUrgentRed else MaterialTheme.colorScheme.primary,
                    onClick = { onTogglePriority(prio) }
                )
            }
            if (hasActiveFilter) {
                TextButton(onClick = onClearFilters) {
                    Text("Temizle", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            state.availableSkillTags.forEach { tag ->
                FilterChipPill(
                    label = tag.name,
                    selected = state.filter.selectedSkillTag.equals(tag.name, ignoreCase = true),
                    accent = Color(tag.colorHex),
                    onClick = { onToggleSkill(tag.name) }
                )
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) accent.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = if (selected) accent else Color.White.copy(alpha = 0.12f),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun HomeworkCardItem(
    details: HomeworkWithDetails,
    customTagColors: Map<String, Long>,
    onCycleStatus: () -> Unit,
    onClick: () -> Unit
) {
    val hw = details.homework
    val isDone = hw.status == HomeworkStatus.DONE
    val isOverdue = !isDone && DateTimeUtils.isOverdue(hw.dueDateTime)

    GlassCard(
        accentColor = if (isOverdue) StatusUrgentRed else null,
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = hw.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusPillBadge(
                    status = hw.status,
                    onClick = onCycleStatus
                )
            }

            if (hw.description.isNotBlank()) {
                Text(
                    text = hw.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkillPillBadge(skillTag = hw.skillTag, customTagColors = customTagColors)
                PriorityPillBadge(priority = hw.priority)
                if (details.links.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${details.links.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (details.totalSubtaskCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlaylistAddCheck,
                        contentDescription = null,
                        tint = StatusSuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    LinearProgressIndicator(
                        progress = { details.subtaskProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50)),
                        color = StatusSuccessGreen,
                        trackColor = Color.White.copy(alpha = 0.08f),
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "${details.completedSubtaskCount}/${details.totalSubtaskCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = if (isOverdue) StatusUrgentRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${DateTimeUtils.formatDateTime(hw.dueDateTime)} • ${
                        if (isDone) "Tamamlandı" else DateTimeUtils.formatRemainingTime(hw.dueDateTime)
                    }",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isOverdue) StatusUrgentRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HomeworkMonthCalendarCard(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    dateMap: Map<LocalDate, List<HomeworkWithDetails>>,
    customTagColors: Map<String, Long>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val firstOfMonth = displayedMonth.atDay(1)
    val startOffset = firstOfMonth.dayOfWeek.value - 1
    val daysInMonth = displayedMonth.lengthOfMonth()
    val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7

    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Önceki Ay",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = DateTimeUtils.formatMonthYear(firstOfMonth),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Sonraki Ay",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz").forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val rows = totalCells / 7
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - startOffset + 1
                        if (dayNumber in 1..daysInMonth) {
                            val cellDate = displayedMonth.atDay(dayNumber)
                            val isSelected = cellDate == selectedDate
                            val isToday = cellDate == today
                            val dayHomeworks = dateMap[cellDate].orEmpty()

                            val cellShape = RoundedCornerShape(12.dp)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.95f)
                                    .clip(cellShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                                            isToday -> Color.White.copy(alpha = 0.08f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isSelected || isToday) 1.dp else 0.dp,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)
                                            else -> Color.Transparent
                                        },
                                        shape = cellShape
                                    )
                                    .clickable { onSelectDate(cellDate) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$dayNumber",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                if (dayHomeworks.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        dayHomeworks.take(3).forEach { hw ->
                                            val dotColor = if (hw.homework.status != HomeworkStatus.DONE &&
                                                DateTimeUtils.isOverdue(hw.homework.dueDateTime)
                                            ) {
                                                StatusUrgentRed
                                            } else {
                                                Color(
                                                    SkillTagDefaults.resolveColorHex(
                                                        hw.homework.skillTag,
                                                        customTagColors
                                                    )
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
