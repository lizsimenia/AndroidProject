package com.example.coflow.repositories

import com.example.coflow.models.TaskModel

// Интерфейс репозитория для работы с задачами в Firestore
interface TaskRepository {
    suspend fun getById(id: String): Result<TaskModel>
    suspend fun getByTitle(title: String): Result<List<TaskModel>>
    suspend fun getByProjectId(id: String): Result<List<TaskModel>>
    suspend fun addTask(task: TaskModel): Result<TaskModel>
    suspend fun deleteTask(id: String)
}
