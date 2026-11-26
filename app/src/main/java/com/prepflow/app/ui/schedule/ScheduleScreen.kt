package com.prepflow.app.ui.schedule

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Weekend
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prepflow.app.data.local.entity.ScheduleSessionEntity
import com.prepflow.app.domain.model.SkillTagDefaults
import com.prepflow.app.domain.model.SkillTagOption
import com.prepflow.app.domain.repository.PrepFlowRepository
import com.prepflow.app.ui.components.DateTimeUtils
import com.prepflow.app.ui.components.EmptyStateGlassCard
import com.prepflow.app.ui.components.GlassCard
import com.prepflow.app.ui.components.GlassFloatingActionButton
import com.prepflow.app.ui.components.PrepTimePickerDialog
import com.prepflow.app.ui.components.SkillPillBadge
import com.prepflow.app.ui.components.SkillTagSelectorRow
import com.prepflow.app.ui.theme.LocalGlassTokens
import com.prepflow.app.ui.theme.StatusUrgentRed
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ScheduleScreen(
    repository: PrepFlowRepository,
    contentPadding: PaddingValues
) {
    val vm: ScheduleViewModel = viewModel(factory = ScheduleViewModel.Factory(repository))
    val state by vm.uiState.collectAsState()

    var showEditor by remember { mutableStateOf(false) }
    var editingSession by remember { mutableStateOf<ScheduleSessionEntity?>(null) }

    val daysToDisplay = if (state.showWeekend) (1..7).toList() else (1..5).toList()
    val todayDow = LocalDate.now().dayOfWeek.value

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "schedule_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ders Programı",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Haftalık toplam ${state.totalWeeklySessions} ders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val chipShape = RoundedCornerShape(50)
                    Row(
                        modifier = Modifier
                            .clip(chipShape)
                            .background(
                                if (state.showWeekend) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                else Color.White.copy(alpha = 0.06f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (state.showWeekend) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.14f),
                                shape = chipShape
                            )
                            .clickable { vm.toggleWeekendVisibility() }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Weekend,
                            contentDescription = null,
                            tint = if (state.showWeekend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (state.showWeekend) "7 Gün" else "Hafta İçi",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.showWeekend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item(key = "day_selector") {
                DaySelectorStrip(
                    days = daysToDisplay,
                    selectedDay = state.selectedDayOfWeek,
                    todayDay = todayDow,
                    sessionsByDay = state.sessionsByDay,
                    onSelectDay = vm::selectDay
                )
            }

            item(key = "selected_day_title") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateTimeUtils.dayOfWeekFullTr(state.selectedDayOfWeek),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${state.selectedDaySessions.size} oturum",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (state.selectedDaySessions.isEmpty()) {
                item(key = "schedule_day_empty") {
                    EmptyStateGlassCard(
                        icon = Icons.Rounded.CalendarMonth,
                        title = "${DateTimeUtils.dayOfWeekFullTr(state.selectedDayOfWeek)} için ders yok",
                        subtitle = "Sağ alttaki + butonundan bu güne ders ekleyebilir veya örnek program yükleyebilirsin.",
                        actionLabel = if (state.totalWeeklySessions == 0) "Örnek Program Yükle" else "Ders Ekle",
                        onActionClick = {
                            if (state.totalWeeklySessions == 0) {
                                vm.loadDemoData()
                            } else {
                                editingSession = null
                                showEditor = true
                            }
                        }
                    )
                }
            } else {
                items(
                    items = state.selectedDaySessions,
                    key = { "sched_${it.id}" }
                ) { session ->
                    ScheduleSessionCard(
                        session = session,
                        customTagColors = state.customTagColors,
                        onEdit = {
                            editingSession = session
                            showEditor = true
                        },
                        onDelete = { vm.deleteSession(session.id) }
                    )
                }
            }
        }

        GlassFloatingActionButton(
            icon = Icons.Rounded.Add,
            contentDescription = "Ders Ekle",
            onClick = {
                editingSession = null
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

    if (showEditor) {
        ScheduleSessionEditorSheet(
            existing = editingSession,
            defaultDayOfWeek = state.selectedDayOfWeek,
            availableSkillTags = state.availableSkillTags,
            onAddCustomSkillTag = vm::addCustomSkillTag,
            onDismiss = {
                showEditor = false
                editingSession = null
            },
            onSave = { entity ->
                vm.saveSession(entity)
                showEditor = false
                editingSession = null
            }
        )
    }
}

@Composable
private fun DaySelectorStrip(
    days: List<Int>,
    selectedDay: Int,
    todayDay: Int,
    sessionsByDay: Map<Int, List<ScheduleSessionEntity>>,
    onSelectDay: (Int) -> Unit
) {
    val glass = LocalGlassTokens.current
    val stripShape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(stripShape)
            .background(glass.cardFill)
            .border(1.dp, glass.cardBorder, stripShape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        days.forEach { day ->
            val isSelected = day == selectedDay
            val isToday = day == todayDay
            val count = sessionsByDay[day]?.size ?: 0
            val itemShape = RoundedCornerShape(14.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(itemShape)
                    .background(
                        when {
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                            isToday -> Color.White.copy(alpha = 0.06f)
                            else -> Color.Transparent
                        }
                    )
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = itemShape
                    )
                    .clickable { onSelectDay(day) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = DateTimeUtils.dayOfWeekShortTr(day),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                count > 0 && isSelected -> MaterialTheme.colorScheme.primary
                                count > 0 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.80f)
                                else -> Color.Transparent
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun ScheduleSessionCard(
    session: ScheduleSessionEntity,
    customTagColors: Map<String, Long>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = Color(SkillTagDefaults.resolveColorHex(session.skillTag, customTagColors))

    GlassCard(
        accentColor = accentColor,
        onClick = onEdit
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
                    .background(accentColor.copy(alpha = 0.16f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 11.dp, vertical = 9.dp)
            ) {
                Text(
                    text = session.startTime,
                    style = MaterialTheme.typography.titleSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = session.endTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                SkillPillBadge(skillTag = session.skillTag, customTagColors = customTagColors)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (session.roomOrNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = session.roomOrNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Düzenle",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Sil",
                        tint = StatusUrgentRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleSessionEditorSheet(
    existing: ScheduleSessionEntity?,
    defaultDayOfWeek: Int,
    availableSkillTags: List<SkillTagOption>,
    onAddCustomSkillTag: (String, Long) -> Unit,
    onDismiss: () -> Unit,
    onSave: (ScheduleSessionEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val glass = LocalGlassTokens.current

    val initialStart = existing?.startTime?.let { DateTimeUtils.parseLocalTimeOrNull(it) } ?: LocalTime.of(9, 0)
    val initialEnd = existing?.endTime?.let { DateTimeUtils.parseLocalTimeOrNull(it) } ?: LocalTime.of(10, 40)

    var title by remember(existing) { mutableStateOf(existing?.title.orEmpty()) }
    var dayOfWeek by remember(existing) { mutableIntStateOf(existing?.dayOfWeek ?: defaultDayOfWeek) }
    var skillTag by remember(existing) { mutableStateOf(existing?.skillTag ?: availableSkillTags.first().name) }
    var startTime by remember(existing) { mutableStateOf(initialStart) }
    var endTime by remember(existing) { mutableStateOf(initialEnd) }
    var roomOrNote by remember(existing) { mutableStateOf(existing?.roomOrNote.orEmpty()) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = glass.cardFillElevated.copy(alpha = 0.98f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (existing == null) "Yeni Ders Ekle" else "Dersi Düzenle",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Ders adı *") },
                placeholder = { Text("örn. Main Course, Reading & Writing") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Gün",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..7).forEach { day ->
                        val selected = day == dayOfWeek
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(shape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f),
                                    shape = shape
                                )
                                .clickable { dayOfWeek = day }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = DateTimeUtils.dayOfWeekShortTr(day),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showStartPicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Başlangıç: ${DateTimeUtils.formatTime(startTime.hour, startTime.minute)}")
                    }
                    OutlinedButton(
                        onClick = { showEndPicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bitiş: ${DateTimeUtils.formatTime(endTime.hour, endTime.minute)}")
                    }
                }
                timeError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusUrgentRed
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Beceri / Renk Etiketi",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SkillTagSelectorRow(
                    availableTags = availableSkillTags,
                    selectedTag = skillTag,
                    onSelectTag = { skillTag = it },
                    onAddCustomTag = onAddCustomSkillTag
                )
            }

            OutlinedTextField(
                value = roomOrNote,
                onValueChange = { roomOrNote = it },
                label = { Text("Derslik veya not") },
                placeholder = { Text("örn. B-204 • Öğr. Gör. Ayşe Yılmaz") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("İptal")
                }
                Button(
                    onClick = {
                        val cleanTitle = title.trim()
                        if (cleanTitle.isEmpty()) return@Button
                        if (!endTime.isAfter(startTime)) {
                            timeError = "Bitiş saati başlangıç saatinden sonra olmalıdır."
                            return@Button
                        }
                        onSave(
                            ScheduleSessionEntity(
                                id = existing?.id ?: 0L,
                                dayOfWeek = dayOfWeek,
                                title = cleanTitle,
                                skillTag = skillTag,
                                startTime = DateTimeUtils.formatTime(startTime.hour, startTime.minute),
                                endTime = DateTimeUtils.formatTime(endTime.hour, endTime.minute),
                                roomOrNote = roomOrNote.trim()
                            )
                        )
                    },
                    enabled = title.trim().isNotEmpty(),
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (existing == null) "Dersi Kaydet" else "Güncelle")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }

    if (showStartPicker) {
        PrepTimePickerDialog(
            initialHour = startTime.hour,
            initialMinute = startTime.minute,
            title = "Başlangıç Saati",
            onTimeSelected = { h, m ->
                startTime = LocalTime.of(h, m)
                if (!endTime.isAfter(startTime)) {
                    endTime = startTime.plusMinutes(50)
                }
                timeError = null
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        PrepTimePickerDialog(
            initialHour = endTime.hour,
            initialMinute = endTime.minute,
            title = "Bitiş Saati",
            onTimeSelected = { h, m ->
                endTime = LocalTime.of(h, m)
                timeError = null
            },
            onDismiss = { showEndPicker = false }
        )
    }
}
