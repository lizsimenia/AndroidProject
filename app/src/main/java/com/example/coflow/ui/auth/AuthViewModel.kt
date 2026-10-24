package com.example.coflow.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coflow.data.firebase.AuthRepository
import com.example.coflow.data.firebase.FirebaseAuthRepositoryImpl
import com.example.coflow.models.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    REGISTER
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val username: String = "",          // Имя пользователя
    val contact: String = "",           // Почта или телефон
    val password: String = "",          // Пароль
    val confirmPassword: String = "",   // Подтверждение пароля
    val usernameError: String? = null,  // Ошибка имени
    val contactError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,

    val currentUser: UserModel? = null,
    val isAuthorized: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository = FirebaseAuthRepositoryImpl()
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onUsernameChange(value: String) {
        _state.update { it.copy(username = value, usernameError = null) }
    }

    fun onContactChange(value: String) {
        _state.update { it.copy(contact = value, contactError = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, passwordError = null) }
    }

    fun onConfirmChange(value: String) {
        _state.update { it.copy(confirmPassword = value, confirmError = null) }
    }

    fun onToggleMode() {
        _state.update {
            it.copy(
                mode = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN,
                username = "", contact = "", password = "", confirmPassword = "",
                usernameError = null, contactError = null,
                passwordError = null, confirmError = null, errorMessage = null
            )
        }
    }

    fun onSubmit() {
        if (_state.value.isSubmitting) return

        val current = _state.value

        // Валидация имени (только для регистрации)
        val usernameError = if (current.mode == AuthMode.REGISTER && current.username.isBlank()) {
            "Введите имя пользователя"
        } else if (current.mode == AuthMode.REGISTER && current.username.length < 2) {
            "Имя слишком короткое"
        } else null

        // Валидация Email
        val contactError = when {
            current.contact.isBlank() -> "Введите почту"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(current.contact).matches() -> "Неверный формат email"
            else -> null
        }

        // Валидация пароля (минимум 8 символов, хотя бы одна буква и цифра)
        val passwordError = when {
            current.mode == AuthMode.REGISTER && current.password.length < 8 -> "Пароль должен быть не менее 8 символов"
            current.mode == AuthMode.REGISTER && !current.password.any { it.isDigit() } -> "Пароль должен содержать хотя бы одну цифру"
            current.mode == AuthMode.REGISTER && !current.password.any { it.isLetter() } -> "Пароль должен содержать хотя бы одну букву"
            else -> null
        }

        // Валидация подтверждения пароля
        val confirmError = if (current.mode == AuthMode.REGISTER && current.password != current.confirmPassword) {
            "Пароли не совпадают"
        } else null

        if (usernameError != null || contactError != null || passwordError != null || confirmError != null) {
            _state.update {it.copy(
                    usernameError = usernameError,
                    contactError = contactError,
                    passwordError = passwordError,
                    confirmError = confirmError,
                    isSubmitting = false
                )
            }
            return
        }

        _state.update {it.copy(
                isSubmitting = true,
                usernameError = null,
                contactError = null,
                passwordError = null,
                confirmError = null,
                errorMessage = null
            )
        }

        // Вызов бэкенда
        viewModelScope.launch {
            val result = if (current.mode == AuthMode.REGISTER) {
                repository.register(current.contact, current.password, current.username)
            } else {
                repository.login(current.contact, current.password)
            }

            result.fold(
                onSuccess = { user ->
                    _state.update {it.copy(
                            isSubmitting = false,
                            currentUser = user,
                            isAuthorized = true
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {it.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "Неизвестная ошибка"
                        )
                    }
                }
            )
        }
    }

}