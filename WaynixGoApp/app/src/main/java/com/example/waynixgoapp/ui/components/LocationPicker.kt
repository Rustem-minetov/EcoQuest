package com.example.waynixgoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waynixgoapp.data.waynixDistricts
import com.example.waynixgoapp.ui.theme.*

// ─────────────────────────────────────────────
// LOCATION PICKER BOTTOM SHEET
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    title: String,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = WaynixColors.White,
        shape = RoundedCornerShape(topStart = Dimens.radiusXL, topEnd = Dimens.radiusXL),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(WaynixColors.Border)
            )
        }
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = WaynixColors.TextMain,
                modifier = Modifier.padding(horizontal = Dimens.paddingMd, vertical = Dimens.paddingSm)
            )
            HorizontalDivider(color = WaynixColors.Border)
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                items(waynixDistricts, key = { it }) { district ->
                    val isSelected = district == selected
                    Column {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .noRippleClickable {
                                    onSelect(district)
                                    onDismiss()
                                }
                                .background(if (isSelected) WaynixColors.Teal else Color.Transparent)
                                .padding(horizontal = Dimens.paddingMd, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                district,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else WaynixColors.TextMain
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(Dimens.iconSm)
                                )
                            }
                        }
                        if (!isSelected) {
                            HorizontalDivider(
                                color = WaynixColors.Border.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = Dimens.paddingMd)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// PREVIEW (с моковыми данными)
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun LocationPickerSheetPreview() {
    // Для превью используем упрощённый список
    val mockDistricts = listOf("Центр", "Север", "Юг", "Запад")
    ModalBottomSheet(onDismissRequest = {}) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Откуда?", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))
            mockDistricts.forEach { district ->
                Text(district, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}