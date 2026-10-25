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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coflow.models.TaskStatus
import com.example.coflow.ui.components.*
import com.example.coflow.ui.theme.*
import java.time.format.DateTimeFormatter
import com.example.coflow.models.TaskModel
import java.time.LocalDate
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.example.coflow.R
import androidx.compose.ui.res.pluralStringResource

@Composable
fun TaskDetailScreen(
    task: TaskModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    BackHandler(onBack = onBackClick)
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yy")
    val taskDate = task.deadline.toLocalDate() // Конвертируем LocalDateTime в LocalDate
    val today = LocalDate.now()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(coFlowTasksBackgroundBrush())
    ) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding().padding(horizontal = CoFlowSpacing.ScreenHorizontal)) {
            CoFlowScreenHeader(
                title = stringResource(R.string.task_description_header),
                onBackClick = onBackClick,
                trailingContent = {
                    // Карандаш (показываем только если статус TODO)
                    if (task.status == TaskStatus.TODO) {
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
                            text = task.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoFlowColors.TextPrimary
                        )

                        // Теги
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TagChip(text = getStatusText(task.status))
                            TagChip(text = task.projectId ?: "Мое")
                        }

                        // Описание
                        Column {
                            Text(stringResource(R.string.description_header), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = task.description.ifBlank { stringResource(R.string.description_placeholder) },
                                fontSize = 13.sp,
                                color = CoFlowColors.TextSecondary
                            )
                        }

                        // Автор
                        Column {
                            Text(stringResource(R.string.author_header), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = task.authorId ?: stringResource(R.string.author_placeholder),
                                fontSize = 13.sp,
                                color = CoFlowColors.TextSecondary
                            )
                        }

                        // Исполнители
                        Column {
                            Text(stringResource(R.string.assignees_header), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextPrimary)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CoFlowIcon(name = "person", modifier = Modifier.size(24.dp), color = CoFlowColors.TextSecondary)
                                Spacer(Modifier.width(12.dp))
//                                val assigneesList = task.assignees
//
//                                val assigneeText = when {
//                                    task.executorId.isEmpty() -> stringResource(R.string.assignees_placeholder)
//                                    task.executorId.size == 1 -> task.executorId.first()
//                                    else ->
//                                    {
//                                        val assignees_count = task.executorId.size - 1
//                                        pluralStringResource(
//                                            id = R.plurals.task_selected_assignees_count,
//                                            count = assignees_count,
//                                            assignees_count
//                                        )
//                                    }
 //                               }

//                                Text(
//                                    text = assigneeText,
//                                    fontSize = 13.sp,
//                                    color = CoFlowColors.TextPrimary,
//                                    modifier = Modifier.weight(1f)
//                                )
                                CoFlowIcon(name = "chevron", modifier = Modifier.size(16.dp), color = CoFlowColors.Blue)
                            }
                        }

                        // Дата
                        Column {
                            Text(
                                stringResource(R.string.deadline_header),
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
                                    taskDate.format(formatter),
                                    fontSize = 13.sp,
                                    color = if (taskDate.isBefore(java.time.LocalDate.now()) && task.status != TaskStatus.DONE) CoFlowColors.Error else CoFlowColors.TextPrimary
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Нижняя кнопка
            if (task.status != TaskStatus.DONE) {
                CoFlowPrimaryButton(
                    text = if (task.status == TaskStatus.TODO) "Взять задачу" else "Выполнить",
                    onClick = { /* TODO: София подвяжет смену статуса */ },
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


private fun getStatusText(status: TaskStatus): String =
    when (status) {
        TaskStatus.TODO -> "К исполнению"
        TaskStatus.IN_PROCESS -> "В процессе"
        TaskStatus.DONE -> "Сделано"
    }