package com.example.coflow.ui.tasks

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.coflow.R

enum class TaskGrouping { STATUS, DEADLINE }

// Добавляем типы фильтров для безопасной логики без строковых сравнений
enum class FilterType {
    NO_FILTER,
    MY_TASKS,
    PROJECT
}

// Связка типа фильтра и его строкового ресурса
data class FilterOption(
    val type: FilterType,
    val titleResId: Int,
    val projectName: String? = null
)

data class TaskRowUi(
    val id: Int,
    val title: String,
    val projectName: String,
    val dueDateFormatted: String,
    val isDone: Boolean,
    val isOverdue: Boolean,
)

data class TaskGroupUi(
    val id: String,
    val titleResId: Int,
    val count: Int,
    val isExpanded: Boolean,
    val tasks: List<TaskRowUi>,
)

data class TasksUiState(
    val activeCount: Int,
    val completedCount: Int,
    val groups: List<TaskGroupUi>,
    val currentGrouping: TaskGrouping,
    val selectedFilters: Set<FilterType>,
    val availableFilters: List<FilterOption>,
    val isBottomBarVisible: Boolean,
)

class TasksScreenViewModel : ViewModel() {
    // Данные для заглушки
    private val stubTasks =
        listOf(
            TaskRowUi(1, "Обновить иконки навигации", "Дизайн-система", "30.10.26", isDone = false, isOverdue = false),
            TaskRowUi(2, "Согласовать ТЗ с заказчиком", "Сайт", "01.11.26", isDone = false, isOverdue = false),
            TaskRowUi(3, "Разработать онбординг", "Мобильное приложение", "24.10.26", isDone = false, isOverdue = true),
            TaskRowUi(4, "Настроить CI/CD", "Бэкенд", "28.10.26", isDone = false, isOverdue = false),
            TaskRowUi(5, "Провести ревью кода", "Бэкенд", "20.10.26", isDone = true, isOverdue = false),
            TaskRowUi(6, "Написать тесты", "Бэкенд", "22.10.26", isDone = true, isOverdue = false),
        )

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private fun createInitialState(): TasksUiState {
        val activeTasks = stubTasks.count { !it.isDone }
        val completedTasks = stubTasks.count { it.isDone }

        return TasksUiState(
            activeCount = activeTasks,
            completedCount = completedTasks,
            groups = groupTasksByStatus(stubTasks),
            currentGrouping = TaskGrouping.STATUS,
            selectedFilters = setOf(FilterType.NO_FILTER),
            availableFilters = listOf(
                FilterOption(FilterType.NO_FILTER, R.string.filter_none),
                FilterOption(FilterType.MY_TASKS, R.string.filter_mine),
                FilterOption(FilterType.PROJECT, R.string.filter_project)
            ),
            isBottomBarVisible = true,
        )
    }

    // --- Обработчики событий от UI ---

    fun onGroupingChange(grouping: TaskGrouping) {
        _uiState.update { state ->
            val newGroups =
                when (grouping) {
                    TaskGrouping.STATUS -> groupTasksByStatus(stubTasks)
                    TaskGrouping.DEADLINE -> groupTasksByDeadline(stubTasks)
                }
            state.copy(
                currentGrouping = grouping,
                groups = newGroups,
            )
        }
    }

    fun onFilterToggle(filterType: FilterType) {
        _uiState.update { state ->
            val newFilters = state.selectedFilters.toMutableSet()

            if (filterType == FilterType.NO_FILTER) {
                newFilters.clear()
                newFilters.add(FilterType.NO_FILTER)
            } else {
                // Убираем "Без фильтра", если выбран конкретный фильтр
                newFilters.remove(FilterType.NO_FILTER)

                if (newFilters.contains(filterType)) {
                    newFilters.remove(filterType)
                } else {
                    newFilters.add(filterType)
                }

                // Если сняли все фильтры, возвращаем состояние "Без фильтра"
                if (newFilters.isEmpty()) {
                    newFilters.add(FilterType.NO_FILTER)
                }
            }

            state.copy(selectedFilters = newFilters)
        }
    }

    fun onToggleGroup(groupId: String) {
        _uiState.update { state ->
            val updatedGroups =
                state.groups.map { group ->
                    if (group.id == groupId) {
                        group.copy(isExpanded = !group.isExpanded)
                    } else {
                        group
                    }
                }
            state.copy(groups = updatedGroups)
        }
    }

    fun onNotificationClick() {
        // Заглушка: здесь можно отправить событие или ничего не делать
    }

    // --- Вспомогательные функции для группировки ---

    private fun groupTasksByStatus(tasks: List<TaskRowUi>): List<TaskGroupUi> {
        val todo = tasks.filter { !it.isDone && !it.isOverdue }
        // Упрощение для заглушки: считаем "В процессе" те задачи, которые не выполнены, но просрочены
        val inProgress = tasks.filter { !it.isDone && it.isOverdue }
        val done = tasks.filter { it.isDone }

        return listOf(
            TaskGroupUi("TODO", R.string.tasks_group_planned, todo.size, isExpanded = true, tasks = todo),
            TaskGroupUi("IN_PROGRESS", R.string.tasks_group_active, inProgress.size, isExpanded = false, tasks = inProgress),
            TaskGroupUi("DONE", R.string.tasks_group_done, done.size, isExpanded = false, tasks = done),
        )
    }

    private fun groupTasksByDeadline(tasks: List<TaskRowUi>): List<TaskGroupUi> {
        val overdue = tasks.filter { it.isOverdue && !it.isDone }
        val upcoming = tasks.filter { !it.isOverdue && !it.isDone }
        val done = tasks.filter { it.isDone }

        return listOf(
            TaskGroupUi("OVERDUE", R.string.tasks_group_overdue, overdue.size, isExpanded = true, tasks = overdue),
            TaskGroupUi("UPCOMING", R.string.tasks_group_today, upcoming.size, isExpanded = false, tasks = upcoming),
            TaskGroupUi("COMPLETED", R.string.tasks_group_this_week, done.size, isExpanded = false, tasks = done),
        )
    }
}
