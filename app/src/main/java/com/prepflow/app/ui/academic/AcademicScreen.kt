package com.prepflow.app.ui.academic

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prepflow.app.data.local.entity.ExamEntity
import com.prepflow.app.data.local.entity.ResourceEntity
import com.prepflow.app.domain.model.ResourceUnitType
import com.prepflow.app.domain.model.SkillTagDefaults
import com.prepflow.app.domain.model.SkillTagOption
import com.prepflow.app.domain.repository.PrepFlowRepository
import com.prepflow.app.ui.components.DateTimeUtils
import com.prepflow.app.ui.components.EmptyStateGlassCard
import com.prepflow.app.ui.components.GlassCard
import com.prepflow.app.ui.components.GlassFloatingActionButton
import com.prepflow.app.ui.components.PillBadge
import com.prepflow.app.ui.components.PrepDatePickerDialog
import com.prepflow.app.ui.components.SkillPillBadge
import com.prepflow.app.ui.components.SkillTagSelectorRow
import com.prepflow.app.ui.theme.LocalGlassTokens
import com.prepflow.app.ui.theme.StatusMutedSlate
import com.prepflow.app.ui.theme.StatusSuccessGreen
import com.prepflow.app.ui.theme.StatusUrgentRed
import com.prepflow.app.ui.theme.StatusWarningAmber
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

@Composable
fun AcademicScreen(
    repository: PrepFlowRepository,
    contentPadding: PaddingValues
) {
    val vm: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory(repository))
    val state by vm.uiState.collectAsState()

    var showExamEditor by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<ExamEntity?>(null) }

    var showResourceEditor by remember { mutableStateOf(false) }
    var editingResource by remember { mutableStateOf<ResourceEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "academic_header") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Akademik",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (state.exams.isEmpty() && state.resources.isEmpty()) {
                            val pillShape = RoundedCornerShape(50)
                            Row(
                                modifier = Modifier
                                    .clip(pillShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.40f), pillShape)
                                    .clickable { vm.loadDemoData() }
                                    .padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Demo Veri",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    AcademicSubTabToggle(
                        selectedTab = state.selectedSubTab,
                        onSelectTab = vm::selectSubTab
                    )
                }
            }

            if (state.selectedSubTab == AcademicSubTab.EXAMS) {
                item(key = "grade_summary_card") {
                    WeightedAverageSummaryCard(summary = state.gradeSummary)
                }

                if (state.exams.isEmpty()) {
                    item(key = "exams_empty") {
                        EmptyStateGlassCard(
                            icon = Icons.Rounded.School,
                            title = "Henüz sınav eklenmedi",
                            subtitle = "Quiz, Midterm veya Speaking sınavlarını ağırlıklarıyla ekleyip ortalamanı anlık takip et.",
                            actionLabel = "Sınav Ekle",
                            onActionClick = {
                                editingExam = null
                                showExamEditor = true
                            }
                        )
                    }
                } else {
                    items(
                        items = state.exams,
                        key = { "exam_${it.id}" }
                    ) { exam ->
                        ExamCardItem(
                            exam = exam,
                            onEdit = {
                                editingExam = exam
                                showExamEditor = true
                            },
                            onDelete = { vm.deleteExam(exam.id) }
                        )
                    }
                }
            } else {
                if (state.resources.isEmpty()) {
                    item(key = "resources_empty") {
                        EmptyStateGlassCard(
                            icon = Icons.Rounded.Book,
                            title = "Henüz kaynak eklenmedi",
                            subtitle = "Main Course Book, Workbook veya online platform ilerlemeni ünite ya da sayfa bazlı takip et.",
                            actionLabel = "Kaynak Ekle",
                            onActionClick = {
                                editingResource = null
                                showResourceEditor = true
                            }
                        )
                    }
                } else {
                    items(
                        items = state.resources,
                        key = { "resource_${it.id}" }
                    ) { resource ->
                        ResourceProgressCard(
                            resource = resource,
                            customTagColors = state.customTagColors,
                            onDecrement = { vm.adjustResourceProgress(resource, -1) },
                            onIncrement = { vm.adjustResourceProgress(resource, 1) },
                            onEdit = {
                                editingResource = resource
                                showResourceEditor = true
                            },
                            onDelete = { vm.deleteResource(resource.id) }
                        )
                    }
                }
            }
        }

        GlassFloatingActionButton(
            icon = Icons.Rounded.Add,
            contentDescription = if (state.selectedSubTab == AcademicSubTab.EXAMS) "Sınav Ekle" else "Kaynak Ekle",
            onClick = {
                if (state.selectedSubTab == AcademicSubTab.EXAMS) {
                    editingExam = null
                    showExamEditor = true
                } else {
                    editingResource = null
                    showResourceEditor = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 18.dp,
                    bottom = (contentPadding.calculateBottomPadding() - 12.dp).coerceAtLeast(88.dp)
                )
        )
    }

    if (showExamEditor) {
        ExamEditorSheet(
            existing = editingExam,
            onDismiss = {
                showExamEditor = false
                editingExam = null
            },
            onSave = { entity ->
                vm.saveExam(entity)
                showExamEditor = false
                editingExam = null
            }
        )
    }

    if (showResourceEditor) {
        ResourceEditorSheet(
            existing = editingResource,
            availableSkillTags = state.availableSkillTags,
            onAddCustomSkillTag = vm::addCustomSkillTag,
            onDismiss = {
                showResourceEditor = false
                editingResource = null
            },
            onSave = { entity ->
                vm.saveResource(entity)
                showResourceEditor = false
                editingResource = null
            }
        )
    }
}

