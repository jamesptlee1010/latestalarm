package com.james.mathwakealarm

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    ALARMS("Alarms", Icons.Outlined.Alarm),
    ROUTINES("Routines", Icons.Outlined.List),
    PROGRESS("Progress", Icons.Outlined.BarChart),
    SETTINGS("Settings", Icons.Outlined.Settings)
}

@Composable
fun TazAlarmApp(appState: AppState) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    var editingAlarmId by rememberSaveable { mutableStateOf<String?>(null) }

    val editAlarm = editingAlarmId?.let(AppRepository::alarm)
    if (editAlarm != null) {
        AlarmEditorDialog(
            alarm = editAlarm,
            onDismiss = { editingAlarmId = null },
            onSave = {
                AppRepository.upsertAlarm(it)
                if (it.enabled) AlarmScheduler.schedule(context, it) else AlarmScheduler.cancel(context, it.id)
                editingAlarmId = null
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                AppTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(item.icon, null) },
                        label = { Text(item.label, maxLines = 1) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab == AppTab.ALARMS) {
                FloatingActionButton(onClick = {
                    val alarm = AppRepository.addAlarm()
                    editingAlarmId = alarm.id
                }) { Icon(Icons.Outlined.Add, "Add alarm") }
            }
        }
    ) { padding ->
        when (tab) {
            AppTab.HOME -> HomeScreen(
                appState = appState,
                padding = padding,
                onEditAlarm = { editingAlarmId = it },
                onAddAlarm = {
                    val alarm = AppRepository.addAlarm()
                    editingAlarmId = alarm.id
                },
                onEditRoutine = { tab = AppTab.ROUTINES },
                onSettings = { tab = AppTab.SETTINGS }
            )
            AppTab.ALARMS -> AlarmsScreen(appState, padding, onEdit = { editingAlarmId = it })
            AppTab.ROUTINES -> RoutinesScreen(appState, padding)
            AppTab.PROGRESS -> ProgressScreen(appState, padding)
            AppTab.SETTINGS -> SettingsScreen(appState, padding)
        }
    }
}

@Composable
private fun ScreenContainer(padding: PaddingValues, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun HomeScreen(
    appState: AppState,
    padding: PaddingValues,
    onEditAlarm: (String) -> Unit,
    onAddAlarm: () -> Unit,
    onEditRoutine: () -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    val now = ZonedDateTime.now()
    val next = appState.alarms.filter { it.enabled && it.days.isNotEmpty() }
        .minByOrNull { AlarmScheduler.nextOccurrence(it, now).toInstant() }
    val nextAt = next?.let { AlarmScheduler.nextOccurrence(it, now) }
    val lastRun = appState.runs.firstOrNull()
    val greeting = when (now.hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    ScreenContainer(padding) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandHeader(Modifier.weight(1f), compact = true)
            IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, "Settings") }
        }
        Column {
            Text("$greeting, ${appState.userName}", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                if (next == null) "Add an alarm to begin." else "Everything looks ready for your next alarm.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (lastRun != null && lastRun.completed && isToday(lastRun.completedAt)) {
            CompletionCard(lastRun)
        }

        if (next != null && nextAt != null) {
            NextAlarmCard(
                alarm = next,
                nextAt = nextAt,
                onEdit = { onEditAlarm(next.id) },
                onTest = {
                    AlarmScheduler.scheduleTest(context, next.id, 5_000L)
                    Toast.makeText(context, "Test alarm scheduled in 5 seconds", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp)) {
                    Text("No active alarms", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Create an alarm and attach a wake-up routine.")
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onAddAlarm) { Icon(Icons.Outlined.Add, null); Text(" Add Alarm") }
                }
            }
        }

        SectionTitle("QUICK ACTIONS")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction(Icons.Outlined.Alarm, "Add Alarm", onAddAlarm, Modifier.weight(1f))
            QuickAction(Icons.Outlined.List, "Edit Routine", onEditRoutine, Modifier.weight(1f))
            QuickAction(Icons.Outlined.PlayArrow, "Test Wake-Up", {
                next?.let {
                    AlarmScheduler.scheduleTest(context, it.id, 5_000L)
                    Toast.makeText(context, "Test alarm scheduled", Toast.LENGTH_SHORT).show()
                }
            }, Modifier.weight(1f))
        }

        SectionTitle("UPCOMING ALARMS")
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            if (appState.alarms.isEmpty()) {
                Text("No alarms created", Modifier.padding(18.dp))
            } else {
                appState.alarms.take(4).forEachIndexed { index, alarm ->
                    AlarmCompactRow(alarm, onEditAlarm)
                    if (index != appState.alarms.take(4).lastIndex) Divider()
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CompletionCard(run: AlarmRun) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF8F0)),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("COMPLETED THIS MORNING", color = TazGreen, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.CheckCircle, null, tint = TazGreen)
            }
            Text(formatDuration(run.durationSeconds), fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = TazNavy)
            val questions = run.stepResults.sumOf { it.correctAnswers }
            CompletionLine("Questions: $questions correct")
            if (run.stepResults.any { it.type == StepType.BARCODE }) CompletionLine("Barcode scanned successfully")
            if (run.stepResults.any { it.type == StepType.PHOTO }) CompletionLine("Photo verified")
            CompletionLine(if (run.penaltyRouteUsed) "Penalty route completed" else "Penalty route not used")
        }
    }
}

