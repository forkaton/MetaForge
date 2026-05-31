package com.example.metaforge.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metaforge.ui.theme.MFColors
import org.jetbrains.compose.resources.painterResource
import metaforge.composeapp.generated.resources.Res
import metaforge.composeapp.generated.resources.land_of_dawn

/**
 * Banner atmosferik "Land of Dawn".
 * Tanpa teks judul — gambar murni untuk menghidupkan suasana. Hanya ada
 * badge LIVE kecil. Scrim tipis menjaga kedalaman & keterbacaan badge.
 * Semua warna dari token MFColors.
 */
@Composable
fun LandOfDawnBanner(
    modifier: Modifier = Modifier,
    /** Text shown next to the LIVE dot, e.g. "LAST FETCH: 30 May 2026". */
    statusLabel: String = "LAST FETCH: —",
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Image(
            painter = painterResource(Res.drawable.land_of_dawn),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Scrim halus: gelap tipis di atas & bawah, bening di tengah.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to MFColors.Bg.copy(alpha = 0.30f),
                        0.45f to Color.Transparent,
                        1.0f to MFColors.Bg.copy(alpha = 0.55f)
                    )
                )
        )

        // Badge LIVE (pill semi-transparan, kiri-bawah)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MFColors.Bg.copy(alpha = 0.55f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(MFColors.Accent)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                "LIVE • $statusLabel",
                color = MFColors.Accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}