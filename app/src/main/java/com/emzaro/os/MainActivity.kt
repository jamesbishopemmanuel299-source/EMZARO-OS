package com.emzaro.os

import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF070B14)
private val Muted = Color(0xFFA7B4CA)
private val Blue = Color(0xFF5792FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { EmzaroLauncher(this) }
    }
}

private data class InstalledApp(val label: String, val packageName: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmzaroLauncher(activity: ComponentActivity) {
    val prefs = remember { activity.getSharedPreferences("emzaro_launcher", 0) }
    var time by remember { mutableStateOf(clockNow()) }
    var showControl by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var showLabels by remember { mutableStateOf(prefs.getBoolean("labels", true)) }
    var columns by remember { mutableIntStateOf(prefs.getInt("columns", 4).coerceIn(3, 5)) }
    var apps by remember { mutableStateOf(loadApps(activity)) }

    LaunchedEffect(Unit) {
        while (true) {
            time = clockNow()
            delay(30_000)
        }
    }

    val visibleApps = apps.filter { it.label.contains(search, ignoreCase = true) }
    val openSettings: () -> Unit = { showSettings = true }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF172B50), Color(0xFF0B1222), Night))
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
                Row(
                    Modifier.clip(RoundedCornerShape(20.dp)).clickable { showControl = true }.padding(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Wifi, null, tint = Color.White, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.BatteryFull, null, tint = Color.White, modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(3.dp))
                    Icon(Icons.Default.ExpandMore, null, tint = Muted, modifier = Modifier.size(17.dp))
                }
            }
            Spacer(Modifier.height(29.dp))
            Text(
                SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()).uppercase(),
                color = Muted, fontSize = 11.sp, letterSpacing = 1.4.sp
            )
            Text(time, color = Color.White, fontSize = 55.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.height(18.dp))
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
            Spacer(Modifier.height(19.dp))
            Text("YOUR APPS", color = Muted, fontSize = 11.sp, letterSpacing = 1.6.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search installed apps", color = Muted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
                trailingIcon = {
                    if (search.isNotEmpty()) IconButton(onClick = { search = "" }) {
                        Icon(Icons.Default.Close, "Clear search", tint = Muted)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Blue, unfocusedBorderColor = Color(0xFF35445D),
                    cursorColor = Blue
                )
            )
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(visibleApps, key = { it.packageName }) { app ->
                    AppIcon(activity, app, showLabels) {
                        activity.packageManager.getLaunchIntentForPackage(app.packageName)?.let(activity::startActivity)
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp))
                    .background(Color(0xDD202D43)).padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockItem(Icons.Default.Call, "Phone") { launchMatching(activity, apps, "phone") }
                DockItem(Icons.Default.ChatBubble, "Messages") { launchMatching(activity, apps, "message") }
                DockItem(Icons.Default.Language, "Browser") { launchMatching(activity, apps, "browser") }
                DockItem(Icons.Default.Settings, "Settings", openSettings)
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.width(105.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .8f)))
            }
            Spacer(Modifier.height(8.dp))
        }

        if (showControl) {
            ModalBottomSheet(
                onDismissRequest = { showControl = false },
                containerColor = Color(0xFF172235),
                contentColor = Color.White
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
                    Text("Control Centre", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text("Quick access to Android settings", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ControlTile("Internet", Icons.Default.Wifi) {
                            openSystemPanel(activity, "android.settings.panel.action.INTERNET_CONNECTIVITY")
                        }
                        ControlTile("Bluetooth", Icons.Default.Bluetooth) {
                            openSystemPanel(activity, "android.settings.panel.action.BLUETOOTH")
                        }
                        ControlTile("Volume", Icons.Default.VolumeUp) {
                            openSystemPanel(activity, "android.settings.panel.action.VOLUME")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ControlTile("Battery", Icons.Default.BatteryFull) {
                            activity.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                        }
                        ControlTile("Display", Icons.Default.Brightness6) {
                            activity.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                        }
                        ControlTile("Settings", Icons.Default.Settings) {
                            activity.startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Android opens the official system panels. EMZARO OS does not change protected system toggles directly.",
                        color = Muted, fontSize = 11.sp)
                    Spacer(Modifier.height(28.dp))
                }
            }
        }

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                containerColor = Color(0xFF172235),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("EMZARO Settings") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Show app names", fontWeight = FontWeight.Medium)
                                Text("Display labels under app icons", color = Muted, fontSize = 11.sp)
                            }
                            Switch(checked = showLabels, onCheckedChange = {
                                showLabels = it
                                prefs.edit().putBoolean("labels", it).apply()
                            })
                        }
                        Divider(color = Color(0xFF35445D))
                        Text("Home screen columns: $columns", color = Color.White)
                        Slider(
                            value = columns.toFloat(),
                            onValueChange = { columns = it.toInt().coerceIn(3, 5) },
                            valueRange = 3f..5f,
                            steps = 1
                        )
                        Text("Installed apps: ${apps.size}", color = Muted, fontSize = 12.sp)
                        TextButton(onClick = { apps = loadApps(activity); search = "" }) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(7.dp))
                            Text("Refresh app list")
                        }
                        TextButton(onClick = {
                            showSettings = false
                            activity.startActivity(Intent(Settings.ACTION_SETTINGS))
                        }) {
                            Icon(Icons.Default.Settings, null)
                            Spacer(Modifier.width(7.dp))
                            Text("Open Android settings")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        prefs.edit().putInt("columns", columns).apply()
                        showSettings = false
                    }) { Text("Done", color = Blue) }
                }
            )
        }
    }
}

@Composable
private fun AppIcon(activity: ComponentActivity, app: InstalledApp, showLabel: Boolean, onClick: () -> Unit) {
    val iconBitmap = remember(app.packageName) {
        runCatching {
            val drawable = activity.packageManager.getApplicationIcon(app.packageName)
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        }.getOrNull()
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            Modifier.size(57.dp).clip(RoundedCornerShape(17.dp)).background(Color(0xFF26354D)),
            contentAlignment = Alignment.Center
        ) {
            if (iconBitmap != null) {
                Image(iconBitmap, contentDescription = app.label, modifier = Modifier.fillMaxSize().padding(3.dp))
            } else {
                Text(app.label.take(1).uppercase(), color = Color.White, fontSize = 24.sp)
            }
        }
        if (showLabel) {
            Spacer(Modifier.height(5.dp))
            Text(app.label, color = Color.White, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
private fun DockItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(25.dp))
    }
}

@Composable
private fun RowScope.ControlTile(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(Color(0xFF2A3B56))
            .clickable(onClick = onClick).padding(vertical = 15.dp, horizontal = 6.dp),
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
        .filterNot { it.packageName == activity.packageName }
        .sortedBy { it.label.lowercase(Locale.getDefault()) }
}

private fun launchMatching(activity: ComponentActivity, apps: List<InstalledApp>, term: String) {
    val app = apps.firstOrNull { it.label.contains(term, ignoreCase = true) } ?: return
    activity.packageManager.getLaunchIntentForPackage(app.packageName)?.let(activity::startActivity)
}

private fun openSystemPanel(activity: ComponentActivity, action: String) {
    runCatching { activity.startActivity(Intent(action)) }
        .onFailure { activity.startActivity(Intent(Settings.ACTION_SETTINGS)) }
}

private fun clockNow(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
