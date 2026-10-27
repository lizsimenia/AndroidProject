package com.example.coflow.data.firebase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * Интеграционные тесты репозитория задач на Firestore Emulator.
 *
 * Документ создаётся напрямую через Firestore, чтобы проверить чтение и
 * удаление через репозиторий. Данные уникальны для каждого прогона.
 */
@RunWith(AndroidJUnit4::class)
class FirebaseTaskRepositoryImplTest {
    private lateinit var repository: TaskRepository
    private lateinit var db: FirebaseFirestore

    @Before
    fun setup() {
        FirebaseEmulatorConfig.connect()
        repository = FirebaseTaskRepositoryImpl()
        db = FirebaseFirestore.getInstance()
    }

    @Test
    fun readQueriesAndDeleteWorkflow() {
        runBlocking {
            val uniqueSuffix = System.currentTimeMillis()
            val title = "Тестовая задача $uniqueSuffix"
            val projectId = "test_project_$uniqueSuffix"
            val taskId = "test_task_$uniqueSuffix"
            val taskRef = db.collection("Task").document(taskId)
            val schema =
                TaskSchema(
                    id = taskId,
                    projectRef = db.collection("Project").document(projectId),
                    authorRef = db.collection("users").document("test_author"),
                    executorRef = db.collection("users").document("test_assignee"),
                    title = title,
                    description = "Описание задачи для интеграционного теста",
                    deadline = LocalDateTime.of(2026, 1, 1, 12, 0),
                )
            taskRef.set(schema.toMap()).await()

            try {
                val fetchedById = repository.getById(taskId).getOrThrow()
                assertEquals(title, fetchedById.title)
                assertEquals(projectId, fetchedById.projectId)
                assertEquals("test_author", fetchedById.authorId)
                assertEquals("test_assignee", fetchedById.assigneeIds)

                val byTitle = repository.getByTitle(title).getOrThrow()
                assertTrue(
                    "Задача должна находиться по заголовку",
                    byTitle.any { it.id == taskId },
                )

                val byProject = repository.getByProjectId(projectId).getOrThrow()
                assertTrue(
                    "Задача должна находиться по проекту",
                    byProject.any { it.id == taskId },
                )
            } finally {
                repository.deleteTask(taskId)
            }

            assertTrue(
                "Задача должна быть удалена",
                repository.getById(taskId).isFailure,
            )
        }
    }
}
