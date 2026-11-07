package com.emzaro.os

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Background = Color(0xFF070B14)
private val Muted = Color(0xFFA7B4CA)
private val Blue = Color(0xFF5792FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { EmzaroLauncher(this) }
    }
}

private data class InstalledApp(val label: String, val packageName: String)

@Composable
private fun EmzaroLauncher(activity: ComponentActivity) {
    var time by remember { mutableStateOf(now()) }
    var showControl by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf(loadApps(activity)) }

    LaunchedEffect(Unit) {
        while (true) {
            time = now()
            delay(30_000)
        }
    }

    val visibleApps = apps.filter { it.label.contains(search, ignoreCase = true) }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF172B50), Color(0xFF0B1222), Background))
        )
    ) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("EMZARO", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, null, tint = Color.White, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.BatteryFull, null, tint = Color.White, modifier = Modifier.size(19.dp))
                }
            }
            Spacer(Modifier.height(32.dp))
            Text(SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()).uppercase(),
                color = Muted, fontSize = 11.sp, letterSpacing = 1.4.sp)
            Text(time, color = Color.White, fontSize = 55.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF2C4D83), Color(0xFF1A2943))))
                    .padding(17.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("EMZARO OS", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text("Your phone. Your system.", color = Muted, fontSize = 12.sp)
                }
                Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text("YOUR APPS", color = Muted, fontSize = 11.sp, letterSpacing = 1.6.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search installed apps", color = Muted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Blue, unfocusedBorderColor = Color(0xFF35445D),
                    cursorColor = Blue
                )
            )
            Spacer(Modifier.height(14.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(visibleApps, key = { it.packageName }) { app ->
                    AppIcon(app) {
                        val launch = activity.packageManager.getLaunchIntentForPackage(app.packageName)
                        if (launch != null) activity.startActivity(launch)
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp))
                    .background(Color(0xDD202D43)).padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockItem(Icons.Default.Call, "Phone") { openByName(activity, apps, "phone") }
                DockItem(Icons.Default.ChatBubble, "Messages") { openByName(activity, apps, "message") }
                DockItem(Icons.Default.Language, "Browser") { openByName(activity, apps, "browser") }
                DockItem(Icons.Default.Settings, "Settings") {
                    activity.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.width(105.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .8f)))
            }
            Spacer(Modifier.height(8.dp))
        }

        Box(
            Modifier.align(Alignment.TopEnd).statusBarsPadding()
                .size(width = 120.dp, height = 48.dp).clickable { showControl = true }
        )

        if (showControl) {
            ModalBottomSheet(
                onDismissRequest = { showControl = false },
                containerColor = Color(0xFF172235),
                contentColor = Color.White
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Text("Control Centre", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ControlTile("Wi-Fi", Icons.Default.Wifi)
                        ControlTile("Bluetooth", Icons.Default.Bluetooth)
                        ControlTile("Airplane", Icons.Default.Flight)
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("Prototype controls are visual shortcuts; Android system toggles are not modified yet.",
                        color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun AppIcon(app: InstalledApp, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            Modifier.size(57.dp).clip(RoundedCornerShape(17.dp)).background(Color(0xFF26354D)),
            contentAlignment = Alignment.Center
        ) {
            Text(app.label.take(1).uppercase(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(6.dp))
        Text(app.label, color = Color.White, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun DockItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(25.dp))
    }
}

@Composable
private fun RowScope.ControlTile(label: String, icon: ImageVector) {
    Column(
        Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(Color(0xFF2A3B56)).padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = Color.White)
        Spacer(Modifier.height(7.dp))
        Text(label, color = Color.White, fontSize = 11.sp)
    }
}

private fun loadApps(activity: ComponentActivity): List<InstalledApp> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return activity.packageManager.queryIntentActivities(intent, 0)
        .map { InstalledApp(it.loadLabel(activity.packageManager).toString(), it.activityInfo.packageName) }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase(Locale.getDefault()) }
}

private fun openByName(activity: ComponentActivity, apps: List<InstalledApp>, term: String) {
    val app = apps.firstOrNull { it.label.contains(term, ignoreCase = true) } ?: return
    activity.packageManager.getLaunchIntentForPackage(app.packageName)?.let(activity::startActivity)
}

private fun now(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
