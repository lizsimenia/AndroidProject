package com.example.coflow.ui.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import com.example.coflow.data.Task
//import com.example.coflow.models.TaskStatus
import com.example.coflow.models.TaskModel
import com.example.coflow.data.firebase.TaskStatus
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
//import com.example.coflow.data.TasksTestData
import com.example.coflow.ui.components.*
import com.example.coflow.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.coflow.R

private enum class TaskGrouping { STATUS, DEADLINE }
private data class TaskGroup(val id: String, val title: String, val tasks: List<TaskModel>)

@Composable
fun TasksScreen(
    viewModel: TaskScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel(), // ← ViewModel создается сам
    onNavigateToCreateTask: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit
) {
    // 1. Получаем состояние из ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = viewModel.getCurrentUserId()

    // 2. Извлекаем список задач. Если идет загрузка или ошибка, покажем пустой список (или экран загрузки ниже)
    val tasks = when (val state = uiState) {
        is TasksUiState.Success -> state.tasks
        else -> emptyList()
    }

    // 3. (Опционально, но рекомендуется) Показываем индикатор загрузки, если данные еще не пришли
    if (uiState is TasksUiState.Loading) {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(color = com.example.coflow.ui.theme.CoFlowColors.Blue)
        }
        return // Прерываем отрисовку остального UI, пока грузится
    }

    var grouping by remember { mutableStateOf(TaskGrouping.STATUS) }
    var selectedFilters by remember { mutableStateOf(setOf<String>()) }
    var collapsedGroups by remember { mutableStateOf(setOf("IN_PROCESS")) }
    var dialogTitle by remember { mutableStateOf<String?>(null) }
    var dialogMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    var previousScroll by remember { mutableStateOf(0) }
    var bottomVisible by remember { mutableStateOf(true) }

    LaunchedEffect(scrollState.value) {
        val current = scrollState.value
        if (current > previousScroll + 2) bottomVisible = false
        else if (current < previousScroll - 2) bottomVisible = true
        previousScroll = current
    }

    val today = LocalDate.now()
    val now = LocalDateTime.now()

    val filteredTasks = tasks.filter { task ->
        if (selectedFilters.isEmpty()) true
        else {
            val isMineSelected = selectedFilters.contains(stringResource(R.string.filter_mine))
            val projectFilters = selectedFilters.filter { it.startsWith("Проект") }

            val isMine = task.executorRef?.id == currentUserId || task.authorRef?.id == currentUserId

            // Пока фильтруем по ID проекта (projectRef.id).
            // Когда появится кэш имён проектов — заменим на сравнение по имени.
            val matchesProject = projectFilters.any { filterName ->
                task.projectRef?.id == filterName || task.projectRef?.path?.contains(filterName) == true
            }

            (isMineSelected && isMine) || matchesProject
        }
    }



    val groups = when (grouping) {
        TaskGrouping.STATUS -> listOf(
            TaskGroup(
                "TODO",
                stringResource(R.string.tasks_group_planned),
                filteredTasks.filter { it.status == TaskStatus.TODO }
            ),
            TaskGroup(
                "IN_PROCESS",
                stringResource(R.string.tasks_group_active),
                filteredTasks.filter { it.status == TaskStatus.IN_PROCESS }
            ),
            TaskGroup(
                "DONE",
                stringResource(R.string.tasks_group_completed),
                filteredTasks.filter { it.status == TaskStatus.DONE }
            )
        )
        TaskGrouping.DEADLINE -> listOf(
            TaskGroup(
                "TODAY",
                stringResource(R.string.tasks_group_today),
                filteredTasks
                    .filter { it.deadline.toLocalDate() == today }
                    .sortedBy { it.deadline }
            ),
            TaskGroup(
                "THIS_WEEK",
                stringResource(R.string.tasks_group_this_week),
                filteredTasks
                    .filter {
                        val d = it.deadline.toLocalDate()
                        d.isAfter(today) && !d.isAfter(today.plusDays(6))
                    }
                    .sortedBy { it.deadline }
            ),
            TaskGroup(
                "NEXT_WEEK",
                stringResource(R.string.tasks_group_next_week),
                filteredTasks
                    .filter {
                        val d = it.deadline.toLocalDate()
                        d.isAfter(today.plusDays(6)) && !d.isAfter(today.plusDays(13))
                    }
                    .sortedBy { it.deadline }
            )
        )
    }

    val completedCount = filteredTasks.count { it.status == TaskStatus.DONE }
    val activeCount = filteredTasks.size - completedCount
    val filterButtonText = when {
        selectedFilters.isEmpty() -> stringResource(R.string.filter_none)
        selectedFilters.size == 1 -> selectedFilters.first()
        else -> "Выбрано: ${selectedFilters.size}"
    }

    Box(modifier = Modifier.fillMaxSize().background(coFlowTasksBackgroundBrush())) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = CoFlowSpacing.ScreenHorizontal)) {
            // ИСПРАВЛЕНО: Единый, чистый заголовок без стрелки назад, но с подзаголовком и колокольчиком
            CoFlowScreenHeader(
                title = stringResource(R.string.my_tasks_header),
                subtitle = "$activeCount ${stringResource(R.string.my_tasks_active)} · $completedCount ${stringResource(R.string.my_tasks_completed)}",
                trailingContent = {
                    Box {
                        CoFlowGlassIconButton(
                            iconName = "bell",
                            onClick = {
                                dialogTitle = "Уведомления"
                                dialogMessage = "Интерфейс уведомлений ещё не реализован."
                            }
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 3.dp, end = 3.dp)
                                .size(6.dp)
                                .background(CoFlowColors.Blue, CircleShape)
                        )
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.weight(1f).padding(bottom = 8.dp).clipToBounds().verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groups.forEach { group ->
                    TaskGroupCard(
                        group = group,
                        expanded = group.id !in collapsedGroups,
                        onToggle = { collapsedGroups = if (group.id in collapsedGroups) collapsedGroups - group.id else collapsedGroups + group.id },
                        onTaskClick = { task -> onNavigateToTaskDetail(task.id) }
                    )
                }
                Spacer(Modifier.height(18.dp))
            }

            AnimatedVisibility(visible = bottomVisible) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.widthIn(max = 330.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        SortFilterPanel(
                            tasks = tasks, // <-- ДОБАВЛЕНО: передаем реальные задачи из ViewModel
                            modifier = Modifier.weight(1f),
                            grouping = grouping,
                            onGroupingChange = { newGrouping -> grouping = newGrouping; collapsedGroups = emptySet() },
                            selectedFilters = selectedFilters,
                            filterButtonText = filterButtonText,
                            onFilterToggle = { option ->
                                if (option == "Без фильтра") selectedFilters = emptySet()
                                else {
                                    val newSet = selectedFilters.toMutableSet()
                                    if (newSet.contains("Без фильтра")) newSet.remove("Без фильтра")
                                    if (newSet.contains(option)) newSet.remove(option) else newSet.add(option)
                                    selectedFilters = newSet.toSet()
                                }
                            }
                        )
                        Spacer(Modifier.width(16.dp))

                        CoFlowFabButton(onClick = onNavigateToCreateTask)
                    }
                    Spacer(Modifier.height(7.dp))

                    CoFlowBottomNavigationBar(
                        items = listOf(
                            NavItem(icon = "tasks", title = stringResource(R.string.navigation_tasks), selected = true, onClick = {}),
                            NavItem(icon = "folder", title = stringResource(R.string.navigation_projects), selected = false, onClick = { dialogTitle = "Проекты"; dialogMessage = "Интерфейс проектов ещё не реализован." }),
                            NavItem(icon = "person", title = stringResource(R.string.navigation_profile), selected = false, onClick = { dialogTitle = "Профиль"; dialogMessage = "Интерфейс профиля ещё не реализован." })
                        )
                    )
                }
            }
        }

        if (dialogTitle != null) {
            AlertDialog(
                onDismissRequest = { dialogTitle = null },
                title = { Text(text = dialogTitle!!, color = CoFlowColors.TextPrimary) },
                text = { Text(text = dialogMessage, color = CoFlowColors.TextSecondary) },
                confirmButton = { TextButton(onClick = { dialogTitle = null }) { Text("Понятно", color = CoFlowColors.Blue) } },
                containerColor = Color(0xFFF4F7FF)
            )
        }
    }
}

