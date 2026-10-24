package com.example.coflow.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coflow.data.firebase.FirebaseTaskRepositoryImpl
import com.example.coflow.models.TaskModel
import com.example.coflow.data.firebase.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TasksUiState {
    object Loading : TasksUiState()
    data class Success(val tasks: List<TaskModel>) : TasksUiState()
    data class Error(val message: String) : TasksUiState()
}

class TaskScreenViewModel(
    private val repository: TaskRepository = FirebaseTaskRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<TasksUiState>(TasksUiState.Loading)
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = TasksUiState.Loading
            try {
                // ВАЖНО: Убедись, что в TaskRepository добавлен метод getAll()
                val tasks = repository.getAll()
                _uiState.value = TasksUiState.Success(tasks)
            } catch (e: Exception) {
                _uiState.value = TasksUiState.Error(e.message ?: "Неизвестная ошибка при загрузке задач")
            }
        }
    }

    fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    // Можно добавить методы addTask / deleteTask позже, когда будет экран создания
}