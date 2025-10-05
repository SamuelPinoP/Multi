package com.example.multi

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import kotlinx.coroutines.CoroutineScope
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

// Reorderable (drag & drop)
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

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

/** Simple model for a mindset card (id is runtime-only, not persisted). */
private data class MindsetItem(val id: Long, var text: String)

/** Per-goal description & expansion prefs (no DB schema change needed). */
private object GoalNotesPrefs {
    private const val NAME = "goal_notes_prefs"
    private fun sp(ctx: Context) =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun getText(ctx: Context, goalId: Long): String =
        sp(ctx).getString("text_$goalId", "") ?: ""

    fun setText(ctx: Context, goalId: Long, value: String) {
        sp(ctx).edit().putString("text_$goalId", value).apply()
    }

    fun isExpanded(ctx: Context, goalId: Long): Boolean =
        sp(ctx).getBoolean("expanded_$goalId", false)

    fun setExpanded(ctx: Context, goalId: Long, expanded: Boolean) {
        sp(ctx).edit().putBoolean("expanded_$goalId", expanded).apply()
    }
}

/** Persist the flat order (Mindset + Goals). Grouping is inferred from adjacency. */
private object DashboardOrderPrefs {
    private const val NAME = "dashboard_order_prefs"
    private const val KEY = "order"
    private fun sp(ctx: Context) = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    fun load(ctx: Context): List<String> =
        sp(ctx).getString(KEY, "")?.split('|')?.filter { it.isNotBlank() } ?: emptyList()
    fun save(ctx: Context, keys: List<String>) {
        sp(ctx).edit().putString(KEY, keys.joinToString("|")).apply()
    }
}

/** Mixed list rows for the UI (flat). Grouping = goal appears after last seen mindset. */
private sealed class DashItem {
    data class Mindset(val item: MindsetItem) : DashItem()
    data class Goal(val goal: WeeklyGoal) : DashItem()
}

