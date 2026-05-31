package com.example.metaforge.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import metaforge.composeapp.generated.resources.Res
import metaforge.composeapp.generated.resources.metaforge_logo
import org.jetbrains.compose.resources.painterResource

/**
 * MetaForge brand logo — renders the same PNG used by the launcher icon
 * (`composeApp/src/icon.png`). Sourced via Compose Resources so the same
 * file ships to Android, iOS, and Desktop without per-platform duplication.
 */
@Composable
fun MetaforgeLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Image(
        painter = painterResource(Res.drawable.metaforge_logo),
        contentDescription = "MetaForge logo",
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size)
    )
}
