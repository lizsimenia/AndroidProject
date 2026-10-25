package com.example.coflow.ui.projects

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.example.coflow.R
import com.example.coflow.data.ProjectsTestData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Статусы для UI
enum class ProjectStatus { IN_PROGRESS, ARCHIVE }
enum class ProjectRole { OWNER, EDITOR, READER }

// Модель проекта
data class ProjectUiItem(
    val id: String,
    val name: String,
    val status: ProjectStatus,
    val role: ProjectRole
)

// Фильтры для выпадающего меню
enum class ProjectFilter(@StringRes val titleResId: Int) {
    ALL(R.string.project_filter_all),
    MANAGEMENT(R.string.project_filter_management),
    PARTICIPATION(R.string.project_filter_participation),
    ARCHIVE(R.string.project_filter_archive)
}

// Состояние экрана
data class ProjectsUiState(
    val searchQuery: String = "",
    val selectedFilter: ProjectFilter = ProjectFilter.ALL,
    val isFilterMenuExpanded: Boolean = false,
    val projects: List<ProjectUiItem> = emptyList(),
    val activeCount: Int = 0,
    val archivedCount: Int = 0
)

class ProjectsViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProjectsUiState())
    val state: StateFlow<ProjectsUiState> = _state.asStateFlow()

    init {
        updateFilteredProjects()
    }

    // Ввод текста в поиск
    fun onSearchQueryChange(newQuery: String) {
        _state.update { it.copy(searchQuery = newQuery) }
        updateFilteredProjects()
    }

    // Открыть/закрыть меню фильтра
    fun onFilterMenuToggle() {
        _state.update { it.copy(isFilterMenuExpanded = !it.isFilterMenuExpanded) }
    }

    // Выбор фильтра
    fun onFilterSelect(filter: ProjectFilter) {
        _state.update { it.copy(selectedFilter = filter, isFilterMenuExpanded = false) }
        updateFilteredProjects()
    }

    // Логика фильтрации
    private fun updateFilteredProjects() {
        val currentState = _state.value

        val filtered = ProjectsTestData.allProjects.filter { project ->
            // Проверка поиска
            val matchesSearch = project.name.contains(currentState.searchQuery, ignoreCase = true)

            // Проверка фильтра
            val matchesFilter = when (currentState.selectedFilter) {
                // Все (кроме архива)
                ProjectFilter.ALL -> project.status == ProjectStatus.IN_PROGRESS

                // Управление: не архив + роль OWNER или EDITOR
                ProjectFilter.MANAGEMENT -> project.status == ProjectStatus.IN_PROGRESS &&
                        (project.role == ProjectRole.OWNER || project.role == ProjectRole.EDITOR)

                // Участие: не архив + роль READER
                ProjectFilter.PARTICIPATION -> project.status == ProjectStatus.IN_PROGRESS &&
                        project.role == ProjectRole.READER

                // Архив (только архивные)
                ProjectFilter.ARCHIVE -> project.status == ProjectStatus.ARCHIVE
            }

            matchesSearch && matchesFilter
        }

        // Считаем общее количество для шапки
        val active = ProjectsTestData.allProjects.count { it.status == ProjectStatus.IN_PROGRESS }
        val archived = ProjectsTestData.allProjects.count { it.status == ProjectStatus.ARCHIVE }

        _state.update {
            it.copy(
                projects = filtered,
                activeCount = active,
                archivedCount = archived
            )
        }
    }
}