@Composable
private fun CompletionLine(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.CheckCircle, null, tint = TazGreen, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = TazNavy)
    }
}

@Composable
private fun NextAlarmCard(alarm: AlarmConfig, nextAt: ZonedDateTime, onEdit: () -> Unit, onTest: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .72f)),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("NEXT ALARM", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = {
                        AppRepository.toggleAlarm(alarm.id, it)
                        if (it) AlarmScheduler.schedule(context, alarm.copy(enabled = true)) else AlarmScheduler.cancel(context, alarm.id)
                    }
                )
            }
            Text(formatAlarmTime(alarm), fontSize = 46.sp, fontWeight = FontWeight.ExtraBold)
            Text("${relativeDay(nextAt)} • ${alarm.label}", fontWeight = FontWeight.Bold)
            Text("${alarm.routine.size}-step routine • Approximately ${estimateRoutine(alarm.routine)} minutes")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit Alarm") }
                Button(onClick = onTest, modifier = Modifier.weight(1f)) { Text("Test Alarm") }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreVert, "More") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text("Skip next occurrence") },
                            leadingIcon = { Icon(Icons.Outlined.SkipNext, null) },
                            onClick = {
                                val occurrence = AlarmScheduler.nextOccurrence(alarm).toInstant().toEpochMilli()
                                AppRepository.setSkipOccurrence(alarm.id, occurrence)
                                AlarmScheduler.schedule(context, alarm.copy(skipOccurrenceAt = occurrence))
                                menu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate alarm") },
                            leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) },
                            onClick = {
                                val copy = AppRepository.addAlarm(alarm)
                                AlarmScheduler.schedule(context, copy)
                                menu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete alarm", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                AlarmScheduler.cancel(context, alarm.id)
                                AppRepository.deleteAlarm(alarm.id)
                                menu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(18.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(7.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
        }
    }
}

