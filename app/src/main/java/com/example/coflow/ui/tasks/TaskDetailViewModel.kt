package com.example.coflow.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coflow.data.firebase.FirebaseTaskRepositoryImpl
import com.example.coflow.data.firebase.TaskRepository
import com.example.coflow.models.TaskModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskId: String,
    private val repository: TaskRepository = FirebaseTaskRepositoryImpl()
) : ViewModel() {

    // Тип четко указан: TaskModel?
    private val _task = MutableStateFlow<TaskModel?>(null)
    val task: StateFlow<TaskModel?> = _task.asStateFlow()

    init {
        loadTask()
    }

    private fun loadTask() {
        viewModelScope.launch {
            val result = repository.getById(taskId)

            // Распаковываем Result: если успех, кладем задачу, если ошибка - оставляем null
            result.onSuccess { taskModel ->
                _task.value = taskModel
            }.onFailure { exception ->
                exception.printStackTrace()
                _task.value = null
            }
        }
    }
}