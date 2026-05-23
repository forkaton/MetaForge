package com.example.metaforge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.metaforge.core.connectivity.ConnectivityObserver
import com.example.metaforge.data.local.datastore.ThemePreferences
import com.example.metaforge.domain.repository.DraftRepository
import com.example.metaforge.presentation.navigation.AppNavHost
import com.example.metaforge.presentation.theme.MetaForgeTheme
import com.example.metaforge.ui.theme.MFColors
import com.example.metaforge.ui.theme.MFThemeState
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinContext {
        AppContent()
    }
}

@Composable
private fun AppContent() {
    val themePrefs: ThemePreferences       = koinInject()
    val connectivity: ConnectivityObserver = koinInject()
    val draftRepo: DraftRepository         = koinInject()

    val isDark      by themePrefs.isDarkTheme().collectAsStateWithLifecycle(initialValue = true)
    val isConnected by connectivity.isConnected.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var isDismissed by remember { mutableStateOf(false) }

    // Re-show banner whenever connectivity drops
    LaunchedEffect(isConnected) {
        if (!isConnected) isDismissed = false
    }

    SideEffect { MFThemeState.isDark = isDark }

    LaunchedEffect(Unit) {
        scope.launch {
            try { draftRepo.syncHeroes() }
            catch (_: Exception) {}
        }
    }

    MetaForgeTheme(isDark = isDark) {
        Box(Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            AppNavHost(navController = navController)

            AnimatedVisibility(
                visible = !isConnected && !isDismissed,
                modifier = Modifier.align(Alignment.TopCenter),
                enter = expandVertically(),
                exit  = shrinkVertically()
            ) {
                OfflineBanner(onDismiss = { isDismissed = true })
            }
        }
    }
}

@Composable
private fun OfflineBanner(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MFColors.BanRed)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.WifiOff,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Not connected to internet",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    "Can't fetch the latest data. The data used might be outdated.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
