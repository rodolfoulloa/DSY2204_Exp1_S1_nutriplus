package cl.duoc.rulloa.nutriplus.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rulloa.nutriplus.data.Resource
import cl.duoc.rulloa.nutriplus.data.UserProfile
import cl.duoc.rulloa.nutriplus.data.repository.AuthRepository
import cl.duoc.rulloa.nutriplus.data.repository.UserRepository
import cl.duoc.rulloa.nutriplus.data.repository.toAuthMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileActionState(val isLoading: Boolean = false, val error: String? = null)

/** Perfil del usuario: editar nombre, cerrar sesión y eliminar la cuenta por completo. */
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val uid: String? get() = authRepository.currentUid

    val profileState: StateFlow<Resource<UserProfile?>> =
        (uid?.let { userRepository.observeProfile(it) } ?: flowOf(Resource.Success(null)))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    private val _actionState = MutableStateFlow(ProfileActionState())
    val actionState: StateFlow<ProfileActionState> = _actionState.asStateFlow()

    fun updateName(newName: String) {
        val currentUid = uid ?: return
        if (newName.isBlank()) {
            _actionState.update { it.copy(error = "El nombre no puede estar vacío.") }
            return
        }
        viewModelScope.launch {
            userRepository.updateName(currentUid, newName)
                .onFailure { e -> _actionState.update { it.copy(error = e.toAuthMessage()) } }
        }
    }

    fun logout() = authRepository.logout()

    fun deleteAccount(onResult: (Result<Unit>) -> Unit) {
        val currentUid = uid
        if (currentUid == null) {
            onResult(Result.failure(IllegalStateException("No hay una sesión activa.")))
            return
        }
        _actionState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Primero se borran los datos en Realtime Database (las reglas exigen
            // auth.uid === $uid) y recién después se elimina la cuenta de Firebase Auth.
            val dataResult = userRepository.deleteUserData(currentUid)
            if (dataResult.isFailure) {
                val error = dataResult.exceptionOrNull()?.toAuthMessage() ?: "No se pudo eliminar tu cuenta."
                _actionState.update { it.copy(isLoading = false, error = error) }
                onResult(Result.failure(dataResult.exceptionOrNull() ?: Exception(error)))
                return@launch
            }
            authRepository.deleteAccount()
                .onSuccess {
                    _actionState.update { it.copy(isLoading = false) }
                    onResult(Result.success(Unit))
                }
                .onFailure { e ->
                    _actionState.update { it.copy(isLoading = false, error = e.toAuthMessage()) }
                    onResult(Result.failure(e))
                }
        }
    }

    fun clearError() {
        _actionState.update { it.copy(error = null) }
    }
}
