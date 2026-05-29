package com.example.waynixgoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
// COUNTER BUTTON (для PersonCounter)
// ─────────────────────────────────────────────
@Composable
fun CounterButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(WaynixColors.Background)
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = WaynixColors.TextMain,
            modifier = Modifier.size(Dimens.iconSm)
        )
    }
}

// ─────────────────────────────────────────────
// SEGMENTED TAB (для переключателей)
// ─────────────────────────────────────────────
@Composable
fun SegmentedTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .clip(RoundedCornerShape(Dimens.radiusSmall))
            .background(if (selected) WaynixColors.White else Color.Transparent)
            .noRippleClickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            color = if (selected) WaynixColors.TextMain else WaynixColors.TextGray
        )
    }
}

// ─────────────────────────────────────────────
// SWAP BUTTON
// ─────────────────────────────────────────────
@Composable
fun SwapButton(onSwap: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(WaynixColors.Teal.copy(alpha = 0.12f))
                .border(1.dp, WaynixColors.Teal.copy(alpha = 0.3f), CircleShape)
                .noRippleClickable(onClick = onSwap),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.SwapVert,
                contentDescription = "Swap",
                tint = WaynixColors.Teal,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// PROFILE AVATAR BUTTON
// ─────────────────────────────────────────────
@Composable
fun ProfileAvatarButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(Dimens.avatarSize)
            .clip(CircleShape)
            .background(WaynixColors.TextMain)
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Person,
            contentDescription = "Profile",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ─────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────
@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun CounterButtonPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        CounterButton(Icons.Filled.Remove, "Dec", {})
        CounterButton(Icons.Filled.Add, "Inc", {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun SegmentedTabPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SegmentedTab("Пассажир", true, {})
        SegmentedTab("Водитель", false, {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun SwapButtonPreview() {
    SwapButton(onSwap = {})
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ProfileAvatarButtonPreview() {
    ProfileAvatarButton(onClick = {})
}