package com.example.waynixgoapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waynixgoapp.ui.components.*
import com.example.waynixgoapp.ui.theme.*

// ─────────────────────────────────────────────
// MY ADS SCREEN
// ─────────────────────────────────────────────
@Composable
fun MyAdsScreen(onProfileClick: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        WaynixTopBar(trailingContent = { ProfileAvatarButton(onClick = onProfileClick) },)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.paddingMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingMd)
        ) {
            Text(
                "МОИ ОБЪЯВЛЕНИЯ",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = WaynixColors.Teal
            )

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

            EmptyState(icon = Icons.Filled.Search, message = "НЕТ АКТИВНЫХ ЗАПРОСОВ")

            HorizontalDivider(color = WaynixColors.Border)

            SectionHeader(
                leading = {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        tint = WaynixColors.TextGray,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = "ИСТОРИЯ"
            )

            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.radiusMed),
                color = WaynixColors.White,
                border = BorderStroke(1.dp, WaynixColors.Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        tint = WaynixColors.Border,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("ИСТОРИЯ ПУСТА", color = WaynixColors.TextGray, fontSize = 12.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────
@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun MyAdsScreenPreview() {
    MyAdsScreen(onProfileClick = {})
}