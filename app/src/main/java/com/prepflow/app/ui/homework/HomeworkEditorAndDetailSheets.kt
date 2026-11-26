package com.prepflow.app.ui.homework

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.prepflow.app.data.local.entity.HomeworkEntity
import com.prepflow.app.data.local.entity.HomeworkLinkEntity
import com.prepflow.app.data.local.entity.HomeworkWithDetails
import com.prepflow.app.data.local.entity.SubtaskEntity
import com.prepflow.app.domain.model.HomeworkPriority
import com.prepflow.app.domain.model.HomeworkStatus
import com.prepflow.app.domain.model.SkillTagOption
import com.prepflow.app.ui.components.DateTimeUtils
import com.prepflow.app.ui.components.GlassCard
import com.prepflow.app.ui.components.LinkLauncher
import com.prepflow.app.ui.components.PrepDatePickerDialog
import com.prepflow.app.ui.components.PrepTimePickerDialog
import com.prepflow.app.ui.components.PriorityPillBadge
import com.prepflow.app.ui.components.SkillPillBadge
import com.prepflow.app.ui.components.SkillTagSelectorRow
import com.prepflow.app.ui.theme.LocalGlassTokens
import com.prepflow.app.ui.theme.StatusSuccessGreen
import com.prepflow.app.ui.theme.StatusUrgentRed
import java.time.LocalDate
import java.time.LocalTime

private data class DraftSubtask(
    val title: String,
    val isCompleted: Boolean = false
)

