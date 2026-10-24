package com.example.coflow.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.coflow.models.UserModel
import com.google.firebase.firestore.FieldValue


class FirebaseAuthRepositoryImpl : AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    override suspend fun register(email: String, password: String, name: String): Result<UserModel> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Не удалось получить UID")

            val userSchema = UserSchema(uid = uid, email = email, name = name)

            usersCollection.document(uid).set(
                mapOf(
                    "uid" to userSchema.uid,
                    "email" to userSchema.email,
                    "name" to userSchema.name,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Перечитываем документ, чтобы получить серверный createdAt
            val created = usersCollection.document(uid).get().await()
                .toObject(UserSchema::class.java)
                ?: throw Exception("Не удалось прочитать созданный профиль")

            Result.success(userSchema.toDomain())
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthWeakPasswordException -> "Пароль слишком простой (минимум 6 символов)"
                is FirebaseAuthUserCollisionException -> "Пользователь с такой почтой уже существует"
                else -> e.message ?: "Ошибка регистрации"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun login(email: String, password: String): Result<UserModel> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val uid = auth.currentUser?.uid ?: throw Exception("Не удалось получить UID")

            val doc = usersCollection.document(uid).get().await()

            val userSchema = doc.toObject(UserSchema::class.java)
                ?: throw Exception("Профиль пользователя не найден")

            Result.success(userSchema.toDomain())
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Неверная почта или пароль"
                else -> e.message ?: "Ошибка входа"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<UserModel> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Не удалось получить пользователя")
            val uid = user.uid

            val email = user.email
                ?: throw Exception("Google-аккаунт не содержит email")
            val name = user.displayName ?: "Пользователь Google"

            // Если пользователь заходит впервые — создаем документ в Firestore
            val userDoc = usersCollection.document(uid).get().await()
            val schema = if (userDoc.exists()) {
                userDoc.toObject(UserSchema::class.java)
                    ?: throw Exception("Не удалось прочитать профиль")
            } else {
                val newSchema = UserSchema(uid = uid, name = name, email = email)
                usersCollection.document(uid).set(
                    mapOf(
                        "uid" to newSchema.uid,
                        "email" to newSchema.email,
                        "name" to newSchema.name,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                usersCollection.document(uid).get().await()
                    .toObject(UserSchema::class.java)
                    ?: throw Exception("Не удалось прочитать созданный профиль")
            }

            Result.success(schema.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Ошибка входа через Google"))
        }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }
}