@Composable
private fun AlarmCompactRow(alarm: AlarmConfig, onEdit: (String) -> Unit) {
    val context = LocalContext.current
    Row(
        Modifier.fillMaxWidth().clickable { onEdit(alarm.id) }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(formatAlarmTime(alarm), fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(105.dp))
        Text(daysLabel(alarm.days), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Switch(checked = alarm.enabled, onCheckedChange = {
            AppRepository.toggleAlarm(alarm.id, it)
            if (it) AlarmScheduler.schedule(context, alarm.copy(enabled = true)) else AlarmScheduler.cancel(context, alarm.id)
        })
    }
}

@Composable
private fun AlarmsScreen(appState: AppState, padding: PaddingValues, onEdit: (String) -> Unit) {
    val context = LocalContext.current
    ScreenContainer(padding) {
        PageHeader("Alarms", "Create independent alarms with their own days and wake-up routines.")
        appState.alarms.forEach { alarm ->
            Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(formatAlarmTime(alarm), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                            Text(alarm.label, fontWeight = FontWeight.Bold)
                            Text(daysLabel(alarm.days), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = alarm.enabled, onCheckedChange = {
                            AppRepository.toggleAlarm(alarm.id, it)
                            if (it) AlarmScheduler.schedule(context, alarm.copy(enabled = true)) else AlarmScheduler.cancel(context, alarm.id)
                        })
                    }
                    Text("${alarm.routine.size} steps • ${alarm.sunriseSeconds}-second sunrise")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { onEdit(alarm.id) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.Edit, null); Text(" Edit")
                        }
                        FilledTonalButton(onClick = {
                            AlarmScheduler.scheduleTest(context, alarm.id, 5_000L)
                            Toast.makeText(context, "Test alarm scheduled", Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.PlayArrow, null); Text(" Test")
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(76.dp))
    }
}

@Composable
private fun AlarmEditorDialog(alarm: AlarmConfig, onDismiss: () -> Unit, onSave: (AlarmConfig) -> Unit) {
    var label by remember(alarm.id) { mutableStateOf(alarm.label) }
    var hour by remember(alarm.id) { mutableStateOf(alarm.hour.toString()) }
    var minute by remember(alarm.id) { mutableStateOf(alarm.minute.toString()) }
    var days by remember(alarm.id) { mutableStateOf(alarm.days) }
    var sunrise by remember(alarm.id) { mutableStateOf(alarm.sunriseSeconds.toString()) }
    var vibrate by remember(alarm.id) { mutableStateOf(alarm.vibrate) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Edit Alarm", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                OutlinedTextField(label = { Text("Alarm name") }, value = label, onValueChange = { label = it }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = hour,
                        onValueChange = { hour = it.filter(Char::isDigit).take(2) },
                        label = { Text("Hour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minute,
                        onValueChange = { minute = it.filter(Char::isDigit).take(2) },
                        label = { Text("Minute") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Repeat on", fontWeight = FontWeight.Bold)
                AlarmDaySelector(days) { day -> days = if (day in days) days - day else (days + day).sorted() }
                OutlinedTextField(
                    value = sunrise,
                    onValueChange = { sunrise = it.filter(Char::isDigit).take(3) },
                    label = { Text("Sunrise duration in seconds") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = vibrate, onCheckedChange = { vibrate = it })
                    Text("Vibrate while the alarm is active")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            onSave(
                                alarm.copy(
                                    label = label.ifBlank { "Alarm" },
                                    hour = hour.toIntOrNull()?.coerceIn(0, 23) ?: alarm.hour,
                                    minute = minute.toIntOrNull()?.coerceIn(0, 59) ?: alarm.minute,
                                    days = days.ifEmpty { alarm.days },
                                    sunriseSeconds = sunrise.toIntOrNull()?.coerceIn(15, 600) ?: 60,
                                    vibrate = vibrate
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Save Alarm") }
                }
            }
        }
    }
}

@Composable
private fun AlarmDaySelector(selected: List<Int>, onToggle: (Int) -> Unit) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        labels.forEachIndexed { index, label ->
            FilterChip(
                selected = index + 1 in selected,
                onClick = { onToggle(index + 1) },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RoutinesScreen(appState: AppState, padding: PaddingValues) {
    val context = LocalContext.current
    var selectedId by rememberSaveable { mutableStateOf(appState.alarms.firstOrNull()?.id) }
    val selected = appState.alarms.firstOrNull { it.id == selectedId } ?: appState.alarms.firstOrNull()
    var editingStep by remember { mutableStateOf<RoutineStep?>(null) }
    var captureUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val uri = captureUri?.toString()
            if (uri != null) editingStep = editingStep?.copy(referenceUris = (editingStep!!.referenceUris + uri).distinct().take(5))
        }
    }

    fun persistRoutine(routine: List<RoutineStep>) {
        selected?.let {
            val updated = it.copy(routine = routine)
            AppRepository.upsertAlarm(updated)
            if (updated.enabled) AlarmScheduler.schedule(context, updated)
        }
    }

    if (editingStep != null && selected != null) {
        StepEditorDialog(
            step = editingStep!!,
            onDismiss = { editingStep = null },
            onScan = {
                GmsBarcodeScanning.getClient(context).startScan()
                    .addOnSuccessListener { barcode ->
                        editingStep = editingStep?.copy(barcodeValue = barcode.rawValue.orEmpty(), title = "Scan Barcode")
                    }
                    .addOnFailureListener { Toast.makeText(context, "Scanner could not start", Toast.LENGTH_SHORT).show() }
            },
            onCapture = {
                captureUri = PhotoStore.createCaptureUri(context, "routine")
                captureUri?.let { photoLauncher.launch(it) }
            },
            onSave = { saved ->
                val exists = selected.routine.any { it.id == saved.id }
                persistRoutine(if (exists) selected.routine.map { if (it.id == saved.id) saved else it } else selected.routine + saved)
                editingStep = null
            }
        )
    }

    ScreenContainer(padding) {
        PageHeader("Wake-Up Routines", "Build the exact sequence required to stop each alarm.")
        if (appState.alarms.isEmpty()) {
            Text("Create an alarm first.")
            return@ScreenContainer
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            appState.alarms.forEach { alarm ->
                FilterChip(
                    selected = selected?.id == alarm.id,
                    onClick = { selectedId = alarm.id },
                    label = { Text(alarm.label) }
                )
            }
        }
        selected?.let { alarm ->
            SectionTitle("${alarm.label.uppercase()} • ${alarm.routine.size} STEPS")
            alarm.routine.forEachIndexed { index, step ->
                RoutineStepCard(
                    number = index + 1,
                    step = step,
                    canUp = index > 0,
                    canDown = index < alarm.routine.lastIndex,
                    onEdit = { editingStep = step },
                    onDuplicate = {
                        val list = alarm.routine.toMutableList()
                        list.add(index + 1, step.copy(id = UUID.randomUUID().toString()))
                        persistRoutine(list)
                    },
                    onDelete = { persistRoutine(alarm.routine.filterNot { it.id == step.id }) },
                    onUp = {
                        val list = alarm.routine.toMutableList()
                        val previous = list[index - 1]
                        list[index - 1] = list[index]
                        list[index] = previous
                        persistRoutine(list)
                    },
                    onDown = {
                        val list = alarm.routine.toMutableList()
                        val following = list[index + 1]
                        list[index + 1] = list[index]
                        list[index] = following
                        persistRoutine(list)
                    }
                )
            }
            OutlinedButton(
                onClick = { editingStep = RoutineStep(type = StepType.QUESTIONS, title = "Answer Questions", questionsRequired = 2) },
                modifier = Modifier.fillMaxWidth()
            ) { Icon(Icons.Outlined.Add, null); Text(" Add Step") }

            SectionTitle("ROUTINE PRESETS")
            PresetCard("Quick Start", "2 questions → barcode", "2–3 min") { persistRoutine(quickStartRoutine()) }
            PresetCard("Normal Workday", "2 questions → barcode → 3 questions", "4–6 min") { persistRoutine(normalWorkdayRoutine()) }
            PresetCard("Must Get Up", "questions → barcode → questions → photo", "6–9 min") { persistRoutine(mustGetUpRoutine()) }
            PresetCard("Weekend", "lighter questions → photo", "2–4 min") { persistRoutine(weekendRoutine()) }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun RoutineStepCard(
    number: Int,
    step: RoutineStep,
    canUp: Boolean,
    canDown: Boolean,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit
) {
    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(38.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text(number.toString(), color = Color.White, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(12.dp))
                Icon(stepIcon(step.type), null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (step.type == StepType.BARCODE) "Scan Barcode" else step.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(stepStatus(step), color = if (stepReady(step)) TazGreen else TazAmber, fontSize = 13.sp)
                }
                IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Edit") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(enabled = canUp, onClick = onUp) { Icon(Icons.Outlined.ArrowUpward, "Move up") }
                IconButton(enabled = canDown, onClick = onDown) { Icon(Icons.Outlined.ArrowDownward, "Move down") }
                IconButton(onClick = onDuplicate) { Icon(Icons.Outlined.ContentCopy, "Duplicate") }
                IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun StepEditorDialog(
    step: RoutineStep,
    onDismiss: () -> Unit,
    onScan: () -> Unit,
    onCapture: () -> Unit,
    onSave: (RoutineStep) -> Unit
) {
    var type by remember(step.id) { mutableStateOf(step.type) }
    var title by remember(step.id, step.title) { mutableStateOf(step.title) }
    var count by remember(step.id) { mutableStateOf(step.questionsRequired.coerceAtLeast(2).toString()) }
    var topics by remember(step.id) { mutableStateOf(step.topics) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text("Configure Step", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Text("Step type", fontWeight = FontWeight.Bold)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StepType.entries.forEach { option ->
                        FilterChip(selected = type == option, onClick = { type = option }, label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                if (type != StepType.BARCODE) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Step name") }, modifier = Modifier.fillMaxWidth())
                }
                when (type) {
                    StepType.QUESTIONS -> {
                        OutlinedTextField(
                            value = count,
                            onValueChange = { count = it.filter(Char::isDigit).take(2) },
                            label = { Text("Correct answers required") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Question topics", fontWeight = FontWeight.Bold)
                        Column {
                            Topic.entries.chunked(3).forEach { rowTopics ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    rowTopics.forEach { topic ->
                                        FilterChip(
                                            selected = topic in topics,
                                            onClick = { topics = if (topic in topics) topics - topic else topics + topic },
                                            label = { Text(topic.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(3 - rowTopics.size) { Spacer(Modifier.weight(1f)) }
                                }
                            }
                        }
                    }
                    StepType.BARCODE -> {
                        Text("Scan Barcode", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(if (step.barcodeValue.isBlank()) "No barcode registered" else "Barcode registered", color = if (step.barcodeValue.isBlank()) TazAmber else TazGreen)
                        OutlinedButton(onClick = onScan, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.QrCodeScanner, null)
                            Text(if (step.barcodeValue.isBlank()) " Register Barcode" else " Replace Barcode")
                        }
                        Text("During the alarm the screen will say ‘Scan Barcode’, ‘Barcode must match your saved code’ and ‘Open Scanner’.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StepType.PHOTO -> {
                        Text("${step.referenceUris.size} reference photos", color = if (step.referenceUris.size >= 3) TazGreen else TazAmber)
                        OutlinedButton(onClick = onCapture, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.CameraAlt, null); Text(" Add Live Reference Photo")
                        }
                        Text("Add 3–5 photos from slightly different angles and lighting.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            onSave(
                                step.copy(
                                    type = type,
                                    title = when (type) {
                                        StepType.BARCODE -> "Scan Barcode"
                                        StepType.QUESTIONS -> title.ifBlank { "Answer Questions" }
                                        StepType.PHOTO -> title.ifBlank { "Verify Photo" }
                                    },
                                    questionsRequired = if (type == StepType.QUESTIONS) count.toIntOrNull()?.coerceIn(1, 20) ?: 2 else 0,
                                    topics = if (type == StepType.QUESTIONS) topics.ifEmpty { listOf(Topic.MATHS) } else emptyList()
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Save Step") }
                }
            }
        }
    }
}

@Composable
private fun PresetCard(title: String, description: String, duration: String, onClick: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.RestartAlt, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            Text(duration, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProgressScreen(appState: AppState, padding: PaddingValues) {
    val completed = appState.runs.filter { it.completed }
    val average = completed.map { it.durationSeconds }.average().takeIf { !it.isNaN() }?.toInt() ?: 0
    val penaltyCount = completed.count { it.penaltyRouteUsed }
    val topicTotals = mutableMapOf<Topic, Pair<Int, Int>>()
    completed.flatMap { it.stepResults }.flatMap { it.topicScores }.forEach { score ->
        val existing = topicTotals[score.topic] ?: (0 to 0)
        topicTotals[score.topic] = (existing.first + score.correct) to (existing.second + score.attempted)
    }

    ScreenContainer(padding) {
        PageHeader("Progress", "See which parts of your routine are actually getting you awake.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Completed", completed.size.toString(), Modifier.weight(1f))
            MetricCard("Avg time", formatDuration(average), Modifier.weight(1f))
            MetricCard("Penalties", penaltyCount.toString(), Modifier.weight(1f))
        }
        SectionTitle("RECENT MORNINGS")
        if (completed.isEmpty()) {
            Card(Modifier.fillMaxWidth()) { Text("Your completed alarm history will appear here.", Modifier.padding(18.dp)) }
        } else {
            completed.take(10).forEach { run ->
                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CheckCircle, null, tint = TazGreen)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(run.alarmLabel, fontWeight = FontWeight.Bold)
                            Text(formatDateTime(run.completedAt), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatDuration(run.durationSeconds), fontWeight = FontWeight.ExtraBold)
                            Text("${run.stepResults.size} steps", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        SectionTitle("TOPIC ACCURACY")
        Topic.entries.forEach { topic ->
            val total = topicTotals[topic]
            val percentage = if (total == null || total.second == 0) 0 else total.first * 100 / total.second
            OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(topic.displayName, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text(if (total == null) "No data" else "$percentage%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
        SectionTitle("ALARM RELIABILITY")
        appState.reliabilityEvents.take(12).forEach { event ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.Check, null, tint = TazGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(event.stage, fontWeight = FontWeight.SemiBold)
                    Text(formatDateTime(event.occurredAt), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsScreen(appState: AppState, padding: PaddingValues) {
    val context = LocalContext.current
    var name by remember(appState.userName) { mutableStateOf(appState.userName) }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    val notificationAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    ScreenContainer(padding) {
        PageHeader("Settings", "Personalisation, appearance and Android reliability checks.")
        SectionTitle("PROFILE")
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Your name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Button(onClick = { AppRepository.setName(name) }, modifier = Modifier.fillMaxWidth()) { Text("Save Name") }
            }
        }
        SectionTitle("APPEARANCE")
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                ThemeChoice(ThemeMode.SYSTEM, "Follow phone", Icons.Outlined.Tune, appState.themeMode)
                ThemeChoice(ThemeMode.LIGHT, "Light mode", Icons.Outlined.LightMode, appState.themeMode)
                ThemeChoice(ThemeMode.DARK, "Dark mode", Icons.Outlined.DarkMode, appState.themeMode)
            }
        }
        SectionTitle("ALARM RELIABILITY")
        PermissionCard(
            Icons.Outlined.NotificationsActive,
            "Alarm notifications",
            if (notificationAllowed) "Allowed" else "Permission required",
            notificationAllowed
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        PermissionCard(
            Icons.Outlined.Schedule,
            "Exact alarm timing",
            if (exactAllowed) "Allowed" else "Open Android setting",
            exactAllowed
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
            }
        }
        PermissionCard(
            Icons.Outlined.BatteryChargingFull,
            "Battery optimisation",
            "Request unrestricted background operation",
            false
        ) {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}")))
            }.onFailure {
                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.WbSunny, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Two-minute screen-off test", fontWeight = FontWeight.Bold)
                        Text("Lock the phone after pressing test. The alarm should still open and sound.", fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val alarm = appState.alarms.firstOrNull()
                        if (alarm != null) {
                            AlarmScheduler.scheduleTest(context, alarm.id, 120_000L)
                            Toast.makeText(context, "Test scheduled in 2 minutes — lock the phone", Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = appState.alarms.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Schedule Screen-Off Test") }
            }
        }
        OutlinedButton(onClick = { AlarmScheduler.scheduleAll(context) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.RestartAlt, null); Text(" Reschedule All Alarms")
        }
        Text("TAZALARM v2.1.0", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ThemeChoice(mode: ThemeMode, label: String, icon: ImageVector, current: ThemeMode) {
    Row(Modifier.fillMaxWidth().clickable { AppRepository.setTheme(mode) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null)
        Spacer(Modifier.width(10.dp))
        Text(label, modifier = Modifier.weight(1f))
        RadioButton(selected = current == mode, onClick = { AppRepository.setTheme(mode) })
    }
}

@Composable
private fun PermissionCard(icon: ImageVector, title: String, subtitle: String, ready: Boolean, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(if (ready) Icons.Outlined.CheckCircle else Icons.Outlined.Edit, null, tint = if (ready) TazGreen else TazAmber)
        }
    }
}

@Composable
private fun PageHeader(title: String, subtitle: String) {
    Column {
        BrandHeader(compact = true)
        Spacer(Modifier.height(18.dp))
        Text(title, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun stepIcon(type: StepType): ImageVector = when (type) {
    StepType.QUESTIONS -> Icons.Outlined.QuestionMark
    StepType.BARCODE -> Icons.Outlined.QrCodeScanner
    StepType.PHOTO -> Icons.Outlined.CameraAlt
}

private fun stepReady(step: RoutineStep): Boolean = when (step.type) {
    StepType.QUESTIONS -> step.questionsRequired > 0 && step.topics.isNotEmpty()
    StepType.BARCODE -> step.barcodeValue.isNotBlank()
    StepType.PHOTO -> step.referenceUris.size >= 3
}

private fun stepStatus(step: RoutineStep): String = when (step.type) {
    StepType.QUESTIONS -> "${step.questionsRequired} correct • ${step.topics.joinToString { it.displayName }}"
    StepType.BARCODE -> if (step.barcodeValue.isBlank()) "Barcode not registered" else "Barcode registered • Ready"
    StepType.PHOTO -> "${step.referenceUris.size} reference photos${if (step.referenceUris.size >= 3) " • Ready" else ""}"
}

private fun formatAlarmTime(alarm: AlarmConfig): String {
    val date = ZonedDateTime.now().withHour(alarm.hour).withMinute(alarm.minute)
    return date.format(DateTimeFormatter.ofPattern("h:mm a"))
}

private fun relativeDay(value: ZonedDateTime): String {
    val today = ZonedDateTime.now().toLocalDate()
    return when (value.toLocalDate()) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> value.format(DateTimeFormatter.ofPattern("EEEE"))
    }
}

private fun estimateRoutine(routine: List<RoutineStep>): String {
    val min = routine.sumOf {
        when (it.type) {
            StepType.QUESTIONS -> it.questionsRequired * 10
            StepType.BARCODE -> 35
            StepType.PHOTO -> 40
        }
    } / 60
    val low = min.coerceAtLeast(2)
    return "$low–${low + 3}"
}

private fun formatDuration(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val minutes = safe / 60
    val remainder = safe % 60
    return if (minutes > 0) "${minutes}m ${remainder}s" else "${remainder}s"
}

private fun formatDateTime(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("EEE d MMM, h:mm a"))

private fun isToday(epochMillis: Long): Boolean = Instant.ofEpochMilli(epochMillis)
    .atZone(ZoneId.systemDefault()).toLocalDate() == ZonedDateTime.now().toLocalDate()
