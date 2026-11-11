package com.studygo.app

import android.os.Bundle
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Colores de StudyGo
private val Fondo = Color(0xFF101827)
private val Azul = Color(0xFF4169E1)
private val AzulClaro = Color(0xFF8EABFF)
private val Tarjeta = Color(0xFF1D2A40)
private val Blanco = Color(0xFFF5F7FF)
private val Gris = Color(0xFFADB8CC)
private val Verde = Color(0xFF54D6A0)

// Modelo de una actividad
data class StudyTask(
    val title: String,
    val subject: String,
    val completed: Boolean = false,
    val dueDateMillis: Long? = null,
    val id: Long = System.nanoTime()
)

// Pantallas de la aplicación
enum class StudyScreen {
    HOME,
    TASKS,
    PROGRESS,
    ADD
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permiso para mostrar notificaciones
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        setContent {
            StudyGoApp()
        }
    }
}

@Composable
fun StudyGoApp() {

    val context = LocalContext.current

    val tasks = remember {
        mutableStateListOf(
            StudyTask("Terminar proyecto", "Programación"),
            StudyTask("Estudiar para examen", "Bases de datos"),
            StudyTask("Realizar investigación", "Desarrollo móvil")
        )
    }

    var currentScreen by remember {
        mutableStateOf(StudyScreen.HOME)
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Azul,
            background = Fondo,
            surface = Tarjeta,
            onPrimary = Blanco,
            onBackground = Blanco,
            onSurface = Blanco
        )
    ) {
        Scaffold(
            containerColor = Fondo
        ) { padding ->

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Fondo)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {

                val contentWidth = maxWidth * 0.78f

                Column(
                    modifier = Modifier
                        .width(contentWidth)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    when (currentScreen) {

                        StudyScreen.HOME -> {
                            HomeScreen(
                                tasks = tasks,
                                onTasksClick = {
                                    currentScreen = StudyScreen.TASKS
                                },
                                onProgressClick = {
                                    currentScreen = StudyScreen.PROGRESS
                                }
                            )
                        }

                        StudyScreen.TASKS -> {
                            TasksScreen(
                                tasks = tasks,
                                onBack = {
                                    currentScreen = StudyScreen.HOME
                                },
                                onAdd = {
                                    currentScreen = StudyScreen.ADD
                                }
                            )
                        }

                        StudyScreen.PROGRESS -> {
                            ProgressScreen(
                                tasks = tasks,
                                onBack = {
                                    currentScreen = StudyScreen.HOME
                                }
                            )
                        }

                        StudyScreen.ADD -> {
                            AddTaskScreen(
                                onBack = {
                                    currentScreen = StudyScreen.TASKS
                                },
                                onSave = { title, subject, dueDate ->

                                    val newTask = StudyTask(
                                        title = title,
                                        subject = subject,
                                        dueDateMillis = dueDate
                                    )

                                    tasks.add(newTask)

                                    scheduleTaskReminder(
                                        context = context,
                                        task = newTask
                                    )

                                    currentScreen = StudyScreen.TASKS
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------
// PANTALLA DE INICIO
// --------------------------------------

@Composable
fun HomeScreen(
    tasks: List<StudyTask>,
    onTasksClick: () -> Unit,
    onProgressClick: () -> Unit
) {

    val pending = tasks.count { !it.completed }
    val completed = tasks.count { it.completed }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "STUDYGO",
        fontSize = 25.sp,
        fontWeight = FontWeight.ExtraBold,
        color = AzulClaro,
        textAlign = TextAlign.Center
    )

    Text(
        text = "Tu espacio de estudio",
        fontSize = 12.sp,
        color = Gris,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Tarjeta
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tu día de estudio",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Blanco
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$pending pendientes",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = AzulClaro
            )

            Text(
                text = "$completed completadas",
                fontSize = 11.sp,
                color = Verde
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    AppButton(
        text = "Mis actividades",
        onClick = onTasksClick
    )

    AppButton(
        text = "Mi progreso",
        onClick = onProgressClick,
        secondary = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Organiza. Estudia. Avanza.",
        fontSize = 10.sp,
        color = Gris,
        textAlign = TextAlign.Center
    )
}

// --------------------------------------
// PANTALLA DE ACTIVIDADES
// --------------------------------------

@Composable
fun TasksScreen(
    tasks: SnapshotStateList<StudyTask>,
    onBack: () -> Unit,
    onAdd: () -> Unit
) {

    Text(
        text = "MIS ACTIVIDADES",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = AzulClaro,
        textAlign = TextAlign.Center
    )

    Text(
        text = "${tasks.count { !it.completed }} pendientes",
        fontSize = 11.sp,
        color = Gris
    )

    Spacer(modifier = Modifier.height(4.dp))

    if (tasks.isEmpty()) {
        Text(
            text = "Todavía no tienes actividades",
            fontSize = 12.sp,
            color = Gris,
            textAlign = TextAlign.Center
        )
    } else {

        tasks.forEachIndexed { index, task ->

            TaskCard(
                task = task,
                onComplete = { checked ->
                    tasks[index] = task.copy(
                        completed = checked
                    )
                },
                onDelete = {
                    tasks.remove(task)
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    AppButton(
        text = "+ Agregar tarea",
        onClick = onAdd
    )

    AppButton(
        text = "Volver al inicio",
        onClick = onBack,
        secondary = true
    )
}

// --------------------------------------
// TARJETA INDIVIDUAL DE CADA TAREA
// --------------------------------------

@Composable
fun TaskCard(
    task: StudyTask,
    onComplete: (Boolean) -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Tarjeta
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = if (task.completed) "✓ COMPLETADA" else "○ PENDIENTE",
                color = if (task.completed) Verde else AzulClaro,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = task.title,
                color = if (task.completed) Gris else Blanco,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = task.subject,
                color = Gris,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            task.dueDateMillis?.let { date ->
                Text(
                    text = "Entrega: ${
                        SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(Date(date))
                    }",
                    color = AzulClaro,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = if (task.completed) "Desmarcar" else "Completar",
                    color = AzulClaro,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clickable {
                            onComplete(!task.completed)
                        }
                        .padding(5.dp)
                )

                Text(
                    text = "Eliminar",
                    color = Color(0xFFFF8585),
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clickable {
                            onDelete()
                        }
                        .padding(5.dp)
                )
            }
        }
    }
}

// --------------------------------------
// PANTALLA DE PROGRESO
// --------------------------------------

@Composable
fun ProgressScreen(
    tasks: List<StudyTask>,
    onBack: () -> Unit
) {

    val total = tasks.size
    val completed = tasks.count { it.completed }
    val pending = total - completed

    Text(
        text = "MI PROGRESO",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = AzulClaro,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(10.dp))

    ProgressCard(
        number = total.toString(),
        label = "Total de tareas",
        color = Blanco
    )

    ProgressCard(
        number = completed.toString(),
        label = "Completadas",
        color = Verde
    )

    ProgressCard(
        number = pending.toString(),
        label = "Pendientes",
        color = AzulClaro
    )

    Spacer(modifier = Modifier.height(8.dp))

    val progress = if (total > 0) {
        completed.toFloat() / total.toFloat()
    } else {
        0f
    }

    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp),
        color = Verde,
        trackColor = Tarjeta
    )

    Text(
        text = "${(progress * 100).toInt()}% completado",
        fontSize = 11.sp,
        color = Gris
    )

    Spacer(modifier = Modifier.height(8.dp))

    AppButton(
        text = "Volver al inicio",
        onClick = onBack,
        secondary = true
    )
}

// --------------------------------------
// TARJETAS DE ESTADÍSTICAS
// --------------------------------------

@Composable
fun ProgressCard(
    number: String,
    label: String,
    color: Color
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Tarjeta
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = label,
                fontSize = 10.sp,
                color = Gris
            )
        }
    }
}

// --------------------------------------
// PANTALLA PARA AGREGAR ACTIVIDADES
// --------------------------------------


@Composable
fun AddTaskScreen(
    onBack: () -> Unit,
    onSave: (String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Nueva tarea",
            color = Blanco,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Materia") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        AppButton(
            text = "Seleccionar fecha",
            onClick = {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val selectedCalendar = Calendar.getInstance()

                        selectedCalendar.set(
                            year, month, day, 20, 0, 0
                        )
                        selectedCalendar.set(Calendar.MILLISECOND, 0)

                        selectedDate = selectedCalendar.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    datePicker.minDate =
                        Calendar.getInstance().timeInMillis
                }.show()
            }
        )

        Text(
            text = selectedDate?.let {
                "Entrega: ${
                    SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                    ).format(Date(it))
                } - 8:00 p. m."
            } ?: "Selecciona una fecha de entrega",
            color = Gris,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )



        AppButton(
            text = "Guardar",
            onClick = {
                selectedDate?.let { date ->
                    onSave(
                        title.ifBlank { "Mi tarea" },
                        subject.ifBlank { "General" },
                        date
                    )
                }
            },
            enabled = selectedDate != null
        )

        AppButton(
            text = "Cancelar",
            onClick = onBack,
            secondary = true
        )
    }
}




