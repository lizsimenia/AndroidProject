package com.example.coflow.ui.tasks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coflow.models.TaskModel
import com.example.coflow.models.TaskStatus
import com.example.coflow.ui.components.*
import com.example.coflow.ui.theme.*
import java.time.format.DateTimeFormatter


enum class TaskStatusUi {
    TODO, IN_PROGRESS, DONE
}

data class TaskDetailUiState(
    val title: String,
    val status: TaskStatusUi,
    val projectName: String,
    val description: String,
    val author: String,
    val assignees: List<String>,
    val dueDateFormatted: String,
    val isOverdue: Boolean,
    val canEdit: Boolean,
    val showBottomAction: Boolean,
    val bottomActionText: String
)
@Composable
fun TaskDetailScreen(
    state: TaskDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onBottomActionClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(coFlowTasksBackgroundBrush())
    ) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding().padding(horizontal = CoFlowSpacing.ScreenHorizontal)) {
            CoFlowScreenHeader(
                title = "Задача",
                onBackClick = onBackClick,
                trailingContent = {
                    // Карандаш (показываем только если статус "в процессе")
                    if (state.canEdit) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CoFlowColors.Blue)
                                .clickable { onEditClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            CoFlowIcon(
                                name = "edit",
                                modifier = Modifier.size(18.dp),
                                color = Color.White
                            )
                        }
                    }
                }
            )

            // Основной контент
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CoFlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // Название
                        Text(
                            text = state.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoFlowColors.TextPrimary
                        )

                        // Теги
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TagChip(text = getStatusText(state.status))
                            TagChip(text = state.projectName)
                        }

                        // Описание
                        Column {
                            Text("Описание", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = state.description.ifBlank { "Описание отсутствует" },
                                fontSize = 13.sp,
                                color = CoFlowColors.TextSecondary
                            )
                        }

                        // Автор
                        Column {
                            Text("Автор задачи", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = state.author.ifBlank { "Неизвестно" },
                                fontSize = 13.sp,
                                color = CoFlowColors.TextSecondary
                            )
                        }

                        // Исполнители
                        Column {
                            Text("Исполнители", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextPrimary)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CoFlowIcon(name = "person", modifier = Modifier.size(24.dp), color = CoFlowColors.TextSecondary)
                                Spacer(Modifier.width(12.dp))

                                val assigneeText = when {
                                    state.assignees.isEmpty() -> "Нет исполнителей"
                                    state.assignees.size == 1 -> state.assignees.first()
                                    else -> "Вы и еще ${state.assignees.size - 1} участник(а)"
                                }

                                Text(
                                    text = assigneeText,
                                    fontSize = 13.sp,
                                    color = CoFlowColors.TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                CoFlowIcon(name = "chevron", modifier = Modifier.size(16.dp), color = CoFlowColors.Blue)
                            }
                        }

                        // Дата
                        Column {
                            Text(
                                "Дата окончания",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CoFlowColors.TextPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CoFlowIcon(
                                    name = "calendar",
                                    modifier = Modifier.size(20.dp),
                                    color = CoFlowColors.TextSecondary
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    state.dueDateFormatted,
                                    fontSize = 13.sp,
                                    color = if (state.isOverdue) CoFlowColors.Error else CoFlowColors.TextPrimary
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Нижняя кнопка
            if (state.showBottomAction) {
                CoFlowPrimaryButton(
                    text = state.bottomActionText,
                    onClick = onBottomActionClick,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun TagChip(text: String) {
    val bgColor =  CoFlowColors.Blue.copy(alpha = 0.15f)
    val textColor =  CoFlowColors.Blue
    Box(
        modifier = Modifier
            .background(bgColor, shape = RoundedCornerShape(8.dp))
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getStatusText(status: TaskStatusUi): String {
    return when (status) {
        TaskStatusUi.TODO -> "К исполнению"
        TaskStatusUi.IN_PROGRESS -> "В процессе"
        TaskStatusUi.DONE -> "Сделано"
    }
}


@Preview(name = "В процессе (Просрочено)", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun TaskDetailScreen_InProgressOverdue_Preview() {
    CoFlowTheme {
        TaskDetailScreen(
            state = TaskDetailUiState(
                title = "Разработать новый онбординг для пользователей",
                status = TaskStatusUi.IN_PROGRESS,
                projectName = "Мобильное приложение",
                description = "Нужно сделать 3 экрана с анимацией и кнопкой пропуска.",
                author = "Алексей Смирнов",
                assignees = listOf("Мария Иванова", "Ты"),
                dueDateFormatted = "24.10.26",
                isOverdue = true, // Дата красная
                canEdit = false,
                showBottomAction = true,
                bottomActionText = "Выполнить"
            ),
            onBackClick = {},
            onEditClick = {},
            onBottomActionClick = {}
        )
    }
}

@Preview(name = "К исполнению (Можно редактировать)", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun TaskDetailScreen_Todo_Preview() {
    CoFlowTheme {
        TaskDetailScreen(
            state = TaskDetailUiState(
                title = "Обновить иконки в навигации",
                status = TaskStatusUi.TODO,
                projectName = "Дизайн-система",
                description = "", // Проверка пустого описания
                author = "Елена Козлова",
                assignees = emptyList(), // Проверка пустых исполнителей
                dueDateFormatted = "30.10.26",
                isOverdue = false,
                canEdit = true, // Покажет карандаш
                showBottomAction = true,
                bottomActionText = "Взять задачу"
            ),
            onBackClick = {},
            onEditClick = {},
            onBottomActionClick = {}
        )
    }
}

@Preview(name = "Сделано (Без нижней кнопки)", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TaskDetailScreen_Done_Preview() {
    CoFlowTheme {
        TaskDetailScreen(
            state = TaskDetailUiState(
                title = "Провести ревью кода модуля авторизации",
                status = TaskStatusUi.DONE,
                projectName = "Бэкенд",
                description = "Все замечания исправлены, код утвержден.",
                author = "Дмитрий Петров",
                assignees = listOf("Ты"),
                dueDateFormatted = "20.10.26",
                isOverdue = false,
                canEdit = false, // Карандаша нет
                showBottomAction = false, // Кнопки внизу нет
                bottomActionText = ""
            ),
            onBackClick = {},
            onEditClick = {},
            onBottomActionClick = {}
        )
    }
}