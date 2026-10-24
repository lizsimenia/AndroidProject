package com.example.coflow.data.firebase

// Перечисление статусов задачи для Firestore
enum class TaskStatus(val value: Int) {
    TODO(0),
    IN_PROCESS(1),
    DONE(2);

    companion object {
        fun fromValue(value: Int): TaskStatus {
            return entries.firstOrNull { it.value == value } ?: TODO
        }
    }
}
