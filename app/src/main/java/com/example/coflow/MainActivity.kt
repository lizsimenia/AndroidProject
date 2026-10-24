package com.example.coflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coflow.ui.auth.AuthRoute
import com.example.coflow.ui.tasks.ChooseAssigneeScreen
import com.example.coflow.ui.tasks.ChooseDateScreen
import com.example.coflow.ui.tasks.ChooseProjectScreen
import com.example.coflow.ui.tasks.CreateTaskRoute
import com.example.coflow.ui.tasks.TaskDetailScreen
import com.example.coflow.ui.tasks.TaskDetailViewModel
import com.example.coflow.ui.tasks.TasksScreen
import com.example.coflow.ui.theme.CoFlowTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator

object Screen {
    const val TASKS = "tasks"
    const val CREATE_TASK = "create_task"
    const val CHOOSE_ASSIGNEE = "choose_assignee"
    const val CHOOSE_PROJECT = "choose_project"
    const val CHOOSE_DATE = "choose_date"

    const val TASK_DETAIL = "task_detail/{taskId}"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CoFlowTheme {
                var isAuthenticated by remember { mutableStateOf(false) }

                if (isAuthenticated) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Screen.TASKS) {
                        composable(Screen.TASKS) {
                            TasksScreen(
                                onNavigateToCreateTask = { navController.navigate(Screen.CREATE_TASK) },
                                onNavigateToTaskDetail = { taskId -> navController.navigate("task_detail/$taskId") }
                            )
                        }

                        // Экран просмотра задачи (Загрузка из Firebase через ViewModel)
                        // Экран просмотра задачи (Загрузка из Firebase через ViewModel)
                        composable(
                            route = Screen.TASK_DETAIL,
                            arguments = listOf(androidx.navigation.navArgument("taskId") { type = androidx.navigation.NavType.StringType })
                        ) { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable

                            // Создаем ViewModel для конкретной задачи
                            val viewModel: TaskDetailViewModel = viewModel(
                                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                        return TaskDetailViewModel(taskId) as T
                                    }
                                }
                            )

                            // 🔥 ВАЖНО: Здесь ОБЯЗАТЕЛЬНО должно быть .collectAsState()
                            val task by viewModel.task.collectAsState()

                            if (task != null) {
                                TaskDetailScreen(
                                    task = task!!,
                                    onBackClick = { navController.popBackStack() },
                                    onEditClick = {
                                        android.widget.Toast.makeText(
                                            this@MainActivity,
                                            "Редактирование (в разработке)",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            } else {
                                // Показываем крутилку загрузки, пока задача летит из Firestore
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        composable(Screen.CREATE_TASK) {
                            CreateTaskRoute(
                                onBackClick = { navController.popBackStack() },
                                onTaskCreated = { formData ->
                                    // TODO: Сохранение задачи через TasksRepository
                                    navController.popBackStack()
                                },
                                onNavigateToAssignees = { navController.navigate(Screen.CHOOSE_ASSIGNEE) },
                                onNavigateToProject = { navController.navigate(Screen.CHOOSE_PROJECT) },
                                onNavigateToDate = { navController.navigate(Screen.CHOOSE_DATE) }
                            )
                        }

                        // Экран выбора исполнителей
                        composable(Screen.CHOOSE_ASSIGNEE) {
                            ChooseAssigneeScreen(
                                onBackClick = { navController.popBackStack() },
                                onConfirmSelection = {
                                    // TODO: Здесь нужно получить выбранные данные и передать их обратно (например, через ViewModel)
                                    // Пока просто возвращаемся назад
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Экран выбора проекта
                        composable(Screen.CHOOSE_PROJECT) {
                            ChooseProjectScreen(
                                onBackClick = { navController.popBackStack() },
                                onConfirmSelection = {
                                    // TODO: Обработать выбор проекта
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Экран выбора даты
                        composable(Screen.CHOOSE_DATE) {
                            ChooseDateScreen(
                                onBackClick = { navController.popBackStack() },
                                onConfirmSelection = {
                                    // TODO: Обработать выбор даты
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                } else {
                    AuthRoute(onAuthSuccess = { isAuthenticated = true })
                }
            }
        }
    }
}