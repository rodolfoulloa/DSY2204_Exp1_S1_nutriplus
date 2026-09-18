package com.example.myapplication.ui.auth

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.MockData
import com.example.myapplication.data.Recipe
import com.example.myapplication.data.RecipeContentProvider
import com.example.myapplication.data.User
import com.example.myapplication.data.toRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    // Lógica de búsqueda de recetas
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Las recetas se leen a través del ContentResolver contra RecipeContentProvider, no directo de MockData
    private fun loadRecipesFromProvider(): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        getApplication<Application>().contentResolver
            .query(RecipeContentProvider.CONTENT_URI, null, null, null, null)
            ?.use { cursor ->
                while (cursor.moveToNext()) {
                    recipes.add(cursor.toRecipe())
                }
            }
        return recipes
    }

    private val _recipes = MutableStateFlow(loadRecipesFromProvider())
    val filteredRecipes: StateFlow<List<Recipe>> = combine(_recipes, _searchQuery) { recipes, query ->
        if (query.isBlank()) {
            recipes
        } else {
            recipes.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.day.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _recipes.value
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

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

    fun register(name: String, email: String, password: String, goal: String, gender: String): Boolean {
        return if (MockData.registeredUsers.none { it.email == email }) {
            val newId = (MockData.registeredUsers.maxOfOrNull { it.id } ?: 0) + 1
            MockData.registeredUsers.add(User(newId, email, password, name, goal, gender))
            true
        } else {
            false
        }
    }

    fun clearError() {
        _loginError.value = null
    }
}