@Composable
private fun TaskGroupCard(group: TaskGroup, expanded: Boolean, onToggle: () -> Unit, onTaskClick: (TaskModel) -> Unit) {
    CoFlowGlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = group.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextPrimary)
            Spacer(Modifier.width(7.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(CoFlowColors.BlueLight.copy(alpha = 0.38f)).padding(horizontal = 5.dp, vertical = 2.dp)) {
                Text(text = group.tasks.size.toString(), fontSize = 10.sp, fontWeight = FontWeight.Medium, color = CoFlowColors.Blue)
            }
            Spacer(Modifier.weight(1f))
            Text(text = if (expanded) "⌄" else "›", color = CoFlowColors.Blue, fontSize = 17.sp)
        }

        if (expanded) {
            if (group.tasks.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(text = "Нет задач", fontSize = 11.sp, color = CoFlowColors.TextSecondary)
            } else {
                Spacer(Modifier.height(3.dp))
                group.tasks.forEachIndexed { index, task ->
                    TaskRow(task = task, onClick = { onTaskClick(task) })
                    if (index < group.tasks.lastIndex) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CoFlowGlassStyles.BlueBorder.copy(alpha = 0.45f)))
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: TaskModel, onClick: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yy") }
    val today = LocalDate.now()
    val taskDate = task.deadline.toLocalDate()

    // Пока показываем ID проекта как временное решение.
    // Позже здесь будет резолв имени из кэша проектов.
    val projectNameDisplay = task.projectRef?.id ?: "Без проекта"

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onClick() }) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = task.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (task.status == TaskStatus.DONE) CoFlowColors.TextSecondary else CoFlowColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (taskDate.isBefore(today) && task.status != TaskStatus.DONE) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.task_outdated),
                    fontSize = 8.sp,
                    color = CoFlowColors.Error
                )
            }
        }
        Spacer(Modifier.height(3.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = projectNameDisplay,
                fontSize = 10.sp,
                color = CoFlowColors.TextSecondary,
                maxLines = 1
            )
            Text(
                text = taskDate.format(formatter),
                fontSize = 10.sp,
                color = CoFlowColors.TextSecondary
            )
        }
    }
}

