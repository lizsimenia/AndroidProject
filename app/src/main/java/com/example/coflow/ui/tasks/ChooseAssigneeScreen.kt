package com.example.coflow.ui.tasks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coflow.ui.components.*
import com.example.coflow.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import com.example.coflow.R
import androidx.compose.ui.res.pluralStringResource

data class Assignee(val id: String, val name: String, val email: String, val initials: String, val avatarColor: Color)

@Composable
fun ChooseAssigneeScreen(onBackClick: () -> Unit, onConfirmSelection: (List<Assignee>) -> Unit) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val allAssignees = remember {
        listOf(
            Assignee("1", "Анна Смирнова", "anna.s@example.com", "АС", Color(0xFF6B9AF3)),
            Assignee("2", "Михаил Орлов", "m.orlov@example.com", "МО", Color(0xFFA3B8CC)),
            Assignee("3", "Елена Волкова", "elena.v@example.com", "ЕВ", Color(0xFF86D3B5)),
            Assignee("4", "Дмитрий Соколов", "d.sokolov@example.com", "ДС", Color(0xFF8BB8F8)),
            Assignee("5", "Мария Козлова", "maria.k@example.com", "МК", Color(0xFFAFA3F8))
        )
    }
    var selectedIds by remember { mutableStateOf(setOf("1")) }
    val filteredAssignees = allAssignees.filter { it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true) }
    val focusManager = LocalFocusManager.current

    BackHandler(onBack = onBackClick)

    Box(modifier = Modifier.fillMaxSize().background(coFlowTasksBackgroundBrush())) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding().padding(horizontal = CoFlowSpacing.ScreenHorizontal)) {
            CoFlowScreenHeader(
                title = stringResource(R.string.choose_assignee_header),
                onBackClick = { focusManager.clearFocus(); onBackClick() }
            )

            CoFlowGlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = stringResource(R.string.create_task_choose_assignee_placeholder),
                leadingIcon = "search",
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.create_task_assignee), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextPrimary)
                Text(text = "${allAssignees.size}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextSecondary)
            }

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(filteredAssignees, key = { it.id }) { assignee ->
                    val isSelected = selectedIds.contains(assignee.id)

                    // ИСПРАВЛЕНО: передаем isSelected в карточку
                    CoFlowGlassCard(
                        isSelected = isSelected,
                        modifier = Modifier.clickable {
                            selectedIds = if (isSelected) selectedIds - assignee.id else selectedIds + assignee.id
                        }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(assignee.avatarColor.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(text = assignee.initials, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = assignee.avatarColor)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = assignee.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextPrimary)
                                Spacer(Modifier.height(2.dp))
                                Text(text = assignee.email, fontSize = 12.sp, color = CoFlowColors.TextSecondary)
                            }
                            Box(
                                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CoFlowColors.Blue else Color.White)
                                    .border(1.5.dp, if (isSelected) CoFlowColors.Blue else CoFlowColors.TextSecondary.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) CoFlowIcon(name = "check", modifier = Modifier.size(14.dp), color = Color.White)
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)) {
                val selectedCount = selectedIds.size

                // Определяем текст с учетом специального случая для 0
                val selectedText = if (selectedCount == 0) {
                    stringResource(R.string.no_assignees_selected)
                } else {
                    pluralStringResource(
                        id = R.plurals.selected_assignees_count,
                        count = selectedCount,
                        selectedCount
                    )
                }

                Text(
                    text = selectedText,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
                    fontSize = 12.sp, fontWeight = FontWeight.Medium, color = CoFlowColors.TextPrimary
                )
                CoFlowPrimaryButton(
                    text = stringResource(R.string.add_assignee_button),
                    onClick = {
                        focusManager.clearFocus()
                        onConfirmSelection(allAssignees.filter { selectedIds.contains(it.id) })
                    },
                    enabled = selectedIds.isNotEmpty()
                )
            }
        }
    }
}

@Preview(name = "Выбор исполнителя", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ChooseAssigneeScreenPreview() {
    CoFlowTheme { ChooseAssigneeScreen(onBackClick = {}, onConfirmSelection = {}) }
}