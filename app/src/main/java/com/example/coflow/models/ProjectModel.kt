package com.example.coflow.models

import java.time.LocalDateTime

data class ProjectModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val updatedAt: LocalDateTime = LocalDateTime.now()
)