package com.example.waynixgoapp.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waynixgoapp.data.Ride
import com.example.waynixgoapp.ui.theme.WaynixColors

@Composable
fun RideCard(ride: Ride) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = WaynixColors.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Аватарка-заглушка
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(WaynixColors.Yellow.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFFB8860B))
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        ride.driverName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = WaynixColors.TextMain
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = WaynixColors.Yellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${"%.1f".format(ride.rating)} • ${ride.carModel.ifEmpty { "Авто" }}",
                            fontSize = 12.sp,
                            color = WaynixColors.TextGray
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${ride.pricePerSeat} сум",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = WaynixColors.Teal
                    )
                    Text(
                        "мест: ${ride.availableSeats}",
                        fontSize = 11.sp,
                        color = WaynixColors.TextGray
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Маршрут
            Surface(
                color = WaynixColors.Background,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        Icons.Filled.LocationOn, 
                        contentDescription = null, 
                        tint = WaynixColors.Teal, 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        ride.from, 
                        fontSize = 15.sp, 
                        fontWeight = FontWeight.Medium,
                        color = WaynixColors.TextMain
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Filled.ArrowForward, 
                        contentDescription = null, 
                        tint = WaynixColors.Border, 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        ride.to, 
                        fontSize = 15.sp, 
                        fontWeight = FontWeight.Medium,
                        color = WaynixColors.TextMain
                    )
                }
            }

            if (ride.comment.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    ride.comment,
                    fontSize = 13.sp,
                    color = WaynixColors.TextGray,
                    lineHeight = 18.sp
                )
            }

            // Время отправления
            if (ride.departureTime.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = WaynixColors.TextGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    // Показываем время в кратком виде
                    val timeDisplay = try {
                        val parts = ride.departureTime.split("T")
                        if (parts.size >= 2) {
                            val datePart = parts[0].split("-")
                            val timePart = parts[1].take(5) // HH:mm
                            "${datePart.getOrElse(2) { "" }}.${datePart.getOrElse(1) { "" }} $timePart"
                        } else ride.departureTime
                    } catch (_: Exception) { ride.departureTime }

                    Text(
                        timeDisplay,
                        fontSize = 12.sp,
                        color = WaynixColors.TextGray
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Кнопки действия
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Позвонить
                Button(
                    onClick = {
                        val phone = ride.phoneNumber.replace(" ", "").replace("-", "")
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WaynixColors.Teal,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        Icons.Filled.Phone, 
                        contentDescription = null, 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "СВЯЗАТЬСЯ С ВОДИТЕЛЕМ", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}