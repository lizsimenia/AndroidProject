package com.example.coflow.models

import com.example.coflow.data.firebase.TaskSchema
import com.example.coflow.data.firebase.TaskStatus
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime

// Доменная модель задачи (используется в ViewModel и UI)
data class TaskModel(
    val id: String = "",
    val projectRef: DocumentReference? = null,
    val authorRef: DocumentReference? = null,
    val executorRef: DocumentReference? = null,
    val title: String = "",
    val description: String = "",
    val deadline: LocalDateTime = LocalDateTime.now(),
    val status: TaskStatus = TaskStatus.TODO
) {
    // Метод для конвертации в схему базы данных (требуется кодом одногруппника)
    fun toSchema(db: FirebaseFirestore): TaskSchema {
        return TaskSchema(
            id = id,
            projectRef = projectRef,
            authorRef = authorRef,
            executorRef = executorRef,
            title = title,
            description = description,
            deadline = deadline,
            status = status
        )
    }
}

// Функция расширения для конвертации из Схемы БД в Доменную модель
fun TaskSchema.toDomain(): TaskModel {
    return TaskModel(
        id = this.id,
        projectRef = this.projectRef,
        authorRef = this.authorRef,
        executorRef = this.executorRef,
        title = this.title,
        description = this.description,
        deadline = this.deadline,
        status = this.status
    )
}