// --- Внутренние компоненты для панели фильтров ---
@Composable
private fun SortFilterPanel(
    tasks: List<TaskModel>, // <-- ДОБАВЛЕНО: теперь функция видит список задач
    modifier: Modifier = Modifier,
    grouping: TaskGrouping,
    onGroupingChange: (TaskGrouping) -> Unit,
    selectedFilters: Set<String>,
    filterButtonText: String,
    onFilterToggle: (String) -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            var expanded by remember { mutableStateOf(false) }
            GlassControlSurface(modifier = Modifier.fillMaxWidth().clickable { expanded = true }) {
                Column {
                    Text(text = stringResource(R.string.grouping_header), fontSize = 9.sp, color = CoFlowColors.TextSecondary)
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (grouping == TaskGrouping.STATUS) stringResource(R.string.grouping_status) else stringResource(R.string.grouping_deadline), fontSize = 11.sp, color = CoFlowColors.TextPrimary, maxLines = 1)
                        Spacer(Modifier.width(3.dp))
                        Text("⌄", fontSize = 11.sp, color = CoFlowColors.Blue)
                    }
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, shape = RoundedCornerShape(16.dp), containerColor = Color(0xFFF4F8FF).copy(alpha = 0.62f), tonalElevation = 0.dp, shadowElevation = 0.dp, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.82f))) {
                GlassGroupingOption(title = stringResource(R.string.grouping_status), selected = grouping == TaskGrouping.STATUS) { onGroupingChange(TaskGrouping.STATUS); expanded = false }
                GlassGroupingOption(title = stringResource(R.string.grouping_deadline), selected = grouping == TaskGrouping.DEADLINE) { onGroupingChange(TaskGrouping.DEADLINE); expanded = false }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            var expanded by remember { mutableStateOf(false) }
            GlassControlSurface(modifier = Modifier.fillMaxWidth().clickable { expanded = true }) {
                Column {
                    Text(text = stringResource(R.string.filter_header), fontSize = 9.sp, color = CoFlowColors.TextSecondary)
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = filterButtonText, fontSize = 11.sp, color = CoFlowColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        Spacer(Modifier.width(3.dp))
                        Text("⌄", fontSize = 11.sp, color = CoFlowColors.Blue)
                    }
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, shape = RoundedCornerShape(16.dp), containerColor = Color(0xFFF4F8FF).copy(alpha = 0.62f), tonalElevation = 0.dp, shadowElevation = 0.dp, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.82f))) {
                // Берем реальные ID проектов из загруженных задач, убираем дубликаты
                val realProjectIds = tasks.mapNotNull { it.projectRef?.id }.distinct()

                // Формируем итоговый список: базовые фильтры + реальные проекты из БД
                val availableFilters = listOf(
                    stringResource(R.string.filter_none),
                    stringResource(R.string.filter_mine)
                ) + realProjectIds

                availableFilters.forEach { option ->
                    GlassFilterOption(title = option, selected = selectedFilters.contains(option)) { onFilterToggle(option) }
                }
            }
        }
    }
}

