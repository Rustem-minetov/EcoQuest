package com.example.waynixgoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waynixgoapp.ui.theme.*

// ─────────────────────────────────────────────
// WAYNIX DROPDOWN (стилизованное поле выбора)
// ─────────────────────────────────────────────
@Composable
fun WaynixDropdown(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = value != "(Все)"
    val borderColor = if (isSelected) WaynixColors.Teal.copy(alpha = 0.5f) else WaynixColors.Border
    val iconTint = if (isSelected) WaynixColors.Teal else WaynixColors.TextGray
    val valueColor = if (isSelected) WaynixColors.Teal else WaynixColors.TextMain

    Surface(
        shape = RoundedCornerShape(Dimens.radiusMed),
        color = WaynixColors.White,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(Dimens.iconSm))
            Spacer(Modifier.width(10.dp))
            Text(label, color = WaynixColors.TextGray, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = WaynixColors.TextGray, modifier = Modifier.size(Dimens.iconSm))
        }
    }
}

// ─────────────────────────────────────────────
// PERSON COUNTER
// ─────────────────────────────────────────────
@Composable
fun PersonCounter(
    count: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusMed))
            .border(1.dp, WaynixColors.Border, RoundedCornerShape(Dimens.radiusMed))
            .background(WaynixColors.White)
            .padding(horizontal = Dimens.paddingMd, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        CounterButton(icon = Icons.Filled.Remove, contentDescription = "Уменьшить", onClick = onDecrement)
        Text(
            "$count чел.",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        CounterButton(icon = Icons.Filled.Add, contentDescription = "Увеличить", onClick = onIncrement)
    }
}

// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────
@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun WaynixDropdownPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WaynixDropdown("Откуда?", "(Все)", Icons.Filled.LocationOn, onClick = {})
        WaynixDropdown("Куда?", "Центр", Icons.Filled.Navigation, onClick = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun PersonCounterPreview() {
    PersonCounter(count = 3, onDecrement = {}, onIncrement = {})
}