private data class DraftLink(
    val label: String,
    val url: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkDetailSheet(
    details: HomeworkWithDetails,
    customTagColors: Map<String, Long>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelectStatus: (HomeworkStatus) -> Unit,
    onToggleSubtask: (Long, Boolean) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val glass = LocalGlassTokens.current
    val hw = details.homework
    val isOverdue = hw.status != HomeworkStatus.DONE && DateTimeUtils.isOverdue(hw.dueDateTime)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = glass.cardFillElevated.copy(alpha = 0.98f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hw.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${DateTimeUtils.formatFullDateTime(hw.dueDateTime)} • ${
                            if (hw.status == HomeworkStatus.DONE) "Tamamlandı"
                            else DateTimeUtils.formatRemainingTime(hw.dueDateTime)
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) StatusUrgentRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Düzenle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Sil",
                            tint = StatusUrgentRed
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkillPillBadge(skillTag = hw.skillTag, customTagColors = customTagColors)
                PriorityPillBadge(priority = hw.priority)
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Durum Akışı",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeworkStatus.entries.forEach { st ->
                        val selected = hw.status == st
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
                                .clickable { onSelectStatus(st) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = st.labelTr,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (hw.description.isNotBlank()) {
                GlassCard(contentPadding = 14.dp) {
                    Text(
                        text = "Notlar / Açıklama",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = hw.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (details.subtasks.isNotEmpty()) {
                GlassCard(contentPadding = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alt Görevler",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${details.completedSubtaskCount}/${details.totalSubtaskCount}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { details.subtaskProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)),
                        color = StatusSuccessGreen,
                        trackColor = Color.White.copy(alpha = 0.08f),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    details.subtasks.forEach { sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onToggleSubtask(sub.id, !sub.isCompleted) }
                                .padding(vertical = 7.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (sub.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (sub.isCompleted) StatusSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = sub.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (sub.isCompleted) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }
                }
            }

            if (details.links.isNotEmpty()) {
                GlassCard(contentPadding = 14.dp) {
                    Text(
                        text = "Bağlantılar ve Kısayollar",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        details.links.forEach { link ->
                            val shape = RoundedCornerShape(14.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f), shape)
                                    .clickable {
                                        LinkLauncher.openUrl(
                                            context = context,
                                            rawUrl = link.url,
                                            onError = onShowMessage
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Link,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = link.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = link.url,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.OpenInNew,
                                    contentDescription = "Aç",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkEditorSheet(
    existing: HomeworkWithDetails?,
    initialDate: LocalDate,
    availableSkillTags: List<SkillTagOption>,
    onAddCustomSkillTag: (String, Long) -> Unit,
    onDismiss: () -> Unit,
    onSave: (HomeworkEntity, List<HomeworkLinkEntity>, List<SubtaskEntity>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val glass = LocalGlassTokens.current

    val initialDateTime = existing?.homework?.dueDateTime?.let {
        DateTimeUtils.epochMillisToLocalDateTime(it)
    }

    var title by remember(existing) { mutableStateOf(existing?.homework?.title.orEmpty()) }
    var description by remember(existing) { mutableStateOf(existing?.homework?.description.orEmpty()) }
    var selectedDate by remember(existing) {
        mutableStateOf(initialDateTime?.toLocalDate() ?: initialDate)
    }
    var selectedTime by remember(existing) {
        mutableStateOf(initialDateTime?.toLocalTime() ?: LocalTime.of(23, 59))
    }
    var selectedSkill by remember(existing) {
        mutableStateOf(existing?.homework?.skillTag ?: availableSkillTags.first().name)
    }
    var selectedPriority by remember(existing) {
        mutableStateOf(existing?.homework?.priority ?: HomeworkPriority.MEDIUM)
    }
    var selectedStatus by remember(existing) {
        mutableStateOf(existing?.homework?.status ?: HomeworkStatus.TODO)
    }

    val draftSubtasks = remember(existing) {
        mutableStateListOf<DraftSubtask>().apply {
            existing?.subtasks?.forEach { add(DraftSubtask(it.title, it.isCompleted)) }
        }
    }
    var newSubtaskTitle by remember { mutableStateOf("") }

    val draftLinks = remember(existing) {
        mutableStateListOf<DraftLink>().apply {
            existing?.links?.forEach { add(DraftLink(it.label, it.url)) }
        }
    }
    var newLinkLabel by remember { mutableStateOf("") }
    var newLinkUrl by remember { mutableStateOf("") }
    var linkError by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

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
                text = if (existing == null) "Yeni Ödev Ekle" else "Ödevi Düzenle",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Ödev başlığı *") },
                placeholder = { Text("örn. Unit 6 Essay Taslağı") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama / Detaylar") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = DateTimeUtils.formatShortDate(
                            DateTimeUtils.localDateTimeToEpochMillis(selectedDate, selectedTime)
                        )
                    )
                }

                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = DateTimeUtils.formatTime(selectedTime.hour, selectedTime.minute))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Beceri Etiketi",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SkillTagSelectorRow(
                    availableTags = availableSkillTags,
                    selectedTag = selectedSkill,
                    onSelectTag = { selectedSkill = it },
                    onAddCustomTag = onAddCustomSkillTag
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Öncelik Seviyesi",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(HomeworkPriority.LOW, HomeworkPriority.MEDIUM, HomeworkPriority.URGENT).forEach { prio ->
                        val isSelected = selectedPriority == prio
                        val shape = RoundedCornerShape(14.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(shape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f),
                                    shape = shape
                                )
                                .clickable { selectedPriority = prio }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prio.labelTr,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Durum",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeworkStatus.entries.forEach { st ->
                        val isSelected = selectedStatus == st
                        val shape = RoundedCornerShape(14.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(shape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.12f),
                                    shape = shape
                                )
                                .clickable { selectedStatus = st }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = st.labelTr,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Alt Görevler (${draftSubtasks.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newSubtaskTitle,
                        onValueChange = { newSubtaskTitle = it },
                        label = { Text("Alt görev ekle") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val clean = newSubtaskTitle.trim()
                            if (clean.isNotEmpty()) {
                                draftSubtasks.add(DraftSubtask(title = clean))
                                newSubtaskTitle = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Alt görev ekle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                draftSubtasks.forEachIndexed { index, sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                draftSubtasks[index] = sub.copy(isCompleted = !sub.isCompleted)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (sub.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (sub.isCompleted) StatusSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sub.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { draftSubtasks.removeAt(index) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Kaldır",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Bağlantılar (${draftLinks.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = newLinkLabel,
                    onValueChange = { newLinkLabel = it },
                    label = { Text("Kısayol adı (örn. Online Portal, PDF)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newLinkUrl,
                        onValueChange = {
                            newLinkUrl = it
                            linkError = null
                        },
                        label = { Text("URL (örn. youtube.com/...)") },
                        singleLine = true,
                        isError = linkError != null,
                        supportingText = linkError?.let { err -> { Text(err) } },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val normalized = LinkLauncher.normalizeUrl(newLinkUrl)
                            if (normalized == null) {
                                linkError = "Geçerli bir web adresi gir"
                            } else {
                                draftLinks.add(
                                    DraftLink(
                                        label = newLinkLabel.trim().ifBlank { "Bağlantı" },
                                        url = normalized
                                    )
                                )
                                newLinkLabel = ""
                                newLinkUrl = ""
                                linkError = null
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Bağlantı ekle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                draftLinks.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { draftLinks.removeAt(index) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Linki sil",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

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

                        val pendingSubtasks = draftSubtasks.toMutableList()
                        if (newSubtaskTitle.isNotBlank()) {
                            pendingSubtasks.add(DraftSubtask(newSubtaskTitle.trim()))
                        }
                        val pendingLinks = draftLinks.toMutableList()
                        if (newLinkUrl.isNotBlank()) {
                            LinkLauncher.normalizeUrl(newLinkUrl)?.let { validUrl ->
                                pendingLinks.add(
                                    DraftLink(
                                        label = newLinkLabel.trim().ifBlank { "Bağlantı" },
                                        url = validUrl
                                    )
                                )
                            }
                        }

                        val dueMillis = DateTimeUtils.localDateTimeToEpochMillis(selectedDate, selectedTime)
                        val entity = HomeworkEntity(
                            id = existing?.homework?.id ?: 0L,
                            title = cleanTitle,
                            description = description.trim(),
                            dueDateTime = dueMillis,
                            skillTag = selectedSkill,
                            priority = selectedPriority,
                            status = selectedStatus,
                            createdAt = existing?.homework?.createdAt ?: System.currentTimeMillis()
                        )
                        val linkEntities = pendingLinks.map {
                            HomeworkLinkEntity(homeworkId = entity.id, label = it.label, url = it.url)
                        }
                        val subtaskEntities = pendingSubtasks.map {
                            SubtaskEntity(homeworkId = entity.id, title = it.title, isCompleted = it.isCompleted)
                        }
                        onSave(entity, linkEntities, subtaskEntities)
                    },
                    enabled = title.trim().isNotEmpty(),
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (existing == null) "Ödevi Kaydet" else "Değişiklikleri Kaydet")
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

    if (showTimePicker) {
        PrepTimePickerDialog(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            title = "Son Teslim Saati",
            onTimeSelected = { h, m -> selectedTime = LocalTime.of(h, m) },
            onDismiss = { showTimePicker = false }
        )
    }
}
