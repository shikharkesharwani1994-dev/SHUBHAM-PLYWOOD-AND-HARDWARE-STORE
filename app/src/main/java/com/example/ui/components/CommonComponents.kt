package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldContainer
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CoralRedContainer
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenContainer
import com.example.ui.theme.PrimaryBlue

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .testTag("metric_${title.lowercase().replace(" ", "_")}")
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "PAID" -> Triple(EmeraldGreenContainer, EmeraldGreen, "PAID")
        "PENDING" -> Triple(AmberGoldContainer, AmberGold, "PENDING")
        "OVERDUE" -> Triple(CoralRedContainer, CoralRed, "OVERDUE")
        "PARTIAL" -> Triple(Color(0xFFDBEAFE), PrimaryBlue, "PARTIAL")
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("status_badge_$label")
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SyncStatusBadge(syncStatus: String) {
    val isSynced = syncStatus == "SYNCED"
    val bgColor = if (isSynced) Color(0xFFE0F2FE) else Color(0xFFFEF3C7)
    val textColor = if (isSynced) Color(0xFF0369A1) else Color(0xFFB45309)
    val icon = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff
    val label = if (isSynced) "Synced" else "Queued Offline"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RoleBadge(role: UserRole) {
    val (color, text) = when (role) {
        UserRole.ADMIN -> Pair(PrimaryBlue, "Admin")
        UserRole.ACCOUNTANT -> Pair(EmeraldGreen, "CA / Auditor")
        UserRole.MANAGER -> Pair(AmberGold, "Manager")
        UserRole.SALES -> Pair(Color(0xFF8B5CF6), "Sales")
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier.testTag("role_badge")
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun OfflineModeBanner(
    isOnline: Boolean,
    pendingCount: Int,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOnline || pendingCount > 0) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize(),
            color = if (!isOnline) CoralRedContainer else AmberGoldContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (!isOnline) Icons.Default.CloudOff else Icons.Default.Sync,
                        contentDescription = null,
                        tint = if (!isOnline) CoralRed else AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!isOnline) {
                            "Offline Mode Active: Changes cached safely ($pendingCount pending)"
                        } else {
                            "$pendingCount offline changes ready for cloud sync"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (!isOnline) CoralRed else AmberGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isOnline && pendingCount > 0) {
                    Surface(
                        color = AmberGold,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onSyncClick() }
                    ) {
                        Text(
                            text = "Sync Now",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SalesBarChart(
    labels: List<String>,
    values: List<Double>,
    modifier: Modifier = Modifier,
    barColor: Color = EmeraldGreen
) {
    if (values.isEmpty()) return
    val maxValue = values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sales & Revenue Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Peak: ₹${String.format(java.util.Locale.US, "%,.0f", maxValue)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val barWidth = size.width / (values.size * 2f)
                val spacing = size.width / values.size

                for (i in values.indices) {
                    val ratio = (values[i] / maxValue).toFloat().coerceIn(0.05f, 1f)
                    val barHeight = size.height * ratio * 0.85f
                    val x = i * spacing + (spacing - barWidth) / 2f
                    val y = size.height - barHeight

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(barColor, barColor.copy(alpha = 0.5f)),
                            startY = y,
                            endY = size.height
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
