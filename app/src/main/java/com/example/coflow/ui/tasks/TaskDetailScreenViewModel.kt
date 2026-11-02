package com.example.coflow.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.example.coflow.R

enum class TaskStatusUi {
    TODO,
    IN_PROGRESS,
    DONE,
}

data class TaskDetailUiState(
    val id: Int,
    val title: String,
    val status: TaskStatusUi,
    val projectName: String,
    val description: String,
    val author: String,
    val assignees: List<String>,
    val dueDateFormatted: String,
    val isOverdue: Boolean,
    val canEdit: Boolean,
    val showBottomAction: Boolean,
    val bottomActionTextResId: Int,
)

class TaskDetailScreenViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // События для UI (например, навигация или показ Toast)
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _uiState = MutableStateFlow<TaskDetailUiState?>(null)
    val uiState: StateFlow<TaskDetailUiState?> = _uiState.asStateFlow()

    private val taskId: Int

    init {
        // Читаем taskId из маршрута "task_detail/{taskId}"
        taskId = savedStateHandle.get<String>("taskId")?.toIntOrNull() ?: 1
        loadTask(taskId)
    }

    private fun loadTask(taskId: Int) {
        _uiState.value = getMockTask(taskId)
    }

    // --- Обработчики действий ---
    fun onBottomActionClick() {
        val current = _uiState.value ?: return

        // Имитация изменения статуса при нажатии на кнопку
        val newState =
            when (current.status) {
                TaskStatusUi.TODO -> {
                    current.copy(
                        status = TaskStatusUi.IN_PROGRESS,
                        canEdit = true,
                        bottomActionTextResId = R.string.task_action_complete,
                    )
                }

                TaskStatusUi.IN_PROGRESS -> {
                    current.copy(
                        status = TaskStatusUi.DONE,
                        canEdit = false,
                        showBottomAction = false,
                        bottomActionTextResId = 0,
                    )
                }

                TaskStatusUi.DONE -> {
                    current
                } // Ничего не делаем
            }
        _uiState.value = newState
    }

    fun onEditClick() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.NavigateToEdit(taskId))
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.NavigateBack)
        }
    }

    // --- Заглушки данных ---

    private fun getMockTask(taskId: Int): TaskDetailUiState =
        when (taskId) {
            1 -> {
                TaskDetailUiState(
                    id = 1,
                    title = "Обновить иконки в навигации",
                    status = TaskStatusUi.TODO,
                    projectName = "Дизайн-система",
                    description = "",
                    author = "Елена Козлова",
                    assignees = emptyList(),
                    dueDateFormatted = "30.10.26",
                    isOverdue = false,
                    canEdit = true,
                    showBottomAction = true,
                    bottomActionTextResId = R.string.task_action_take,
                )
            }

            3 -> {
                TaskDetailUiState(
                    id = 3,
                    title = "Разработать новый онбординг для пользователей",
                    status = TaskStatusUi.IN_PROGRESS,
                    projectName = "Мобильное приложение",
                    description = "Нужно сделать 3 экрана с анимацией и кнопкой пропуска.",
                    author = "Алексей Смирнов",
                    assignees = listOf("Мария Иванова", "Ты"),
                    dueDateFormatted = "24.10.26",
                    isOverdue = true,
                    canEdit = false,
                    showBottomAction = true,
                    bottomActionTextResId = R.string.task_action_complete,
                )
            }

            else -> {
                TaskDetailUiState(
                    id = 2,
                    title = "Провести ревью кода модуля авторизации",
                    status = TaskStatusUi.DONE,
                    projectName = "Бэкенд",
                    description = "Все замечания исправлены, код утвержден.",
                    author = "Дмитрий Петров",
                    assignees = listOf("Ты"),
                    dueDateFormatted = "20.10.26",
                    isOverdue = false,
                    canEdit = false,
                    showBottomAction = false,
                    bottomActionTextResId = 0,
                )
            }
        }

    // События, которые ViewModel отправляет в Route
    sealed class UiEvent {
        object NavigateBack : UiEvent()

        data class NavigateToEdit(
            val taskId: Int,
        ) : UiEvent()
    }
}