private val DashItem.stableKey: String
    get() = when (this) {
        is DashItem.Mindset -> "M:${item.id}"
        is DashItem.Goal -> "G:${goal.id}"
    }

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeeklyGoalsScreen(highlightGoalId: Long? = null) {
    val context = LocalContext.current
    val goals = remember { mutableStateListOf<WeeklyGoal>() }
    var editingIndex by remember { mutableStateOf<Int?>(null) } // goals editor index
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedGoalIndex by remember { mutableStateOf<Int?>(null) }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    var showConfetti by remember { mutableStateOf(false) }
    var showAllDialog by remember { mutableStateOf(false) }
    var edgeCelebration by remember { mutableStateOf(false) }

    // --- Mindset state (multi-card) ---
    val mindsetItems = remember { mutableStateListOf<MindsetItem>() }
    val mindsetExpanded = remember { mutableStateListOf<Boolean>() }
    var showMindsetDialog by remember { mutableStateOf(false) }
    var mindsetDialogText by remember { mutableStateOf("") }
    var mindsetDialogIndex by remember { mutableStateOf<Int?>(null) } // null = none, -1 = add, >=0 = edit

    // --- Per-goal descriptions & expand states ---
    val goalDescriptions = remember { mutableStateMapOf<Long, String>() }
    val goalExpanded = remember { mutableStateMapOf<Long, Boolean>() }
    var showGoalDescDialog by remember { mutableStateOf(false) }
    var editingGoalDescId by remember { mutableStateOf<Long?>(null) }
    var editingGoalDescText by remember { mutableStateOf("") }

    // --- Flat rows you can reorder (Mindsets + Goals) ---
    val dashItems = remember { mutableStateListOf<DashItem>() }

    /** Build parent map (goalId -> mindsetId) from flat list based on adjacency. */
    fun buildParentMap(list: List<DashItem>): Map<Long, Long> {
        val map = mutableMapOf<Long, Long>()
        var currentMindset: Long? = null
        list.forEach { row ->
            when (row) {
                is DashItem.Mindset -> currentMindset = row.item.id
                is DashItem.Goal -> if (currentMindset != null) map[row.goal.id] = currentMindset!!
            }
        }
        return map
    }

    /** Deterministic color for a mindset. */
    fun colorForMindset(id: Long): Color {
        val hue = ((id and 0xFFFF).toFloat() % 360f)
        return Color.hsv(hue, 0.55f, 0.85f)
    }

    /** Rebuild flat list from current mindsets + goals using saved flat order when possible. */
    fun rebuildDashFromPrefs() {
        val keyToItem = mutableMapOf<String, DashItem>()
        mindsetItems.forEach { keyToItem["M:${it.id}"] = DashItem.Mindset(it) }
        goals.forEach { keyToItem["G:${it.id}"] = DashItem.Goal(it) }

        val saved = DashboardOrderPrefs.load(context)
        dashItems.clear()
        saved.forEach { k -> keyToItem.remove(k)?.let(dashItems::add) }
        // append any new/unsaved items
        mindsetItems.forEach { mi ->
            if (dashItems.none { it is DashItem.Mindset && it.item.id == mi.id }) {
                dashItems.add(DashItem.Mindset(mi))
            }
        }
        goals.forEach { g ->
            if (dashItems.none { it is DashItem.Goal && it.goal.id == g.id }) {
                dashItems.add(DashItem.Goal(g))
            }
        }
        DashboardOrderPrefs.save(context, dashItems.map { it.stableKey })
    }

    LaunchedEffect(Unit) {
        edgeCelebration = GoalCelebrationPrefs.isActive(context)

        // Load persisted mindsets + expanded states
        val savedTexts = MindsetPrefs.getMindsets(context)
        val savedExpanded = MindsetPrefs.getExpandedStates(context)
        mindsetItems.clear()
        mindsetExpanded.clear()
        val base = System.currentTimeMillis()
        savedTexts.forEachIndexed { i, txt ->
            mindsetItems.add(MindsetItem(id = base + i, text = txt))
        }
        val adjusted = MutableList(mindsetItems.size) { idx -> savedExpanded.getOrNull(idx) ?: false }
        mindsetExpanded.addAll(adjusted)

        rebuildDashFromPrefs()
    }

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

            // prime description & expansion for this goal
            goalDescriptions[model.id] = GoalNotesPrefs.getText(context, model.id)
            goalExpanded[model.id] = GoalNotesPrefs.isExpanded(context, model.id)
        }

        rebuildDashFromPrefs()

        highlightGoalId?.let { id ->
            val index = goals.indexOfFirst { it.id == id }
            if (index >= 0) editingIndex = index
        }
    }

    // Derived: who owns each goal right now (by adjacency)
    val parentOfGoal by remember(dashItems) {
        derivedStateOf { buildParentMap(dashItems) }
    }

    // Track drag state
    var draggingKey by remember { mutableStateOf<String?>(null) }
    var draggingMindsetChildren by remember { mutableStateOf<List<String>>(emptyList()) }

    // Reorder state for ONE flat LazyColumn
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            // Default single-item move
            dashItems.add(to.index, dashItems.removeAt(from.index))
        },
        onDragEnd = { _, _ ->
            // If a mindset was dragged, snap all its children right under it (block move after drop)
            val key = draggingKey
            if (key != null && key.startsWith("M:")) {
                val headerIndex = dashItems.indexOfFirst { it.stableKey == key }
                if (headerIndex >= 0 && draggingMindsetChildren.isNotEmpty()) {
                    // Remove any children wherever they ended up
                    val childrenKeys = draggingMindsetChildren.toSet()
                    val extracted = mutableListOf<DashItem>()
                    val it = dashItems.iterator()
                    while (it.hasNext()) {
                        val row = it.next()
                        if (row.stableKey in childrenKeys) {
                            extracted.add(row); it.remove()
                        }
                    }
                    // Insert them right under the header, keeping original order
                    dashItems.addAll(headerIndex + 1, extracted)
                }
            }
            draggingKey = null
            draggingMindsetChildren = emptyList()

            // Persist order
            DashboardOrderPrefs.save(context, dashItems.map { it.stableKey })
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (edgeCelebration) {
            EdgeCelebrationOverlay(modifier = Modifier.matchParentSize())
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(vertical = 8.dp) // full-bleed list; group borders go edge-to-edge
        ) {
            // Header (not draggable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                textAlign = TextAlign.Center
            )

            if (mindsetItems.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            mindsetDialogIndex = -1
                            mindsetDialogText = ""
                            showMindsetDialog = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add mindset")
                    }
                }
            }

            // --- ONE flat list: Mindsets + Goals ---
            LazyColumn(
                state = reorderState.listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .reorderable(reorderState),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                itemsIndexed(
                    items = dashItems,
                    key = { _, it -> it.stableKey }
                ) { index, row ->
                    ReorderableItem(
                        state = reorderState,
                        key = row.stableKey
                    ) { isDragging ->
                        // detect drag start & remember children of a mindset
                        LaunchedEffect(isDragging) {
                            if (isDragging) {
                                draggingKey = row.stableKey
                                if (row is DashItem.Mindset) {
                                    val start = dashItems.indexOfFirst { it.stableKey == row.stableKey }
                                    val kids = mutableListOf<String>()
                                    for (i in (start + 1) until dashItems.size) {
                                        when (val itRow = dashItems[i]) {
                                            is DashItem.Mindset -> break
                                            is DashItem.Goal -> kids.add(itRow.stableKey)
                                        }
                                    }
                                    draggingMindsetChildren = kids
                                }
                            }
                        }

                        val parentMap = parentOfGoal // capture
                        val parentIdForGoal = (row as? DashItem.Goal)?.goal?.id?.let { parentMap[it] }
                        val grouped = parentIdForGoal != null || row is DashItem.Mindset
                        val groupColor = when (row) {
                            is DashItem.Mindset -> colorForMindset(row.item.id)
                            is DashItem.Goal -> parentIdForGoal?.let { colorForMindset(it) }
                            else -> null
                        }

                        val isFirst = row is DashItem.Mindset
                        val isLast = when (row) {
                            is DashItem.Mindset -> {
                                // Last if next item is a Mindset or end & there are no goals under it
                                val next = dashItems.getOrNull(index + 1)
                                next !is DashItem.Goal
                            }
                            is DashItem.Goal -> {
                                val next = dashItems.getOrNull(index + 1)
                                val pThis = parentMap[row.goal.id]
                                when (next) {
                                    is DashItem.Goal -> parentMap[next.goal.id] != pThis
                                    is DashItem.Mindset, null -> true
                                    else -> true
                                }
                            }
                        }

                        val outer = Modifier
                            .fillMaxWidth()
                            .detectReorderAfterLongPress(reorderState)

                        if (grouped && groupColor != null) {
                            // Group frame draws one continuous colored square around the whole group
                            GroupItemFrame(
                                color = groupColor,
                                drawTop = isFirst,
                                drawBottom = isLast,
                                modifier = outer
                            ) {
                                RowContent(
                                    row = row,
                                    context = context,
                                    mindsetItems = mindsetItems,
                                    mindsetExpanded = mindsetExpanded,
                                    goalDescriptions = goalDescriptions,
                                    goalExpanded = goalExpanded,
                                    dashItems = dashItems,
                                    goals = goals,
                                    parentOfGoal = parentMap,
                                    colorForMindset = ::colorForMindset,
                                    snackbarHostState = snackbarHostState,
                                    scope = scope,
                                    onOpenMindsetEditor = { idx ->
                                        mindsetDialogIndex = idx
                                        mindsetDialogText = mindsetItems[idx].text
                                        showMindsetDialog = true
                                    },
                                    onOpenMindsetAdder = {
                                        mindsetDialogIndex = -1
                                        mindsetDialogText = ""
                                        showMindsetDialog = true
                                    },
                                    onDeleteMindset = { idx ->
                                        val idToRemove = mindsetItems[idx].id
                                        mindsetItems.removeAt(idx)
                                        mindsetExpanded.removeAt(idx)
                                        dashItems.removeAll { it is DashItem.Mindset && it.item.id == idToRemove }
                                        MindsetPrefs.setMindsets(context, mindsetItems.map { it.text })
                                        MindsetPrefs.setExpandedStates(context, mindsetExpanded.toList())
                                        DashboardOrderPrefs.save(context, dashItems.map { it.stableKey })
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        scope.launch { snackbarHostState.showSnackbar("Mindset deleted") }
                                    },
                                    showGoalDescDialogSetter = { id, txt ->
                                        editingGoalDescId = id
                                        editingGoalDescText = txt
                                        showGoalDescDialog = true
                                    },
                                    setEditingIndex = { editingIndex = it },
                                    setDayPicker = { gi, di -> selectedGoalIndex = gi; selectedDayIndex = di }
                                )
                            }
                        } else {
                            // Stand-alone goal (neutral border, padded sides)
                            Box(
                                outer
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                RowContent(
                                    row = row,
                                    context = context,
                                    mindsetItems = mindsetItems,
                                    mindsetExpanded = mindsetExpanded,
                                    goalDescriptions = goalDescriptions,
                                    goalExpanded = goalExpanded,
                                    dashItems = dashItems,
                                    goals = goals,
                                    parentOfGoal = parentMap,
                                    colorForMindset = ::colorForMindset,
                                    snackbarHostState = snackbarHostState,
                                    scope = scope,
                                    onOpenMindsetEditor = {},
                                    onOpenMindsetAdder = {},
                                    onDeleteMindset = {},
                                    showGoalDescDialogSetter = { id, txt ->
                                        editingGoalDescId = id
                                        editingGoalDescText = txt
                                        showGoalDescDialog = true
                                    },
                                    setEditingIndex = { editingIndex = it },
                                    setDayPicker = { gi, di -> selectedGoalIndex = gi; selectedDayIndex = di }
                                )
                            }
                        }
                    }

                    // Vertical gap only between different groups or stand-alone rows
                    val next = dashItems.getOrNull(index + 1)
                    val gapNeeded = when (val cur = dashItems[index]) {
                        is DashItem.Mindset -> next == null || next is DashItem.Mindset
                        is DashItem.Goal -> {
                            val pCur = parentOfGoal[cur.goal.id]
                            when (next) {
                                is DashItem.Goal -> parentOfGoal[next.goal.id] != pCur
                                is DashItem.Mindset, null -> true
                            }
                        }
                    }
                    if (gapNeeded) Spacer(Modifier.height(8.dp))
                }
            }
        }

        // Dialogs & overlays

        if (showMindsetDialog) {
            MindsetInputDialog(
                initial = mindsetDialogText,
                onDismiss = { showMindsetDialog = false },
                onSave = { newText ->
                    val text = newText.trim()
                    if (text.isNotEmpty()) {
                        when (val i = mindsetDialogIndex) {
                            null -> Unit
                            -1 -> {
                                val item = MindsetItem(
                                    id = System.currentTimeMillis() + mindsetItems.size,
                                    text = text
                                )
                                mindsetItems.add(item)
                                mindsetExpanded.add(true)
                                dashItems.add(DashItem.Mindset(item))
                                DashboardOrderPrefs.save(context, dashItems.map { it.stableKey })
                            }
                            else -> {
                                if (i in mindsetItems.indices) {
                                    val oldId = mindsetItems[i].id
                                    mindsetItems[i] = mindsetItems[i].copy(text = text)
                                    val di = dashItems.indexOfFirst { it is DashItem.Mindset && it.item.id == oldId }
                                    if (di >= 0) dashItems[di] = DashItem.Mindset(mindsetItems[i])
                                }
                            }
                        }
                        MindsetPrefs.setMindsets(context, mindsetItems.map { it.text })
                        MindsetPrefs.setExpandedStates(context, mindsetExpanded.toList())
                        scope.launch { snackbarHostState.showSnackbar("Mindset saved") }
                    }
                    showMindsetDialog = false
                }
            )
        }

        if (showGoalDescDialog) {
            GoalDescriptionDialog(
                initial = editingGoalDescText,
                onDismiss = { showGoalDescDialog = false },
                onSave = { newText ->
                    val id = editingGoalDescId
                    if (id != null) {
                        val cleaned = newText.trim()
                        goalDescriptions[id] = cleaned
                        GoalNotesPrefs.setText(context, id, cleaned)
                        goalExpanded[id] = true
                        GoalNotesPrefs.setExpanded(context, id, true)
                        scope.launch { snackbarHostState.showSnackbar("Description saved") }
                    }
                    showGoalDescDialog = false
                }
            )
        }

        if (goals.isEmpty() && mindsetItems.isEmpty()) {
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
                    val di = dashItems.indexOfFirst { it is DashItem.Goal && it.goal.id == g.id }
                    if (di >= 0) dashItems[di] = DashItem.Goal(updated)

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
                    val di = dashItems.indexOfFirst { it is DashItem.Goal && it.goal.id == g.id }
                    if (di >= 0) dashItems[di] = DashItem.Goal(updated)

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

        // Confetti
        if (showConfetti) {
            val confettiColors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary,
                Color(0xFFFFD54F),
                Color(0xFF80DEEA)
            ).map { it.toArgb() }

            val shapes: List<Shape> = listOf(Shape.Square, Shape.Circle)
            val sizes: List<Size> = listOf(Size.SMALL, Size.MEDIUM, Size.LARGE)

            val burstTop = Party(
                angle = Angle.BOTTOM,
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
                angle = Angle.RIGHT,
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
                angle = Angle.LEFT,
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
                angle = Angle.TOP,
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
                    .alpha(0.98f),
                parties = listOf(burstTop, sideLeft, sideRight, fountainBottom)
            )
        }

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

