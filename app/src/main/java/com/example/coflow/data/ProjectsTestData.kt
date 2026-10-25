package com.example.coflow.data

import com.example.coflow.ui.projects.ProjectRole
import com.example.coflow.ui.projects.ProjectStatus
import com.example.coflow.ui.projects.ProjectUiItem

object ProjectsTestData {
    val allProjects = listOf(
        ProjectUiItem("1", "Пиво228", ProjectStatus.ARCHIVE, ProjectRole.OWNER),
        ProjectUiItem("2", "Старый дизайн сайта", ProjectStatus.ARCHIVE, ProjectRole.READER),
        ProjectUiItem("3", "Закрытая лабораторная", ProjectStatus.ARCHIVE, ProjectRole.EDITOR),
        ProjectUiItem("4", "Сайт колледжа", ProjectStatus.ARCHIVE, ProjectRole.READER),
        ProjectUiItem("5", "Мобильное приложение CoFlow", ProjectStatus.IN_PROGRESS, ProjectRole.OWNER),
        ProjectUiItem("6", "Дизайн-система", ProjectStatus.IN_PROGRESS, ProjectRole.EDITOR),
        ProjectUiItem("7", "Научная работа", ProjectStatus.IN_PROGRESS, ProjectRole.READER),
        ProjectUiItem("8", "Курсовая работа", ProjectStatus.IN_PROGRESS, ProjectRole.OWNER),
        ProjectUiItem("9", "Парсер вакансий", ProjectStatus.IN_PROGRESS, ProjectRole.EDITOR),
        ProjectUiItem("10", "Бот для Telegram", ProjectStatus.IN_PROGRESS, ProjectRole.READER),
        ProjectUiItem("11", "Анализ рынка", ProjectStatus.IN_PROGRESS, ProjectRole.OWNER),
        ProjectUiItem("12", "Игра на Unity", ProjectStatus.IN_PROGRESS, ProjectRole.EDITOR),
        ProjectUiItem("13", "База данных вуза", ProjectStatus.IN_PROGRESS, ProjectRole.READER),
        ProjectUiItem("14", "Умный дом", ProjectStatus.IN_PROGRESS, ProjectRole.OWNER)
    )
}