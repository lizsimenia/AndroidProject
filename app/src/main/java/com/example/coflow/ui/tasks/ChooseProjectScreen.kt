package com.example.coflow.ui.tasks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun ChooseProjectScreen(onBackClick: () -> Unit, onConfirmSelection: (String) -> Unit) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedProject by rememberSaveable { mutableStateOf("Проект 1") }
    val projects = remember { listOf("Проект 1", "Проект 2", "Проект 3", "Проект 4", "Проект 5") }
    val filteredProjects = projects.filter { it.contains(searchQuery, ignoreCase = true) }
    val focusManager = LocalFocusManager.current

    BackHandler(onBack = onBackClick)

    Box(modifier = Modifier.fillMaxSize().background(coFlowTasksBackgroundBrush())) {
            Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding().padding(horizontal = CoFlowSpacing.ScreenHorizontal)) {

            CoFlowScreenHeader(
                title = stringResource(R.string.choose_project_header),
                onBackClick = { focusManager.clearFocus(); onBackClick() }
            )

            CoFlowGlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = stringResource(R.string.create_task_choose_project_name),
                leadingIcon = "search",
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.create_task_projects), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextPrimary)
                Text(text = "${filteredProjects.size}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextSecondary)
            }

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(filteredProjects) { project ->
                    val isSelected = project == selectedProject


                    CoFlowGlassCard(
                        isSelected = isSelected,
                        modifier = Modifier.height(52.dp).clickable { selectedProject = project }
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), contentAlignment = Alignment.CenterStart) {
                            Text(
                                text = project, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                color = if (isSelected) CoFlowColors.Blue else CoFlowColors.TextPrimary
                            )
                        }
                    }
                }
            }

            CoFlowPrimaryButton(
                text = stringResource(R.string.done_button),
                onClick = {
                    focusManager.clearFocus()
                    onConfirmSelection(selectedProject)
                },
                enabled = selectedProject.isNotBlank(),
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }
    }
}

@Preview(name = "Выбор проекта", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ChooseProjectScreenPreview() {
    CoFlowTheme { ChooseProjectScreen(onBackClick = {}, onConfirmSelection = {}) }
}