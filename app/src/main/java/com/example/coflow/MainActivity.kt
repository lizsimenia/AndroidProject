package com.example.coflow

import android.annotation.SuppressLint
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
import com.example.coflow.ui.tasks.TasksScreen
import com.example.coflow.ui.theme.CoFlowTheme

object Screen {
    const val TASKS = "tasks"
    const val CREATE_TASK = "create_task"
    const val CHOOSE_ASSIGNEE = "choose_assignee"
    const val CHOOSE_PROJECT = "choose_project"
    const val CHOOSE_DATE = "choose_date"

    const val TASK_DETAIL = "task_detail/{taskId}"
}

class MainActivity : ComponentActivity() {
    @SuppressLint("ComposableDestinationInComposeScope")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CoFlowTheme {
                var isAuthenticated by remember { mutableStateOf(true) }

                if (isAuthenticated) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Screen.TASKS) {
                        composable(Screen.TASKS) {
                        }

                        // Экран просмотра задачи
                        composable(
                            route = Screen.TASK_DETAIL,
                        ) { backStackEntry ->

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