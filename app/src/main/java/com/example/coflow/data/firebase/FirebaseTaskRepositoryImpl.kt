package com.example.coflow.data.firebase

import com.example.coflow.models.TaskModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Реализация репозитория задач для Firestore
class FirebaseTaskRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TaskRepository {

    override suspend fun getAll(): List<TaskModel> {
        val querySnapshot = taskCollection.get().await()
        return querySnapshot.documents.mapNotNull { doc ->
            TaskSchema.fromDocument(doc)?.toDomain()
        }
    }

    private val taskCollection: CollectionReference = db.collection("Task")

    override suspend fun getById(id: String): Result<TaskModel> = try {
        val snap = taskCollection.document(id).get().await()
        val schema = TaskSchema.fromDocument(snap)
            ?: throw Exception("Задача не найдена")
        Result.success(schema.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Ошибка загрузки задачи"))
        }


    override suspend fun getByTitle(title: String): Result<List<TaskModel>> = try {
        val qs = taskCollection.whereEqualTo("title", title).get().await()
        Result.success(qs.documents.mapNotNull { TaskSchema.fromDocument(it)?.toDomain() })
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Ошибка поиска задач"))
    }

    override suspend fun getByProjectId(id: String): Result<List<TaskModel>> = try {
        val projectRef = db.collection("Project").document(id)
        val qs = taskCollection.whereEqualTo("projectRef", projectRef).get().await()
        Result.success(qs.documents.mapNotNull { TaskSchema.fromDocument(it)?.toDomain() })
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка загрузки задач проекта"))
    }

    override suspend fun addTask(task: TaskModel): Result<TaskModel> = try {
        val docRef = taskCollection.document()                        // id генерим на клиенте
        val schema = task.toSchema(db).copy(id = docRef.id)           // ← вот здесь db
        docRef.set(schema.toMap()).await()
        val created = docRef.get().await()
            .toObject(TaskSchema::class.java)
            ?: throw Exception("Не удалось прочитать созданную задачу")
        Result.success(created.toDomain())
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка создания задачи"))
    }


    override suspend fun deleteTask(id: String): Result<Unit> = try {
        taskCollection.document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка удаления задачи"))
    }
}
