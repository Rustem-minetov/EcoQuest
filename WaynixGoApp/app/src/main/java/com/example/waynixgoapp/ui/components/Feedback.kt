package com.example.waynixgoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waynixgoapp.ui.theme.*

// ─────────────────────────────────────────────
// EMPTY STATE
// ─────────────────────────────────────────────
@Composable
fun EmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSm)
    ) {
        Icon(icon, contentDescription = null, tint = WaynixColors.Border, modifier = Modifier.size(48.dp))
        Text(message, color = WaynixColors.TextGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────
// SECTION HEADER
// ─────────────────────────────────────────────
@Composable
fun SectionHeader(
    leading: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        leading()
        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = WaynixColors.TextGray,
            letterSpacing = 0.5.sp
        )
    }
}

// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────
@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun EmptyStatePreview() {
    EmptyState(icon = Icons.Filled.Search, message = "НЕТ ВОДИТЕЛЕЙ")
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun SectionHeaderPreview() {
    SectionHeader(
        leading = {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(WaynixColors.GreenDot)
            )
        },
        label = "АКТИВНЫЕ"
    )
}