@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    secondary: Boolean = false,
    enabled: Boolean = true
) {

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secondary) Tarjeta else Azul,
            contentColor = Blanco,
            disabledContainerColor = Tarjeta,
            disabledContentColor = Gris
        )
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --------------------------------------
// PROGRAMAR RECORDATORIO
// --------------------------------------


fun scheduleTaskReminder(
    context: Context,
    task: StudyTask
) {
    val dueDateMillis = task.dueDateMillis ?: return

    // No programar recordatorios para tareas que ya vencieron
    if (dueDateMillis <= System.currentTimeMillis()) return

    val alarmManager = context.getSystemService(
        Context.ALARM_SERVICE
    ) as AlarmManager

    // Calcular un día antes de la entrega, a las 8:00 p. m.
    val reminderCalendar = Calendar.getInstance().apply {
        timeInMillis = dueDateMillis
        add(Calendar.DAY_OF_YEAR, -1)
        set(Calendar.HOUR_OF_DAY, 20)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    var alarmTime = reminderCalendar.timeInMillis

    // Si ya pasó la hora del recordatorio, avisar cuanto antes
    if (alarmTime <= System.currentTimeMillis()) {
        alarmTime = System.currentTimeMillis() + 5000
    }

    val intent = Intent(
        context,
        TaskReminderReceiver::class.java
    ).apply {
        putExtra("task_title", task.title)
        putExtra("task_subject", task.subject)
        putExtra("task_id", task.id)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        task.id.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
    )

    try {
        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        }
    } catch (e: SecurityException) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmTime,
            pendingIntent
        )
    }
}
