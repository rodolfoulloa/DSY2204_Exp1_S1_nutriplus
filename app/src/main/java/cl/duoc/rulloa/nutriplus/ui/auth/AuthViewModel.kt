package cl.duoc.rulloa.nutriplus.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rulloa.nutriplus.data.UserProfile
import cl.duoc.rulloa.nutriplus.data.repository.AuthRepository
import cl.duoc.rulloa.nutriplus.data.repository.UserRepository
import cl.duoc.rulloa.nutriplus.data.repository.toAuthMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null
)

/**
 * Login, registro y recuperación de contraseña contra Firebase Auth. Al registrarse,
 * además crea el perfil del usuario en Realtime Database (/users/{uid}/profile).
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun isEmailValid(email: String): Boolean =
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isPasswordValid(password: String): Boolean = password.length >= 6

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (!isEmailValid(email)) {
            _uiState.update { it.copy(error = "Ingresa un correo electrónico válido.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.toAuthMessage()) }
                }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        goal: String,
        gender: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa tu nombre completo.") }
            return
        }
        if (!isEmailValid(email)) {
            _uiState.update { it.copy(error = "Ingresa un correo electrónico válido.") }
            return
        }
        if (!isPasswordValid(password)) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.register(email, password)
                .onSuccess { user ->
                    val profile = UserProfile(
                        name = name,
                        email = email,
                        goal = goal,
                        gender = gender,
                        createdAt = System.currentTimeMillis()
                    )
                    userRepository.createProfile(user.uid, profile)
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.toAuthMessage()) }
                }
        }
    }

    fun sendPasswordReset(email: String, onSuccess: () -> Unit) {
        if (!isEmailValid(email)) {
            _uiState.update { it.copy(error = "Ingresa un correo electrónico válido.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, infoMessage = "Te enviamos instrucciones a tu correo.")
                    }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.toAuthMessage()) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
