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
import com.example.coflow.ui.tasks.*
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
                            val mockTasksState = TasksUiState(
                                activeCount = 5,
                                completedCount = 12,
                                groups = listOf(
                                    TaskGroupUi(
                                        id = "TODO",
                                        title = "К исполнению",
                                        count = 2,
                                        isExpanded = true,
                                        tasks = listOf(
                                            TaskRowUi(1, "Обновить иконки навигации", "Дизайн-система", "30.10.26", isDone = false, isOverdue = false),
                                            TaskRowUi(2, "Согласовать ТЗ с заказчиком", "Сайт", "01.11.26", isDone = false, isOverdue = false)
                                        )
                                    ),
                                    TaskGroupUi(
                                        id = "IN_PROGRESS",
                                        title = "В процессе",
                                        count = 2,
                                        isExpanded = false,
                                        tasks = listOf(
                                            TaskRowUi(3, "Разработать онбординг", "Мобильное приложение", "24.10.26", isDone = false, isOverdue = true),
                                            TaskRowUi(4, "Настроить CI/CD", "Бэкенд", "28.10.26", isDone = false, isOverdue = false)
                                        )
                                    ),
                                    TaskGroupUi(
                                        id = "DONE",
                                        title = "Сделано",
                                        count = 12,
                                        isExpanded = false,
                                        tasks = emptyList()
                                    )
                                ),
                                currentGrouping = TaskGrouping.STATUS,
                                filterButtonText = "Без фильтра",
                                availableFilters = listOf("Без фильтра", "Мои задачи", "Проект: Дизайн-система", "Проект: Мобильное приложение"),
                                isBottomBarVisible = true,
                                selectedFilters = setOf("Мои задачи")
                            )

                            TasksScreen(
                                state = mockTasksState,
                                onNavigateToCreateTask = { navController.navigate(Screen.CREATE_TASK) },
                                onNavigateToTaskDetail = { taskId -> navController.navigate("${Screen.TASK_DETAIL}/$taskId") },
                                onGroupingChange = { /* TODO: Handle grouping change */ },
                                onFilterToggle = { /* TODO: Handle filter toggle */ },
                                onToggleGroup = { /* TODO: Handle group toggle */ },
                                onNotificationClick = { /* TODO: Handle notification click */ },
                                onProjectsClick = { /* TODO: Handle projects click */ },
                                onProfileClick = { /* TODO: Handle profile click */ }
                            )
                        }

                        // Экран просмотра задачи
                        composable(route = Screen.TASK_DETAIL) { backStackEntry ->  val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 1

                            val mockTaskDetailState = when (taskId) {
                                1 -> TaskDetailUiState(
                                    title = "Обновить иконки в навигации",
                                    status = TaskStatusUi.TODO,
                                    projectName = "Дизайн-система",
                                    description = "",
                                    author = "Елена Козлова",
                                    assignees = emptyList(),
                                    dueDateFormatted = "30.10.26",
                                    isOverdue = false,
                                    canEdit = true,
                                    showBottomAction = true,
                                    bottomActionText = "Взять задачу"
                                )
                                3 -> TaskDetailUiState(
                                    title = "Разработать новый онбординг для пользователей",
                                    status = TaskStatusUi.IN_PROGRESS,
                                    projectName = "Мобильное приложение",
                                    description = "Нужно сделать 3 экрана с анимацией и кнопкой пропуска.",
                                    author = "Алексей Смирнов",
                                    assignees = listOf("Мария Иванова", "Ты"),
                                    dueDateFormatted = "24.10.26",
                                    isOverdue = true,
                                    canEdit = false,
                                    showBottomAction = true,
                                    bottomActionText = "Выполнить"
                                )
                                else -> TaskDetailUiState(
                                    title = "Провести ревью кода модуля авторизации",
                                    status = TaskStatusUi.DONE,
                                    projectName = "Бэкенд",
                                    description = "Все замечания исправлены, код утвержден.",
                                    author = "Дмитрий Петров",
                                    assignees = listOf("Ты"),
                                    dueDateFormatted = "20.10.26",
                                    isOverdue = false,
                                    canEdit = false,
                                    showBottomAction = false,
                                    bottomActionText = ""
                                )
                            }

                            TaskDetailScreen(
                                state = mockTaskDetailState,
                                onBackClick = { navController.popBackStack() },
                                onEditClick = { /* TODO: Navigate to edit screen */ },
                                onBottomActionClick = { /* TODO: Handle bottom action */ }
                            )

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