@Composable
private fun GlassControlSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(13.dp))
            .background(brush = Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.30f), Color(0xFFDDEAFF).copy(alpha = 0.16f))))
            .border(width = 1.dp, brush = Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.92f), CoFlowColors.Blue.copy(alpha = 0.14f))), shape = RoundedCornerShape(13.dp))
            .padding(horizontal = 9.dp, vertical = 7.dp)
    ) { content() }
}

@Composable
private fun GlassGroupingOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (selected) CoFlowColors.BlueLight.copy(alpha = 0.14f) else Color.Transparent).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
            if (selected) drawCircle(brush = Brush.linearGradient(colors = listOf(Color(0xFF6EA5FF), CoFlowColors.Blue), start = androidx.compose.ui.geometry.Offset.Zero, end = androidx.compose.ui.geometry.Offset(size.width, size.height)))
            else drawCircle(color = CoFlowGlassStyles.BlueBorder, style = Stroke(width = 1.2.dp.toPx()))
        }
        Spacer(Modifier.width(9.dp))
        Text(text = title, fontSize = 13.sp, color = CoFlowColors.TextPrimary, maxLines = 1)
    }
}

@Composable
private fun GlassFilterOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (selected) CoFlowColors.BlueLight.copy(alpha = 0.14f) else Color.Transparent).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp).clip(RoundedCornerShape(5.dp)).background(if (selected) CoFlowColors.Blue else Color.Transparent).border(width = 1.dp, color = if (selected) CoFlowColors.Blue else CoFlowGlassStyles.BlueBorder, shape = RoundedCornerShape(5.dp)), contentAlignment = Alignment.Center) {
            if (selected) Text("✓", fontSize = 10.sp, color = Color.White)
        }
        Spacer(Modifier.width(9.dp))
        Text(text = title, fontSize = 13.sp, color = CoFlowColors.TextPrimary, maxLines = 1)
    }
}

@Preview(name = "Tasks Screen - Phone", showBackground = true, widthDp = 390, heightDp = 844, showSystemUi = true)
@Composable
private fun TasksScreenPreview() {
    CoFlowTheme {
        // ViewModel создается автоматически, параметр tasks удален, так как экран сам управляет данными
        TasksScreen(
            onNavigateToCreateTask = { println("🔥 PREVIEW: Нажата кнопка создания задачи (+)") },
            onNavigateToTaskDetail = { id: String -> println("PREVIEW: Нажата задача с ID: $id") }
        )
    }
}