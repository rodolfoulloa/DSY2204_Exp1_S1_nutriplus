package com.example.myapplication.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    fun login(email: String, password: String): Boolean {
        val user = MockData.registeredUsers.find { it.email == email && it.password == password }
        return if (user != null) {
            _loginError.value = null
            true
        } else {
            _loginError.value = "Correo o contraseña incorrectos"
            false
        }
    }

    fun clearError() {
        _loginError.value = null
    }
}
