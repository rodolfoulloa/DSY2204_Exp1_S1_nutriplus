package cl.duoc.rulloa.nutriplus.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rulloa.nutriplus.data.Recipe
import cl.duoc.rulloa.nutriplus.data.Resource
import cl.duoc.rulloa.nutriplus.data.repository.AuthRepository
import cl.duoc.rulloa.nutriplus.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeListItem(val recipe: Recipe, val isFavorite: Boolean)

/** Catálogo de recetas (lectura) y favoritas del usuario actual. */
class RecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val uid: String? get() = authRepository.currentUid

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val recipesResource = recipeRepository.observeRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    private val favoritesResource = (uid?.let { recipeRepository.observeFavorites(it) } ?: flowOf(Resource.Success(emptySet())))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    val recipeListState: StateFlow<Resource<List<RecipeListItem>>> =
        combine(recipesResource, favoritesResource, _searchQuery) { recipesRes, favoritesRes, query ->
            if (recipesRes is Resource.Error) return@combine Resource.Error(recipesRes.message)
            if (recipesRes !is Resource.Success) return@combine Resource.Loading
            val favorites = (favoritesRes as? Resource.Success)?.data.orEmpty()
            val filtered = recipesRes.data.filter { recipe ->
                query.isBlank() ||
                    recipe.title.contains(query, ignoreCase = true) ||
                    recipe.day.contains(query, ignoreCase = true) ||
                    recipe.category.contains(query, ignoreCase = true)
            }
            Resource.Success(filtered.map { RecipeListItem(it, favorites.contains(it.id)) })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun toggleFavorite(recipeId: String, isFavorite: Boolean) {
        val currentUid = uid ?: return
        viewModelScope.launch { recipeRepository.setFavorite(currentUid, recipeId, isFavorite) }
    }
}