@Composable
private fun AcademicSubTabToggle(
    selectedTab: AcademicSubTab,
    onSelectTab: (AcademicSubTab) -> Unit
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
        AcademicSubTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            val innerShape = RoundedCornerShape(14.dp)
            val icon = if (tab == AcademicSubTab.EXAMS) Icons.Rounded.EventNote else Icons.Rounded.Book

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
                    .clickable { onSelectTab(tab) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tab.labelTr,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun WeightedAverageSummaryCard(summary: GradeSummary) {
    val avg = summary.weightedAverage
    val scoreColor = when {
        avg == null -> MaterialTheme.colorScheme.primary
        avg >= 70.0 -> StatusSuccessGreen
        avg >= 50.0 -> StatusWarningAmber
        else -> StatusUrgentRed
    }
    val avgDisplay = if (avg != null) String.format(Locale("tr", "TR"), "%.1f", avg) else "—"
    val earnedDisplay = String.format(Locale("tr", "TR"), "%.1f", summary.weightedEarnedPoints)

    GlassCard(
        highlighted = true,
        accentColor = scoreColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ağırlıklı Not Ortalaması",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (summary.gradedExamCount > 0) {
                        "${summary.gradedExamCount}/${summary.totalExamCount} sınav açıklandı • %${summary.gradedWeightTotal.toInt()} ağırlık üzerinden"
                    } else {
                        "Notu girilen sınav olduğunda ortalama otomatik hesaplanır."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillBadge(
                        text = "Kazanılan: $earnedDisplay puan",
                        accentColor = scoreColor
                    )
                    PillBadge(
                        text = "Planlı ağırlık: %${summary.totalPlannedWeight.toInt()}",
                        accentColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(scoreColor.copy(alpha = 0.16f))
                    .border(2.dp, scoreColor.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = avgDisplay,
                        style = MaterialTheme.typography.headlineSmall,
                        color = scoreColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/ 100",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamCardItem(
    exam: ExamEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val countdown = DateTimeUtils.formatExamCountdown(exam.date)
    val scoreColor = when {
        exam.score == null -> StatusWarningAmber
        exam.score >= 70.0 -> StatusSuccessGreen
        exam.score >= 50.0 -> StatusWarningAmber
        else -> StatusUrgentRed
    }

    GlassCard(
        accentColor = if (exam.score == null) null else scoreColor,
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PillBadge(
                        text = "%${exam.weightPercent.toInt()} ağırlık",
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    PillBadge(
                        text = countdown,
                        accentColor = if (exam.score == null) StatusWarningAmber else StatusMutedSlate
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = exam.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = DateTimeUtils.formatShortDate(exam.date) +
                        if (exam.coveredUnits.isNotBlank()) " • ${exam.coveredUnits}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(scoreColor.copy(alpha = 0.16f))
                        .border(1.dp, scoreColor.copy(alpha = 0.42f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = exam.score?.let { String.format(Locale("tr", "TR"), "%.1f", it) } ?: "Bekliyor",
                        style = MaterialTheme.typography.titleSmall,
                        color = scoreColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Düzenle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Sil",
                            tint = StatusUrgentRed,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceProgressCard(
    resource: ResourceEntity,
    customTagColors: Map<String, Long>,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = Color(SkillTagDefaults.resolveColorHex(resource.skillTag, customTagColors))
    val percent = (resource.progressFraction * 100f).toInt()

    GlassCard(
        accentColor = accentColor,
        onClick = onEdit
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SkillPillBadge(skillTag = resource.skillTag, customTagColors = customTagColors)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = resource.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Düzenle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Sil",
                            tint = StatusUrgentRed,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { resource.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = accentColor,
                trackColor = Color.White.copy(alpha = 0.09f),
                strokeCap = StrokeCap.Round
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${resource.currentCount} / ${resource.totalCount} ${resource.unitType.pluralShortTr}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "%$percent tamamlandı",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val canDecrease = resource.currentCount > 0
                    val canIncrease = resource.currentCount < resource.totalCount

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = if (canDecrease) 0.10f else 0.03f))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = if (canDecrease) 0.20f else 0.06f),
                                CircleShape
                            )
                            .clickable(enabled = canDecrease, onClick = onDecrement),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Azalt",
                            tint = if (canDecrease) MaterialTheme.colorScheme.onSurface else StatusMutedSlate,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = if (canIncrease) 0.24f else 0.06f))
                            .border(
                                1.dp,
                                accentColor.copy(alpha = if (canIncrease) 0.55f else 0.14f),
                                CircleShape
                            )
                            .clickable(enabled = canIncrease, onClick = onIncrement),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Artır",
                            tint = if (canIncrease) accentColor else StatusMutedSlate,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamEditorSheet(
    existing: ExamEntity?,
    onDismiss: () -> Unit,
    onSave: (ExamEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val glass = LocalGlassTokens.current

    var title by remember(existing) { mutableStateOf(existing?.title.orEmpty()) }
    var selectedDate by remember(existing) {
        mutableStateOf(
            existing?.date?.let { DateTimeUtils.epochMillisToLocalDate(it) } ?: LocalDate.now().plusDays(7)
        )
    }
    var coveredUnits by remember(existing) { mutableStateOf(existing?.coveredUnits.orEmpty()) }
    var weightText by remember(existing) {
        mutableStateOf(existing?.weightPercent?.let {
            if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
        } ?: "20")
    }
    var scoreText by remember(existing) {
        mutableStateOf(existing?.score?.let {
            if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
        } ?: "")
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (existing == null) "Yeni Sınav / Quiz Ekle" else "Sınavı Düzenle",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Sınav adı *") },
                placeholder = { Text("örn. Quiz 2, Midterm, Speaking Exam") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tarih: ${DateTimeUtils.formatTodayHeader(selectedDate)}"
                )
            }

            OutlinedTextField(
                value = coveredUnits,
                onValueChange = { coveredUnits = it },
                label = { Text("Sorumlu olunan üniteler / konular") },
                placeholder = { Text("örn. Unit 1–4, Passive Voice, Essay") },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = {
                        weightText = it
                        validationError = null
                    },
                    label = { Text("Ağırlık (%) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = scoreText,
                    onValueChange = {
                        scoreText = it
                        validationError = null
                    },
                    label = { Text("Alınan Not (Opsiyonel)") },
                    placeholder = { Text("0–100") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            validationError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusUrgentRed
                )
            }

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

                        val weight = weightText.replace(",", ".").trim().toDoubleOrNull()
                        if (weight == null || weight <= 0.0 || weight > 100.0) {
                            validationError = "Ağırlık 1 ile 100 arasında geçerli bir sayı olmalıdır."
                            return@Button
                        }

                        val rawScore = scoreText.replace(",", ".").trim()
                        val parsedScore = if (rawScore.isEmpty()) {
                            null
                        } else {
                            val num = rawScore.toDoubleOrNull()
                            if (num == null || num < 0.0 || num > 100.0) {
                                validationError = "Not 0 ile 100 arasında olmalı veya boş bırakılmalıdır."
                                return@Button
                            }
                            num
                        }

                        val dateMillis = DateTimeUtils.localDateTimeToEpochMillis(
                            selectedDate,
                            LocalTime.of(9, 0)
                        )
                        onSave(
                            ExamEntity(
                                id = existing?.id ?: 0L,
                                title = cleanTitle,
                                date = dateMillis,
                                coveredUnits = coveredUnits.trim(),
                                weightPercent = weight,
                                score = parsedScore
                            )
                        )
                    },
                    enabled = title.trim().isNotEmpty(),
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (existing == null) "Sınavı Kaydet" else "Güncelle")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }

    if (showDatePicker) {
        PrepDatePickerDialog(
            initialDate = selectedDate,
            onDateSelected = { selectedDate = it },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResourceEditorSheet(
    existing: ResourceEntity?,
    availableSkillTags: List<SkillTagOption>,
    onAddCustomSkillTag: (String, Long) -> Unit,
    onDismiss: () -> Unit,
    onSave: (ResourceEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val glass = LocalGlassTokens.current

    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    var unitType by remember(existing) { mutableStateOf(existing?.unitType ?: ResourceUnitType.UNIT) }
    var totalText by remember(existing) { mutableStateOf(existing?.totalCount?.toString() ?: "12") }
    var currentText by remember(existing) { mutableStateOf(existing?.currentCount?.toString() ?: "0") }
    var skillTag by remember(existing) { mutableStateOf(existing?.skillTag ?: availableSkillTags.first().name) }
    var validationError by remember { mutableStateOf<String?>(null) }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (existing == null) "Yeni Kaynak Ekle" else "Kaynağı Düzenle",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Kitap / Platform adı *") },
                placeholder = { Text("örn. Main Course Book, Online Practice") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "İlerleme Birimi",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResourceUnitType.entries.forEach { type ->
                        val selected = unitType == type
                        val shape = RoundedCornerShape(14.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(shape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f),
                                    shape = shape
                                )
                                .clickable { unitType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.labelTr,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = currentText,
                    onValueChange = {
                        currentText = it
                        validationError = null
                    },
                    label = { Text("Mevcut ${unitType.labelTr}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = totalText,
                    onValueChange = {
                        totalText = it
                        validationError = null
                    },
                    label = { Text("Toplam ${unitType.labelTr} *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "İlgili Beceri",
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

            validationError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusUrgentRed
                )
            }

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
                        val cleanName = name.trim()
                        if (cleanName.isEmpty()) return@Button

                        val total = totalText.trim().toIntOrNull()
                        val current = currentText.trim().toIntOrNull() ?: 0
                        if (total == null || total <= 0) {
                            validationError = "Toplam sayı 0'dan büyük olmalıdır."
                            return@Button
                        }
                        if (current < 0 || current > total) {
                            validationError = "Mevcut konum 0 ile $total arasında olmalıdır."
                            return@Button
                        }

                        onSave(
                            ResourceEntity(
                                id = existing?.id ?: 0L,
                                name = cleanName,
                                unitType = unitType,
                                totalCount = total,
                                currentCount = current,
                                skillTag = skillTag
                            )
                        )
                    },
                    enabled = name.trim().isNotEmpty(),
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (existing == null) "Kaynağı Kaydet" else "Güncelle")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
