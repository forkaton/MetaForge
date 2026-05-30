package com.example.metaforge.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metaforge.ui.theme.MFColors

/**
 * Kartu tool profesional tanpa emoji: chip ikon ber-aksen (kiri-atas),
 * indikator chevron (kanan-atas), judul + subjudul (bawah), border tipis,
 * plus gesture press-scale. Signature identik dengan versi lama (drop-in).
 */
@Composable
fun ToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .pressScale(onClick = onClick)
            .height(124.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MFColors.BgCard)
            .border(1.dp, MFColors.BgElevated, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        // Chip ikon
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MFColors.Accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MFColors.Accent,
                modifier = Modifier.size(22.dp)
            )
        }

        // Indikator navigasi
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MFColors.TextHint,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(18.dp)
        )

        // Teks
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                title,
                color = MFColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                color = MFColors.TextHint,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}