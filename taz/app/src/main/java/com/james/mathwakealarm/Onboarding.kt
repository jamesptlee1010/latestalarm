package com.james.mathwakealarm

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
fun OnboardingScreen(appState: AppState) {
    val context = LocalContext.current
    var page by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf(appState.userName) }
    var alarmLabel by remember { mutableStateOf("Weekday Alarm") }
    var hourText by remember { mutableStateOf("6") }
    var minuteText by remember { mutableStateOf("30") }
    var days by remember { mutableStateOf(listOf(1, 2, 3, 4, 5)) }
    var queuedAlarms by remember { mutableStateOf(emptyList<AlarmConfig>()) }
    var barcode by remember { mutableStateOf("") }
    var referenceUris by remember { mutableStateOf(emptyList<String>()) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingUri?.let { referenceUris = (referenceUris + it.toString()).distinct().take(5) }
            message = "Reference photo added"
        }
    }

    fun scanBarcode() {
        GmsBarcodeScanning.getClient(context).startScan()
            .addOnSuccessListener { result ->
                barcode = result.rawValue.orEmpty()
                message = if (barcode.isBlank()) "No barcode value detected" else "Barcode registered"
            }
            .addOnFailureListener { message = it.localizedMessage ?: "Scanner could not start" }
    }

    fun draftAlarm(): AlarmConfig = defaultAlarm().copy(
        label = alarmLabel.ifBlank { "Alarm" },
        hour = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 6,
        minute = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 30,
        days = days.ifEmpty { listOf(1, 2, 3, 4, 5) }
    )

    Scaffold { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.primaryContainer.copy(alpha = .35f))
                    )
                )
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrandHeader()
                Spacer(Modifier.height(24.dp))
                LinearProgressIndicator(
                    progress = { (page + 1) / 4f },
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(28.dp))

                when (page) {
                    0 -> {
                        Icon(Icons.Outlined.Alarm, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Welcome to TAZALARM", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "A sunrise alarm that makes sure you are genuinely awake.",
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(28.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Your name") },
                            supportingText = { Text("Used in your morning, afternoon or evening greeting") },
                            singleLine = true
                        )
                    }
                    1 -> {
                        Text("Create your first alarm", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "You can add as many independent alarms as you need after setup.",
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))
                        if (queuedAlarms.isNotEmpty()) {
                            Text("${queuedAlarms.size} alarm${if (queuedAlarms.size == 1) "" else "s"} already added", color = TazGreen, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                        }
                        OutlinedTextField(
                            value = alarmLabel,
                            onValueChange = { alarmLabel = it },
                            label = { Text("Alarm name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = hourText,
                                onValueChange = { hourText = it.filter(Char::isDigit).take(2) },
                                label = { Text("Hour") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = minuteText,
                                onValueChange = { minuteText = it.filter(Char::isDigit).take(2) },
                                label = { Text("Minute") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Repeat on", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)
                        DayChipRows(days) { day ->
                            days = if (day in days) days - day else (days + day).sorted()
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                queuedAlarms = queuedAlarms + draftAlarm()
                                alarmLabel = "Alarm ${queuedAlarms.size + 1}"
                                hourText = "7"
                                minuteText = "00"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Alarm, null)
                            Text(" Add This Alarm and Create Another")
                        }
                    }
                    2 -> {
                        Text("Prepare your routine", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "The default routine uses questions, a barcode, more questions and a live photo.",
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(20.dp))
                        SetupCard(
                            icon = Icons.Outlined.QrCodeScanner,
                            title = "Scan Barcode",
                            subtitle = if (barcode.isBlank()) "Not registered yet" else "Barcode registered",
                            ready = barcode.isNotBlank(),
                            button = if (barcode.isBlank()) "Register Barcode" else "Replace Barcode",
                            onClick = ::scanBarcode
                        )
                        Spacer(Modifier.height(12.dp))
                        SetupCard(
                            icon = Icons.Outlined.CameraAlt,
                            title = "Verify Photo",
                            subtitle = "${referenceUris.size} of 5 reference photos added",
                            ready = referenceUris.size >= 3,
                            button = "Add Reference Photo",
                            onClick = {
                                pendingUri = PhotoStore.createCaptureUri(context, "onboarding")
                                pendingUri?.let { photoLauncher.launch(it) }
                            }
                        )
                        if (message.isNotBlank()) {
                            Text(message, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                    else -> {
                        Icon(Icons.Outlined.CheckCircle, null, Modifier.size(58.dp), tint = TazGreen)
                        Spacer(Modifier.height(14.dp))
                        Text("Everything is ready", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "TAZALARM will use a one-minute sunrise, rising alarm volume and your configured routine.",
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(22.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("${queuedAlarms.size + 1} alarm${if (queuedAlarms.isEmpty()) "" else "s"}", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                                (queuedAlarms + draftAlarm()).forEach { alarm ->
                                    Text("${alarm.label}: ${alarm.hour.toString().padStart(2, '0')}:${alarm.minute.toString().padStart(2, '0')} • ${daysLabel(alarm.days)}", fontWeight = FontWeight.SemiBold)
                                }
                                Text("Each starts with the same 4-step routine, which can be customised independently later.")
                                Text(if (barcode.isBlank()) "Barcode: configure later" else "Barcode: ready")
                                Text("Photo references: ${referenceUris.size}")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(34.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (page > 0) {
                        OutlinedButton(onClick = { page-- }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.ArrowBack, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Back")
                        }
                    }
                    Button(
                        onClick = {
                            if (page < 3) {
                                page++
                            } else {
                                val configured = defaultRoutine().map { step ->
                                    when (step.type) {
                                        StepType.BARCODE -> step.copy(barcodeValue = barcode)
                                        StepType.PHOTO -> step.copy(referenceUris = referenceUris)
                                        else -> step
                                    }
                                }
                                val alarms = (queuedAlarms + draftAlarm()).map { it.copy(routine = configured) }
                                AppRepository.completeOnboarding(name, alarms)
                                AlarmScheduler.scheduleAll(context)
                            }
                        },
                        enabled = when (page) {
                            0 -> name.isNotBlank()
                            1 -> days.isNotEmpty()
                            else -> true
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (page == 3) "Finish Setup" else "Continue") }
                }
                Spacer(Modifier.height(26.dp))
            }
        }
    }
}

@Composable
private fun DayChipRows(selected: List<Int>, onToggle: (Int) -> Unit) {
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            labels.take(4).forEachIndexed { index, label ->
                FilterChip(
                    selected = index + 1 in selected,
                    onClick = { onToggle(index + 1) },
                    label = { Text(label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            labels.drop(4).forEachIndexed { index, label ->
                val day = index + 5
                FilterChip(
                    selected = day in selected,
                    onClick = { onToggle(day) },
                    label = { Text(label) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun SetupCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    ready: Boolean,
    button: String,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(subtitle, color = if (ready) TazGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (ready) Icon(Icons.Outlined.CheckCircle, null, tint = TazGreen)
            }
            Spacer(Modifier.height(14.dp))
            OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(button) }
        }
    }
}

fun daysLabel(days: List<Int>): String {
    val sorted = days.distinct().sorted()
    return when (sorted) {
        listOf(1, 2, 3, 4, 5) -> "Mon–Fri"
        listOf(6, 7) -> "Weekend"
        listOf(1, 2, 3, 4, 5, 6, 7) -> "Every day"
        else -> sorted.joinToString(" · ") { listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")[it - 1] }
    }
}
