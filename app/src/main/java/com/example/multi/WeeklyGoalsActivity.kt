package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import com.example.multi.util.GoalCelebrationPrefs
import com.example.multi.util.MindsetPrefs
import com.example.multi.util.capitalizeSentences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.concurrent.TimeUnit

// Konfetti (Compose) ✨
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Rotation
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size

const val EXTRA_GOAL_ID = "extra_goal_id"

class WeeklyGoalsActivity : SegmentActivity("Weekly Goals") {
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    override fun SegmentContent() {
        val goalId = intent.getLongExtra(EXTRA_GOAL_ID, -1L)
        WeeklyGoalsScreen(highlightGoalId = goalId.takeIf { it > 0 })
    }
}

@Composable
private fun DayChoiceDialog(
    onDismiss: () -> Unit,
    onMiss: () -> Unit,
    onComplete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onComplete()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Icon(Icons.Default.Check, contentDescription = null) }
        },
        dismissButton = {
            Button(
                onClick = {
                    onMiss()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Icon(Icons.Default.Close, contentDescription = null) }
        },
        title = { Text("Mark Day") },
        text = { Text("Choose status") }
    )
}

@Composable
private fun MindsetEntryDialog(
    text: String,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Mindset Focus") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("What focus would you like to highlight this week?")
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = { Text("Focus name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = text.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeeklyGoalsScreen(highlightGoalId: Long? = null) {
    val context = LocalContext.current
    val goals = remember { mutableStateListOf<WeeklyGoal>() }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedGoalIndex by remember { mutableStateOf<Int?>(null) }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    var showConfetti by remember { mutableStateOf(false) }
    var showAllDialog by remember { mutableStateOf(false) }
    var edgeCelebration by remember { mutableStateOf(false) }
    var isMindsetExpanded by remember { mutableStateOf(false) }
    var showMindsetDialog by remember { mutableStateOf(false) }
    var newMindsetText by remember { mutableStateOf("") }
    val mindsetFocuses = remember {
        mutableStateListOf("Workout", "Study", "Read")
    }

    LaunchedEffect(Unit) { edgeCelebration = GoalCelebrationPrefs.isActive(context) }
    LaunchedEffect(Unit) { isMindsetExpanded = MindsetPrefs.isExpanded(context) }

    // Auto stop confetti after 3s
    LaunchedEffect(showConfetti) {
        if (showConfetti) {
            kotlinx.coroutines.delay(3000)
            showConfetti = false
        }
    }

    // Load + roll week + optionally highlight a goal
    LaunchedEffect(highlightGoalId) {
        val db = EventDatabase.getInstance(context)
        val dao = db.weeklyGoalDao()
        val recordDao = db.weeklyGoalRecordDao()
        val stored = withContext(Dispatchers.IO) { dao.getGoals() }
        val currentWeek = currentWeek()
        val today = LocalDate.now()
        val startCurrent = today.minusDays((today.dayOfWeek.value % 7).toLong())
        val prevStart = startCurrent.minusDays(7)
        val prevEnd = startCurrent.minusDays(1)
        val prevStartStr = prevStart.toString()
        val prevEndStr = prevEnd.toString()

        goals.clear()
        stored.forEach { entity ->
            var model = entity.toModel()
            if (model.weekNumber != currentWeek) {
                val completed = model.dayStates.count { it == 'C' }
                val record = WeeklyGoalRecord(
                    header = model.header,
                    completed = completed,
                    frequency = model.frequency,
                    weekStart = prevStartStr,
                    weekEnd = prevEndStr,
                    dayStates = model.dayStates,
                    overageCount = overageCount(completed, model.frequency)
                )
                withContext(Dispatchers.IO) { recordDao.insert(record.toEntity()) }
                model = model.copy(
                    remaining = model.frequency,
                    weekNumber = currentWeek,
                    lastCheckedDate = null,
                    dayStates = DEFAULT_DAY_STATES
                )
                withContext(Dispatchers.IO) { dao.update(model.toEntity()) }
            }
            val completed = model.dayStates.count { it == 'C' }
            model = model.copy(remaining = (model.frequency - completed).coerceAtLeast(0))
            goals.add(model)
        }
        highlightGoalId?.let { id ->
            val index = goals.indexOfFirst { it.id == id }
            if (index >= 0) editingIndex = index
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (edgeCelebration) {
            EdgeCelebrationOverlay(modifier = Modifier.matchParentSize())
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ElevatedButton(
                    onClick = {
                        context.startActivity(
                            android.content.Intent(context, RecordActivity::class.java)
                        )
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .defaultMinSize(minWidth = 170.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Record", fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            val remaining = daysRemainingInWeek()
            Text(
                text = "$remaining days remaining",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            if (showMindsetDialog) {
                MindsetEntryDialog(
                    text = newMindsetText,
                    onTextChange = { newMindsetText = it },
                    onDismiss = {
                        showMindsetDialog = false
                        newMindsetText = ""
                    },
                    onConfirm = {
                        val formatted = newMindsetText.trim()
                        if (formatted.isNotEmpty()) {
                            mindsetFocuses.add(formatted.capitalizeSentences())
                        }
                        showMindsetDialog = false
                        newMindsetText = ""
                    }
                )
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val newState = !isMindsetExpanded
                                    isMindsetExpanded = newState
                                    MindsetPrefs.setExpanded(context, newState)
                                }
                        ) {
                            Text(
                                text = "Mindset",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isMindsetExpanded) {
                                    "Keep your focus front and center."
                                } else {
                                    "Tap to view this week's inspiration."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = { showMindsetDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add mindset focus"
                            )
                        }
                        IconButton(
                            onClick = {
                                val newState = !isMindsetExpanded
                                isMindsetExpanded = newState
                                MindsetPrefs.setExpanded(context, newState)
                            }
                        ) {
                            Icon(
                                imageVector = if (isMindsetExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isMindsetExpanded) "Collapse mindset" else "Expand mindset"
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isMindsetExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Loving Jesus",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                mindsetFocuses.forEach { focus ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            modifier = Modifier
                                                .size(32.dp),
                                            shape = MaterialTheme.shapes.small,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = focus,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "Weekly goal",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                itemsIndexed(goals) { index, goal ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingIndex = index }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (goal.remaining == 0) {
                                Text(
                                    text = "Completed!",
                                    color = Color(0xFF43A047),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(goal.header, style = MaterialTheme.typography.bodyLarge)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.semantics {
                                        val completed = goal.dayStates.count { it == 'C' }
                                        val over = overageCount(completed, goal.frequency)
                                        contentDescription = "$completed of ${goal.frequency} completed" +
                                            if (over > 0) ", $over over target" else ""
                                    }
                                ) {
                                    val completed = goal.dayStates.count { it == 'C' }
                                    val over = overageCount(completed, goal.frequency)
                                    Text(
                                        text = "$completed/${goal.frequency}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (over > 0) {
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "+$over",
                                            color = MaterialTheme.colorScheme.tertiary,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    val today = LocalDate.now().toString()
                                    if (goal.lastCheckedDate != today && completed < goal.frequency + 20) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = Color(0xFF43A047),
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .clickable {
                                                    val chars = goal.dayStates.toCharArray()
                                                    val dIndex = LocalDate.now().dayOfWeek.value % 7
                                                    if (chars[dIndex] != 'C') {
                                                        chars[dIndex] = 'C'
                                                        val newCompleted = chars.count { it == 'C' }
                                                        val updated = goal.copy(
                                                            dayStates = String(chars),
                                                            remaining = (goal.frequency - newCompleted).coerceAtLeast(0),
                                                            lastCheckedDate = today
                                                        )
                                                        val wasIncomplete = goal.remaining > 0
                                                        goals[index] = updated
                                                        if (wasIncomplete && updated.remaining == 0) {
                                                            showConfetti = true
                                                            scope.launch { snackbarHostState.showSnackbar("Goal completed!") }
                                                        }
                                                        if (goals.all { it.remaining == 0 }) {
                                                            GoalCelebrationPrefs.activateForCurrentWeek(context)
                                                            showAllDialog = true
                                                            edgeCelebration = true
                                                        }
                                                        scope.launch {
                                                            saveGoalCompletion(
                                                                context = context,
                                                                goalId = goal.id,
                                                                goalHeader = goal.header,
                                                                completionDate = LocalDate.now()
                                                            )
                                                            val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                                                            withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                                                        }
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                            val progress = (goal.frequency - goal.remaining).toFloat() / goal.frequency
                            LinearProgressIndicator(
                                progress = progress,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            DayButtonsRow(states = goal.dayStates) { dayIndex ->
                                selectedGoalIndex = index
                                selectedDayIndex = dayIndex
                            }
                        }
                    }
                }
            }
        }

        // Empty state animation stays
        if (goals.isEmpty()) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.making))
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(200.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "No goals yet",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray, fontSize = 18.sp)
                )
            }
        }

        // Bottom action bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = { editingIndex = -1 },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Goal") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
            ExtendedFloatingActionButton(
                onClick = {
                    context.startActivity(
                        android.content.Intent(context, WeeklyGoalsCalendarActivity::class.java)
                    )
                },
                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                text = { Text("Calendar") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        // Goal editor dialog
        val index = editingIndex
        if (index != null) {
            val isNew = index < 0
            val goal = if (isNew) WeeklyGoal(header = "", frequency = 1) else goals[index]
            WeeklyGoalDialog(
                initial = goal,
                onDismiss = { editingIndex = null },
                onSave = { header, freq ->
                    editingIndex = null
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                        if (isNew) {
                            val id = withContext(Dispatchers.IO) {
                                dao.insert(WeeklyGoal(header = header, frequency = freq).toEntity())
                            }
                            goals.add(WeeklyGoal(id, header, freq))
                            snackbarHostState.showSnackbar("New Weekly Activity added")
                        } else {
                            val updated = goal.copy(header = header, frequency = freq)
                            goals[index] = updated
                            withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                        }
                    }
                },
                onDelete = if (isNew) null else {
                    {
                        scope.launch {
                            val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                            withContext(Dispatchers.IO) { dao.delete(goal.toEntity()) }
                            goals.removeAt(index)
                            editingIndex = null
                        }
                    }
                },
                onProgress = if (isNew) null else {
                    {
                        val g = goals[index]
                        if (g.remaining > 0) {
                            val updated = g.copy(remaining = g.remaining - 1)
                            goals[index] = updated
                            if (updated.remaining == 0) {
                                showConfetti = true
                                scope.launch { snackbarHostState.showSnackbar("Goal completed!") }
                            }
                            if (goals.all { it.remaining == 0 }) {
                                GoalCelebrationPrefs.activateForCurrentWeek(context)
                                showAllDialog = true
                                edgeCelebration = true
                            }
                            scope.launch {
                                saveGoalCompletion(
                                    context = context,
                                    goalId = g.id,
                                    goalHeader = g.header,
                                    completionDate = LocalDate.now()
                                )
                                val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                                withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                            }
                        }
                    }
                }
            )
        }

        // Day status chooser
        val gIndex = selectedGoalIndex
        val dIndex = selectedDayIndex
        if (gIndex != null && dIndex != null) {
            DayChoiceDialog(
                onDismiss = { selectedGoalIndex = null; selectedDayIndex = null },
                onMiss = {
                    val g = goals[gIndex]
                    val chars = g.dayStates.toCharArray()
                    val wasComplete = chars[dIndex] == 'C'
                    chars[dIndex] = 'M'
                    val completed = chars.count { it == 'C' }
                    val updated = g.copy(dayStates = String(chars), remaining = (g.frequency - completed).coerceAtLeast(0))
                    goals[gIndex] = updated
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                        withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                        if (wasComplete) {
                            val today = LocalDate.now()
                            val startOfWeek = today.minusDays((today.dayOfWeek.value % 7).toLong())
                            val date = startOfWeek.plusDays(dIndex.toLong())
                            removeGoalCompletion(context, g.id, date)
                        }
                    }
                },
                onComplete = {
                    val g = goals[gIndex]
                    val chars = g.dayStates.toCharArray()
                    val alreadyComplete = chars[dIndex] == 'C'
                    chars[dIndex] = 'C'
                    val completed = chars.count { it == 'C' }
                    val updated = g.copy(
                        dayStates = String(chars),
                        remaining = (g.frequency - completed).coerceAtLeast(0),
                        lastCheckedDate = LocalDate.now().toString()
                    )
                    val wasIncomplete = g.remaining > 0
                    goals[gIndex] = updated
                    if (wasIncomplete && updated.remaining == 0) {
                        showConfetti = true
                        scope.launch { snackbarHostState.showSnackbar("Goal completed!") }
                    }
                    if (goals.all { it.remaining == 0 }) {
                        GoalCelebrationPrefs.activateForCurrentWeek(context)
                        showAllDialog = true
                        edgeCelebration = true
                    }
                    scope.launch {
                        val today = LocalDate.now()
                        val startOfWeek = today.minusDays((today.dayOfWeek.value % 7).toLong())
                        val date = startOfWeek.plusDays(dIndex.toLong())
                        if (!alreadyComplete) {
                            saveGoalCompletion(
                                context = context,
                                goalId = g.id,
                                goalHeader = g.header,
                                completionDate = date
                            )
                        }
                        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                        withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                    }
                }
            )
        }

        // ✨ Upgraded Confetti Overlay (multiple parties, themed colors, shapes, rotations)
        if (showConfetti) {
            val confettiColors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary,
                Color(0xFFFFD54F), // amber accent
                Color(0xFF80DEEA)  // cyan accent
            ).map { it.toArgb() }

            val shapes = listOf(Shape.Square, Shape.Circle)
            val sizes = listOf(Size.SMALL, Size.MEDIUM, Size.LARGE)

            val burstTop = Party(
                angle = Angle.BOTTOM,                    // fall down from top
                spread = 70,
                speed = 0f,
                maxSpeed = 22f,
                damping = 0.9f,
                rotation = Rotation.enabled(),
                timeToLive = 3500L,
                colors = confettiColors,
                shapes = shapes,
                size = sizes,
                position = Position.Relative(0.5, 0.0),
                emitter = Emitter(1200, TimeUnit.MILLISECONDS).perSecond(140)
            )

            val sideLeft = Party(
                angle = Angle.RIGHT,                     // stream from left
                spread = 45,
                speed = 4f,
                maxSpeed = 18f,
                rotation = Rotation.enabled(),
                timeToLive = 3000L,
                colors = confettiColors,
                shapes = shapes,
                size = sizes,
                position = Position.Relative(0.0, 0.4),
                emitter = Emitter(1000, TimeUnit.MILLISECONDS).perSecond(90)
            )

            val sideRight = Party(
                angle = Angle.LEFT,                      // stream from right
                spread = 45,
                speed = 4f,
                maxSpeed = 18f,
                rotation = Rotation.enabled(),
                timeToLive = 3000L,
                colors = confettiColors,
                shapes = shapes,
                size = sizes,
                position = Position.Relative(1.0, 0.4),
                emitter = Emitter(1000, TimeUnit.MILLISECONDS).perSecond(90)
            )

            val fountainBottom = Party(
                angle = Angle.TOP,                       // fountain up from bottom
                spread = 80,
                speed = 0f,
                maxSpeed = 20f,
                rotation = Rotation.enabled(),
                timeToLive = 3200L,
                colors = confettiColors,
                shapes = shapes,
                size = sizes,
                position = Position.Relative(0.5, 1.0),
                emitter = Emitter(1400, TimeUnit.MILLISECONDS).perSecond(120)
            )

            KonfettiView(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.98f), // slight blend with UI below
                parties = listOf(burstTop, sideLeft, sideRight, fountainBottom)
            )
        }

        // 🎉 Upgraded "All Goals Complete" Dialog (glass card + gradient header + actions)
        if (showAllDialog) {
            WeeklyWinDialog(
                onDismiss = { showAllDialog = false },
                onOpenRecords = {
                    showAllDialog = false
                    context.startActivity(android.content.Intent(context, RecordActivity::class.java))
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        )
    }
}

@Composable
private fun WeeklyGoalDialog(
    initial: WeeklyGoal,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
    onDelete: (() -> Unit)? = null,
    onProgress: (() -> Unit)? = null
) {
    var header by remember { mutableStateOf(initial.header) }
    var frequency by remember { mutableStateOf<Int?>(initial.frequency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { frequency?.let { onSave(header, it) } },
                enabled = header.isNotBlank() && frequency != null
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                onProgress?.let { prog ->
                    Button(
                        onClick = {
                            prog()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.padding(end = 8.dp)
                    ) { Text("Progress") }
                }
                onDelete?.let { del ->
                    TextButton(onClick = del) { Text("Delete") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        title = { Text("Customize your Weekly Routine!") },
        text = {
            Column {
                Text("Header", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = header,
                    onValueChange = { header = it.capitalizeSentences() },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text("Frequency", style = MaterialTheme.typography.bodySmall)
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .fillMaxWidth()
                ) {
                    for (i in 1..7) {
                        val text = when (i) {
                            1 -> "Once a Week"
                            2 -> "Twice a Week"
                            7 -> "Every Day"
                            else -> "$i Times a Week"
                        }
                        val selected = frequency == i
                        val selectedColor = MaterialTheme.colorScheme.primary
                        val unselectedColor = MaterialTheme.colorScheme.surfaceVariant

                        Button(
                            onClick = { frequency = i },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) selectedColor else unselectedColor
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) { Text(text) }
                    }
                }
            }
        }
    )
}

@Composable
private fun WeeklyWinDialog(
    onDismiss: () -> Unit,
    onOpenRecords: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        // Soft scrim
        Box(
            Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Card with gradient header + glass body
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                // Header gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Week Completed! 🏆",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // Body
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "You crushed every goal this week.\nKeep the streak alive!",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))

                    // Subtle divider
                    Box(
                        Modifier
                            .height(1.dp)
                            .fillMaxWidth(0.7f)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )

                    Spacer(Modifier.height(16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ElevatedButton(
                            onClick = onOpenRecords,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text("View Records")
                        }
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Awesome!")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun EdgeCelebrationOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val thickness = 32.dp.toPx()
        drawRect(
            brush = Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.12f), Color.Transparent)),
            size = androidx.compose.ui.geometry.Size(size.width, thickness)
        )
        drawRect(
            brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.12f))),
            topLeft = Offset(0f, size.height - thickness),
            size = androidx.compose.ui.geometry.Size(size.width, thickness)
        )
        drawRect(
            brush = Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.12f), Color.Transparent)),
            size = androidx.compose.ui.geometry.Size(thickness, size.height)
        )
        drawRect(
            brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.12f))),
            topLeft = Offset(size.width - thickness, 0f),
            size = androidx.compose.ui.geometry.Size(thickness, size.height)
        )
    }
}
