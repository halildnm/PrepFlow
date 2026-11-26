package com.prepflow.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prepflow.app.ui.theme.LocalGlassTokens

@Composable
fun GlassAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalGlassTokens.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glass.canvasDeep)
            .background(
                Brush.radialGradient(
                    colors = listOf(glass.canvasAmbientTop, Color.Transparent),
                    center = Offset(180f, 120f),
                    radius = 950f
                )
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(glass.canvasAmbientBottom, Color.Transparent),
                    center = Offset(980f, 1850f),
                    radius = 1050f
                )
            ),
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    highlighted: Boolean = false,
    accentColor: Color? = null,
    elevated: Boolean = false,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glass = LocalGlassTokens.current
    val fillBase = if (elevated) glass.cardFillElevated else glass.cardFill
    val borderColor = when {
        accentColor != null -> accentColor.copy(alpha = 0.50f)
        highlighted -> glass.cardHighlightBorder
        else -> glass.cardBorder
    }

    val topGlow = accentColor?.copy(alpha = 0.14f)
        ?: if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
        else Color.White.copy(alpha = 0.05f)

    val cardBrush = Brush.linearGradient(
        colors = listOf(
            fillBase,
            fillBase.copy(alpha = (fillBase.alpha * 0.88f).coerceIn(0.45f, 0.95f))
        )
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(cardBrush)
            .background(
                Brush.verticalGradient(
                    colors = listOf(topGlow, Color.Transparent)
                )
            )
            .border(width = 1.dp, color = borderColor, shape = shape)
            .then(clickableModifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun SectionHeaderRow(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GlassFloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(58.dp)
            .shadow(
                elevation = 14.dp,
                shape = CircleShape,
                ambientColor = primary.copy(alpha = 0.45f),
                spotColor = primary.copy(alpha = 0.45f)
            ),
        shape = CircleShape,
        color = primary,
        contentColor = onPrimary,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun EmptyStateGlassCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onActionClick) {
                    Text(text = actionLabel, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
