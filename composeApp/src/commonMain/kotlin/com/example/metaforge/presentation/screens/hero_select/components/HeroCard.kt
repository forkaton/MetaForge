package com.example.metaforge.presentation.screens.hero_select.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.metaforge.domain.model.Hero

@Composable
fun HeroCard(
    hero: Hero,
    isAlreadyPicked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isAlreadyPicked) Color.DarkGray.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (isAlreadyPicked) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable(enabled = !isAlreadyPicked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(if (isAlreadyPicked) Color.Black else Color.Gray))
            Spacer(Modifier.height(4.dp))
            Text(hero.name, style = MaterialTheme.typography.bodySmall, color = if (isAlreadyPicked) Color.Gray else Color.White)
            if (isAlreadyPicked) Text("Picked", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

