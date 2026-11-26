package com.prepflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prepflow.app.domain.model.HomeworkPriority
import com.prepflow.app.domain.model.HomeworkStatus
import com.prepflow.app.domain.model.SkillTagDefaults
import com.prepflow.app.domain.model.SkillTagOption
import com.prepflow.app.ui.theme.StatusMutedSlate
import com.prepflow.app.ui.theme.StatusProgressBlue
import com.prepflow.app.ui.theme.StatusSuccessGreen
import com.prepflow.app.ui.theme.StatusUrgentRed
import com.prepflow.app.ui.theme.StatusWarningAmber

@Composable
fun PillBadge(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    leadingDot: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(accentColor.copy(alpha = 0.16f))
            .border(1.dp, accentColor.copy(alpha = 0.40f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = accentColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun SkillPillBadge(
    skillTag: String,
    customTagColors: Map<String, Long> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val color = Color(SkillTagDefaults.resolveColorHex(skillTag, customTagColors))
    PillBadge(
        text = skillTag,
        accentColor = color,
        leadingDot = true,
        modifier = modifier
    )
}

@Composable
fun PriorityPillBadge(
    priority: HomeworkPriority,
    modifier: Modifier = Modifier
) {
    val color = when (priority) {
        HomeworkPriority.URGENT -> StatusUrgentRed
        HomeworkPriority.MEDIUM -> StatusWarningAmber
        HomeworkPriority.LOW -> StatusMutedSlate
    }
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.36f), shape)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Flag,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = priority.labelTr,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatusPillBadge(
    status: HomeworkStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (color, icon) = when (status) {
        HomeworkStatus.TODO -> StatusMutedSlate to Icons.Rounded.RadioButtonUnchecked
        HomeworkStatus.IN_PROGRESS -> StatusProgressBlue to Icons.Rounded.PlayCircle
        HomeworkStatus.DONE -> StatusSuccessGreen to Icons.Rounded.CheckCircle
    }
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.42f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = status.labelTr,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SkillTagSelectorRow(
    availableTags: List<SkillTagOption>,
    selectedTag: String,
    onSelectTag: (String) -> Unit,
    onAddCustomTag: ((String, Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        availableTags.forEach { option ->
            val selected = option.name.equals(selectedTag, ignoreCase = true)
            val accent = Color(option.colorHex)
            val shape = RoundedCornerShape(50)
            Row(
                modifier = Modifier
                    .clip(shape)
                    .background(if (selected) accent.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.05f))
                    .border(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) accent else Color.White.copy(alpha = 0.12f),
                        shape = shape
                    )
                    .clickable { onSelectTag(option.name) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (onAddCustomTag != null) {
            val shape = RoundedCornerShape(50)
            Row(
                modifier = Modifier
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), shape)
                    .clickable { showCreateDialog = true }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Özel Etiket Ekle",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Yeni Etiket",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showCreateDialog && onAddCustomTag != null) {
        CreateCustomSkillTagDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, colorHex ->
                onAddCustomTag(name, colorHex)
                onSelectTag(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CreateCustomSkillTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var tagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableLongStateOf(SkillTagDefaults.customColorPalette.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Özel Beceri Etiketi") },
        text = {
            Column {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Etiket adı (örn. Translation)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Renk seç",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SkillTagDefaults.customColorPalette.forEach { hex ->
                        val isSelected = hex == selectedColor
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val clean = tagName.trim()
                    if (clean.isNotEmpty()) {
                        onConfirm(clean, selectedColor)
                    }
                },
                enabled = tagName.trim().isNotEmpty()
            ) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
