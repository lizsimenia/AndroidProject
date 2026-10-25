package com.example.coflow.ui.tasks

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Модель данных исполнителя задачи.
data class Assignee(
    val id: String,
    val name: String,
    val email: String,
    val initials: String,
    val avatarColor: Color
)

// Модель задачи
data class CreateTaskFormData(
    val title: String,
    val description: String,
    val assignees: List<Assignee>,
    val project: String,
    val dueDate: String
)

data class CreateTaskUiState(
    // --- Основные поля формы ---
    val title: String = "",
    val description: String = "",

    // --- Исполнители ---
    val assignees: List<Assignee> = emptyList(), // Уже подтверждённые исполнители
    val allAssignees: List<Assignee> = emptyList(),// Полный список доступных исполнителей
    val assigneeSearchQuery: String = "", // Текущий текст в поле поиска
    val pendingAssigneeIds: Set<String> = emptySet(),  // Временно выбранные ID на экране выбора

    // --- Дата и время ---
    val selectedDueDate: LocalDate = LocalDate.now(),
    val calendarMonth: YearMonth = YearMonth.now(),
    val dueDate: String = "",

    // --- Проект ---
    val project: String = "",
    val allProjects: List<String> = emptyList(),
    val projectSearchQuery: String = "",
    val pendingProject: String = "",

    // --- Статус и валидация ---
    val titleError: String? = null,
    val descriptionError: String? = null,
    val isSubmitting: Boolean = false
) {

    /** Отфильтрованный список исполнителей на основе поискового запроса */
    val filteredAssignees: List<Assignee>
        get() = if (assigneeSearchQuery.isBlank()) {
            allAssignees
        } else {
            allAssignees.filter {
                it.name.contains(assigneeSearchQuery, ignoreCase = true) ||
                        it.email.contains(assigneeSearchQuery, ignoreCase = true)
            }
        }
    /** Отфильтрованный список проектов на основе поискового запроса. */
    val filteredProjects: List<String>
        get() = if (projectSearchQuery.isBlank()) allProjects
        else allProjects.filter { it.contains(projectSearchQuery, ignoreCase = true) }

    /** Количество временно выбранных исполнителей (для отображения в UI). */
    val pendingSelectedCount: Int
        get() = pendingAssigneeIds.size

    /** Кнопка "Готово" на экране выбора исполнителей активна, если выбран хотя бы один. */
    val canConfirmAssignees: Boolean
        get() = pendingAssigneeIds.isNotEmpty()

    /** Кнопка "Готово" на экране выбора проекта активна, если проект выбран. */
    val canConfirmProject: Boolean get() = pendingProject.isNotBlank()

    /**
     * Главная кнопка "Создать задачу" активна только при заполнении обязательного поля (название)
     * и когда не идет процесс отправки.
     */
    val canCreate: Boolean
        get() = title.isNotBlank() && !isSubmitting
}

/**
 * ViewModel для управления состоянием создания задачи.
 * Координирует данные между основным экраном и экранами выбора (исполнитель, проект, дата).
 */
class CreateTaskViewModel : ViewModel() {

    private val _state = MutableStateFlow(CreateTaskUiState())
    val state: StateFlow<CreateTaskUiState> = _state.asStateFlow()

    init {
        loadAssignees()
        loadProjects()
    }

    private fun loadAssignees() {
        // TODO: заменить на вызов репозитория
        val demo = listOf(
            Assignee("1", "Анна Смирнова", "anna.s@example.com", "АС", Color(0xFF6B9AF3)),
            Assignee("2", "Михаил Орлов", "m.orlov@example.com", "МО", Color(0xFFA3B8CC)),
            Assignee("3", "Елена Волкова", "elena.v@example.com", "ЕВ", Color(0xFF86D3B5)),
            Assignee("4", "Дмитрий Соколов", "d.sokolov@example.com", "ДС", Color(0xFF8BB8F8)),
            Assignee("5", "Мария Козлова", "maria.k@example.com", "МК", Color(0xFFAFA3F8))
        )

        _state.update { it.copy(allAssignees = demo) }
    }

    private fun loadProjects() {
        // TODO: заменить на вызов репозитория
        val demo = listOf("Проект 1", "Проект 2", "Проект 3", "Проект 4", "Проект 5")
        _state.update { it.copy(allProjects = demo) }
    }

    // --- Обработчики основных полей ---
    fun onTitleChange(value: String) {
        _state.update { it.copy(title = value, titleError = null) }
    }
    fun onDescriptionChange(value: String) {
        // Описание ограничено 250 символами, как и в UI
        _state.update { it.copy(description = value.take(250), descriptionError = null) }
    }

    // --- Обработчики экрана выбора исполнителей ---
    fun onAssigneeSearchQueryChange(query: String) {
        _state.update { it.copy(assigneeSearchQuery = query) }
    }
    fun togglePendingAssignee(assigneeId: String) {
        _state.update { current ->
            val pending = if (assigneeId in current.pendingAssigneeIds) {
                current.pendingAssigneeIds - assigneeId
            } else {
                current.pendingAssigneeIds + assigneeId
            }
            current.copy(pendingAssigneeIds = pending)
        }
    }

    fun confirmAssigneeSelection(assignees: List<Assignee>) {
        _state.update { it.copy(assignees = assignees, pendingAssigneeIds = emptySet()) }
    }

    fun cancelAssigneeSelection() {
        _state.update {
            it.copy(
                pendingAssigneeIds = emptySet(),
                assigneeSearchQuery = ""
            )
        }
    }

    // --- Обработчики экрана выбора проекта ---
    fun onProjectSearchQueryChange(query: String) {
        _state.update { it.copy(projectSearchQuery = query) }
    }

    fun onPendingProjectChange(project: String) {
        _state.update { it.copy(pendingProject = project) }
    }

    fun confirmProjectSelection(project: String) {
        if (project.isNotBlank()) {
            _state.update { it.copy(project = project) }
        }
        _state.update { it.copy(pendingProject = "") }
    }

    fun cancelProjectSelection() {
        _state.update { it.copy(pendingProject = "") }
    }

    // --- Обработчики экрана выбора даты ---
    fun onCalendarMonthChange(month: YearMonth) {
        _state.update { it.copy(calendarMonth = month) }
    }
    fun onDateSelected(date: LocalDate) {
        _state.update { it.copy(selectedDueDate = date) }
    }
    fun confirmDateSelection(dateString: String) {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        _state.update { it.copy(selectedDueDate = date, dueDate = dateString) }
    }

    // --- Финальное действие ---
    fun onCreateTask(onTaskCreated: (CreateTaskFormData) -> Unit) {
        val current = _state.value

        // Валидация: название — обязательное поле
        if (current.title.isBlank()) return

        onTaskCreated(
            CreateTaskFormData(
                title = current.title.trim(),
                description = current.description.trim(),
                assignees = current.assignees,
                project = current.project.trim(),
                dueDate = current.dueDate.trim()
            )
        )
    }
}
