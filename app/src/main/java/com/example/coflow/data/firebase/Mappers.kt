package com.example.coflow.data.firebase

import com.example.coflow.models.TaskModel
import com.example.coflow.models.UserModel
import com.google.firebase.firestore.FirebaseFirestore

fun UserSchema.toDomain() : UserModel{
    return UserModel(
        id = this.uid,
        name = this.name,
        email = this.email
    )
}

fun UserModel.toSchema() : UserSchema{
    return UserSchema(
        uid = this.id,
        name = this.name,
        email = this.email
    )
}

fun TaskSchema.toDomain(): TaskModel {
    return TaskModel(
        id = this.id,
        title = this.title,
        description = this.description,
        projectRef = this.projectRef,    // ← Просто копируем ссылку напрямую
        authorRef = this.authorRef,      // ← Просто копируем ссылку напрямую
        executorRef = this.executorRef,  // ← Просто копируем ссылку напрямую
        deadline = this.deadline,
        status = this.status
    )
}

fun TaskModel.toSchema(db: FirebaseFirestore): TaskSchema {
    return TaskSchema(
        id = this.id,
        projectRef = this.projectRef,    // ← Просто копируем ссылку напрямую
        authorRef = this.authorRef,      // ← Просто копируем ссылку напрямую
        executorRef = this.executorRef,  // ← Просто копируем ссылку напрямую
        title = this.title,
        description = this.description,
        deadline = this.deadline,
        status = this.status
    )
}