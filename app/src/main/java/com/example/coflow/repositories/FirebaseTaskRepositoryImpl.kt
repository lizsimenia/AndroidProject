package com.example.coflow.repositories

import com.example.coflow.models.TaskModel
import com.example.coflow.models.TaskStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class FirebaseTaskRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TaskRepository {

    private val taskCollection: CollectionReference = db.collection("tasks")
    private val projectCollection: CollectionReference = db.collection("projects")
    private val userCollection: CollectionReference = db.collection("users")

    override suspend fun getById(id: String): Result<TaskModel> = try {
        val snap = taskCollection.document(id).get().await()
        val task = fromDocument(snap) ?: throw Exception("Задача не найдена")
        Result.success(task)
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка загрузки задачи"))
    }

    override suspend fun getByTitle(title: String): Result<List<TaskModel>> = try {
        val qs = taskCollection.whereEqualTo("title", title).get().await()
        Result.success(qs.documents.mapNotNull { fromDocument(it) })
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка поиска задач"))
    }

    override suspend fun getByProjectId(id: String): Result<List<TaskModel>> = try {
        val projectRef = projectCollection.document(id)
        val qs = taskCollection.whereEqualTo("projectId", projectRef).get().await()
        Result.success(qs.documents.mapNotNull { fromDocument(it) })
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка загрузки задач проекта"))
    }

    override suspend fun addTask(task: TaskModel): Result<TaskModel> = try {
        val docRef = taskCollection.document()
        val toWrite = task.copy(id = docRef.id)
        docRef.set(toWrite.toMap()).await()

        val created = fromDocument(docRef.get().await())
            ?: throw Exception("Не удалось прочитать созданную задачу")

        Result.success(created)
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка создания задачи"))
    }

    override suspend fun deleteTask(id: String) {
        taskCollection.document(id).delete().await()
    }

    private fun TaskModel.toMap(): Map<String, Any> = buildMap {
        put("title", title)
        put("description", description)
        put("status", status.code.toLong())
        put("deadline", Timestamp(
            deadline.toEpochSecond(ZoneOffset.UTC),
            deadline.nano
        ))
        if (projectId.isNotBlank()) put("projectId", projectCollection.document(projectId))
        if (authorId.isNotBlank()) put("authorId", userCollection.document(authorId))
        if (assigneeId.isNotBlank()) put("assigneeId", userCollection.document(assigneeId))
    }

    companion object {
        fun fromDocument(doc: DocumentSnapshot): TaskModel? {
            if (!doc.exists()) return null

            val deadlineLocal = doc.getTimestamp("deadline")?.let {
                LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(it.seconds, it.nanoseconds.toLong()),
                    ZoneOffset.UTC
                )
            } ?: LocalDateTime.now(ZoneOffset.UTC)

            val statusInt = (doc.getLong("status") ?: 0L).toInt()

            return TaskModel(
                id = doc.id,
                projectId = doc.getDocumentReference("projectId")?.id.orEmpty(),
                authorId = doc.getDocumentReference("authorId")?.id.orEmpty(),
                assigneeId = doc.getDocumentReference("assigneeId")?.id.orEmpty(),
                title = doc.getString("title").orEmpty(),
                description = doc.getString("description").orEmpty(),
                deadline = deadlineLocal,
                status = TaskStatus.fromInt(statusInt)
            )
        }
    }
}