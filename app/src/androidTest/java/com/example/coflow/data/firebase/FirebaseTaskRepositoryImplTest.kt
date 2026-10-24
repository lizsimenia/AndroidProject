package com.example.coflow.data.firebase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
class FirebaseTaskRepositoryImplTest {

    private lateinit var repository: TaskRepository
    private lateinit var db: FirebaseFirestore

    @Before
    fun setup() {
        repository = FirebaseTaskRepositoryImpl()
        db = FirebaseFirestore.getInstance()
    }

    @Test
    fun testTaskRepositoryWorkflow_Success() = runBlocking {
        val testProjectId = "test_project_${System.currentTimeMillis()}"
        val projectRef = db.collection("Project").document(testProjectId)

        val newTaskSchema = TaskSchema(
            projectRef = projectRef,
            title = "Тестовая задача Firestore",
            description = "Описание задачи для интеграционного теста",
            deadline = LocalDateTime.now(ZoneOffset.UTC),
            status = TaskStatus.IN_PROCESS
        )

        var createdTaskId: String? = null

        try {
            // 1. Тестируем добавление задачи
            val addedTask = repository.addTask(newTaskSchema)
            createdTaskId = addedTask.id
            assertTrue("ID созданной задачи должен быть заполнен", createdTaskId.isNotBlank())
            assertEquals("Тестовая задача Firestore", addedTask.title)

            // 2. Тестируем получение по ID
            val fetchedById = repository.getById(createdTaskId)
            assertNotNull("Задача должна существовать в БД по ID", fetchedById)
            assertEquals("Тестовая задача Firestore", fetchedById?.title)
            assertEquals(TaskStatus.IN_PROCESS, fetchedById?.status)

            // 3. Тестируем получение по названию
            val tasksByTitle = repository.getByTitle("Тестовая задача Firestore")
            assertTrue("Должна найтись хотя бы одна задача по заголовку", tasksByTitle.isNotEmpty())
            assertTrue("Созданная задача должна присутствовать в результатах", tasksByTitle.any { it.id == createdTaskId })

            // 4. Тестируем получение по ID проекта
            val tasksByProject = repository.getByProjectId(testProjectId)
            assertTrue("Должна найтись задача по ID проекта", tasksByProject.isNotEmpty())
            assertTrue("Задача с нужным ID должна быть найдена", tasksByProject.any { it.id == createdTaskId })

        } finally {
            // 5. Очистка созданной задачи в БД после окончания теста
            if (!createdTaskId.isNullOrBlank()) {
                repository.deleteTask(createdTaskId)
                val checkDeleted = repository.getById(createdTaskId)
                assertNull("Задача должна быть удалена из Firestore", checkDeleted)
            }
        }
    }
}
