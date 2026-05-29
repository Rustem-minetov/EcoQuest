package com.example.waynixgoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waynixgoapp.ui.theme.*

// ─────────────────────────────────────────────
// TOP APP BAR
// ─────────────────────────────────────────────
@Composable
fun WaynixTopBar(
    trailingContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    title: String = ""
) {
    Surface(
        color = WaynixColors.White,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingMd, vertical = Dimens.paddingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WaynixLogo()
            if (title.isNotEmpty()) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = WaynixColors.TextMain)
            }
            trailingContent?.invoke()
        }
    }
}

// ─────────────────────────────────────────────
// LOGO
// ─────────────────────────────────────────────
@Composable
fun WaynixLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier
                .size(Dimens.logoSize)
                .clip(CircleShape)
                .background(WaynixColors.Yellow),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Navigation,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(Dimens.iconSm)
            )
        }
        Text(
            "WAYNIX GO",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            letterSpacing = 1.sp,
            color = WaynixColors.TextMain
        )
    }
}

// ─────────────────────────────────────────────
// DAY TAB ROW
// ─────────────────────────────────────────────
@Composable
fun DayTabRow(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val strings = LocalWaynixStrings.current
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(WaynixColors.Border)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        listOf(strings.now, strings.later).forEachIndexed { idx, label ->
            SegmentedTab(
                label = label,
                selected = selected == idx,
                onClick = { onSelect(idx) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────
@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun WaynixTopBarPreview() {
    WaynixTopBar(
        trailingContent = {
            ProfileAvatarButton(onClick = {})
        },
    )
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun WaynixLogoPreview() {
    WaynixLogo()
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun DayTabRowPreview() {
    DayTabRow(selected = 0, onSelect = {})
}