/* --------------------------- Group frame & helpers --------------------------- */

/** Draws the colored square around items belonging to one mindset group. */
@Composable
private fun GroupItemFrame(
    color: Color,
    drawTop: Boolean,
    drawBottom: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Canvas(Modifier
            .matchParentSize()
            .padding(horizontal = 0.dp)) {
            val stroke = 3.dp.toPx()
            val inset = 8.dp.toPx() // pull in a hair so it’s fully visible
            val leftX = inset
            val rightX = size.width - inset

            // verticals (always)
            drawLine(color, start = Offset(leftX, 0f), end = Offset(leftX, size.height), strokeWidth = stroke)
            drawLine(color, start = Offset(rightX, 0f), end = Offset(rightX, size.height), strokeWidth = stroke)

            // top/bottom only at group edges
            if (drawTop) {
                drawLine(color, start = Offset(leftX, 0f), end = Offset(rightX, 0f), strokeWidth = stroke)
            }
            if (drawBottom) {
                drawLine(color, start = Offset(leftX, size.height), end = Offset(rightX, size.height), strokeWidth = stroke)
            }
        }

        // inside padding so content doesn’t collide with the border
        Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            content()
        }
    }
}

/* --------------------------- Row renderers --------------------------- */

@Composable
private fun RowContent(
    row: DashItem,
    context: Context,
    mindsetItems: MutableList<MindsetItem>,
    mindsetExpanded: MutableList<Boolean>,
    goalDescriptions: MutableMap<Long, String>,
    goalExpanded: MutableMap<Long, Boolean>,
    dashItems: MutableList<DashItem>,
    goals: MutableList<WeeklyGoal>,
    parentOfGoal: Map<Long, Long>,
    colorForMindset: (Long) -> Color,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,            // <- fix here
    onOpenMindsetEditor: (Int) -> Unit,
    onOpenMindsetAdder: () -> Unit,
    onDeleteMindset: (Int) -> Unit,
    showGoalDescDialogSetter: (Long, String) -> Unit,
    setEditingIndex: (Int) -> Unit,
    setDayPicker: (Int, Int) -> Unit
) {
    when (row) {
        is DashItem.Mindset -> MindsetCard(
            context = context,
            mindsetItems = mindsetItems,
            mindsetExpanded = mindsetExpanded,
            mindset = row.item,
            onEdit = onOpenMindsetEditor,
            onAddAnother = onOpenMindsetAdder,
            onDelete = onDeleteMindset
        )
        is DashItem.Goal -> {
            val g = row.goal
            val parentId = parentOfGoal[g.id]
            GoalCard(
                context = context,
                goal = g,
                isGroupedUnder = parentId,
                colorForMindset = colorForMindset,
                desc = goalDescriptions[g.id] ?: "",
                isExpanded = goalExpanded[g.id] ?: false,
                onToggleDesc = {
                    val newState = !(goalExpanded[g.id] ?: false)
                    goalExpanded[g.id] = newState
                    GoalNotesPrefs.setExpanded(context, g.id, newState)
                },
                onEditDesc = { showGoalDescDialogSetter(g.id, goalDescriptions[g.id] ?: "") },
                onClick = {
                    val idx = goals.indexOfFirst { it.id == g.id }
                    if (idx >= 0) setEditingIndex(idx)
                },
                onDayClick = { di ->
                    val gi = goals.indexOfFirst { it.id == g.id }
                    if (gi >= 0) setDayPicker(gi, di)
                },
                onQuickComplete = {
                    val gi = goals.indexOfFirst { it.id == g.id }
                    if (gi >= 0) {
                        val today = LocalDate.now().toString()
                        val current = goals[gi]
                        val completed = current.dayStates.count { it == 'C' }
                        if (current.lastCheckedDate != today && completed < current.frequency + 20) {
                            val chars = current.dayStates.toCharArray()
                            val dIndex = LocalDate.now().dayOfWeek.value % 7
                            if (chars[dIndex] != 'C') {
                                chars[dIndex] = 'C'
                                val newCompleted = chars.count { it == 'C' }
                                val updated = current.copy(
                                    dayStates = String(chars),
                                    remaining = (current.frequency - newCompleted).coerceAtLeast(0),
                                    lastCheckedDate = today
                                )
                                val wasIncomplete = current.remaining > 0
                                goals[gi] = updated
                                val di = dashItems.indexOfFirst { it is DashItem.Goal && it.goal.id == updated.id }
                                if (di >= 0) dashItems[di] = DashItem.Goal(updated)
                                if (wasIncomplete && updated.remaining == 0) {
                                    scope.launch { snackbarHostState.showSnackbar("Goal completed!") }
                                }
                                scope.launch {
                                    saveGoalCompletion(
                                        context = context,
                                        goalId = updated.id,
                                        goalHeader = updated.header,
                                        completionDate = LocalDate.now()
                                    )
                                    val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                                    withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun MindsetCard(
    context: Context,
    mindsetItems: MutableList<MindsetItem>,
    mindsetExpanded: MutableList<Boolean>,
    mindset: MindsetItem,
    onEdit: (index: Int) -> Unit,
    onAddAnother: () -> Unit,
    onDelete: (index: Int) -> Unit
) {
    val idx = mindsetItems.indexOfFirst { it.id == mindset.id }
    val expanded = if (idx >= 0) mindsetExpanded.getOrNull(idx) ?: false else false
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "mindset-${mindset.id}")

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (idx >= 0) {
                    val newState = !mindsetExpanded[idx]
                    mindsetExpanded[idx] = newState
                    MindsetPrefs.setExpandedStates(context, mindsetExpanded.toList())
                }
            },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drag handle icon REMOVED (kept drag gesture on the whole row)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mindset",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (expanded) "Your focus is:" else "Tap to view",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                IconButton(onClick = onAddAnother) {
                    Icon(Icons.Default.Add, contentDescription = "Add another mindset")
                }
                IconButton(onClick = { if (idx >= 0) onEdit(idx) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit mindset")
                }
                IconButton(onClick = { if (idx >= 0) onDelete(idx) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete mindset")
                }
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse mindset" else "Expand mindset",
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val txt = mindsetItems.getOrNull(idx)?.text.orEmpty()
                    if (txt.isNotBlank()) {
                        Text(
                            text = txt,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "No text yet. Tap the pencil to edit.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    context: Context,
    goal: WeeklyGoal,
    isGroupedUnder: Long?, // mindsetId if grouped by adjacency
    colorForMindset: (Long) -> Color,
    desc: String,
    isExpanded: Boolean,
    onToggleDesc: () -> Unit,
    onEditDesc: () -> Unit,
    onClick: () -> Unit,
    onDayClick: (Int) -> Unit,
    onQuickComplete: () -> Unit,
) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "goal-${goal.id}")

    // Grouped goals live inside the colored square; standalone goals keep their neutral border.
    val cardModifier = if (isGroupedUnder == null) {
        Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
    } else {
        Modifier.fillMaxWidth()
    }

    ElevatedCard(
        modifier = cardModifier
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Title + description controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drag handle icon REMOVED (drag is on whole row)
                Text(goal.header, style = MaterialTheme.typography.bodyLarge)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditDesc) {
                        Icon(
                            imageVector = if (desc.isBlank()) Icons.Default.Add else Icons.Default.Edit,
                            contentDescription = if (desc.isBlank()) "Add description" else "Edit description"
                        )
                    }
                    IconButton(onClick = onToggleDesc) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse description" else "Expand description",
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                }
            }

            // Counts row
            val completed = goal.dayStates.count { it == 'C' }
            val over = overageCount(completed, goal.frequency)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics {
                        contentDescription =
                            "$completed of ${goal.frequency} completed" +
                                    if (over > 0) ", $over over target" else ""
                    }
                ) {
                    if (goal.remaining == 0) {
                        Text(
                            text = "Completed!",
                            color = Color(0xFF43A047),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
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
                }

                Icon(
                    Icons.Default.Check,
                    contentDescription = "Quick complete today",
                    tint = Color(0xFF43A047),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable { onQuickComplete() }
                )
            }

            // Progress bar
            val progress = (goal.frequency - goal.remaining).toFloat() / goal.frequency
            LinearProgressIndicator(
                progress = progress,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            // Description
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (desc.isBlank()) {
                        Text(
                            text = "No description yet. Tap + to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            DayButtonsRow(states = goal.dayStates) { dayIndex -> onDayClick(dayIndex) }
        }
    }
}

/* ---------------------- Existing dialogs & overlay ---------------------- */

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
        Box(
            Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
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
                        ) { Text("Awesome!") }
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

// ---------- Mindset input dialog ----------
@Composable
private fun MindsetInputDialog(
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Mindset") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("e.g., Loving Jesus") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 6,
                keyboardOptions = KeyboardOptions.Default
            )
        },
        confirmButton = {
            TextButton(
                enabled = text.isNotBlank(),
                onClick = { onSave(text.trim()) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ---------- Goal description dialog ----------
@Composable
private fun GoalDescriptionDialog(
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Goal Description") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Add purpose to this goal") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 6,
                keyboardOptions = KeyboardOptions.Default
            )
        },
        confirmButton = {
            TextButton(
                enabled = text.isNotBlank(),
                onClick = { onSave(text.trim()) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
