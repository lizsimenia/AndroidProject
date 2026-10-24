package com.example.coflow.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

// Модель данных задачи для Firestore
data class TaskSchema(
    val id: String = "",
    val projectRef: DocumentReference? = null,
    val authorRef: DocumentReference? = null,
    val executorRef: DocumentReference? = null,
    val title: String = "",
    val description: String = "",
    val deadline: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    val status: TaskStatus = TaskStatus.TODO
) {
    fun toMap(): Map<String, Any?> {
        val instant = deadline.toInstant(ZoneOffset.UTC)
        val timestamp = Timestamp(instant.epochSecond, instant.nano)
        return mapOf(
            "projectRef" to projectRef,
            "authorRef" to authorRef,
            "executorRef" to executorRef,
            "title" to title,
            "description" to description,
            "deadline" to timestamp,
            "status" to status.value
        )
    }

    companion object {
        private fun safeGetRef(doc: DocumentSnapshot, field: String): DocumentReference? {
            return try {
                doc.getDocumentReference(field)
            } catch (e: Exception) {
                null // Если в базе лежит строка вместо ссылки, просто вернем null, а не краш
            }
        }
        fun fromDocument(doc: DocumentSnapshot): TaskSchema? {
            if (!doc.exists()) return null

            val deadlineTimestamp = doc.getTimestamp("deadline")
            val deadlineLocal = if (deadlineTimestamp != null) {
                LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(deadlineTimestamp.seconds, deadlineTimestamp.nanoseconds.toLong()),
                    ZoneOffset.UTC
                )
            } else {
                LocalDateTime.now(ZoneOffset.UTC)
            }

            val statusInt = (doc.getLong("status") ?: 0L).toInt()

            return TaskSchema( // или Task(
                id = doc.id,
                projectRef = safeGetRef(doc, "projectRef"), // <-- ИСПРАВЛЕНО
                authorRef = safeGetRef(doc, "authorRef"),   // <-- ИСПРАВЛЕНО
                executorRef = safeGetRef(doc, "executorRef"), // <-- ИСПРАВЛЕНО
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                deadline = deadlineLocal,
                status = TaskStatus.fromValue(statusInt)
            )

        }
    }

}
