package cl.duoc.rulloa.nutriplus.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rulloa.nutriplus.data.Recipe
import cl.duoc.rulloa.nutriplus.data.Resource
import cl.duoc.rulloa.nutriplus.data.repository.AuthRepository
import cl.duoc.rulloa.nutriplus.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeDetailState(val recipe: Recipe?, val isFavorite: Boolean)

class RecipeDetailViewModel(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val uid: String? get() = authRepository.currentUid

    private val recipeResource = recipeRepository.observeRecipes()
        .map { resource ->
            when (resource) {
                is Resource.Success -> Resource.Success(resource.data.firstOrNull { it.id == recipeId })
                is Resource.Error -> Resource.Error(resource.message)
                Resource.Loading -> Resource.Loading
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    private val favoritesResource = (uid?.let { recipeRepository.observeFavorites(it) } ?: flowOf(Resource.Success(emptySet())))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    val state: StateFlow<Resource<RecipeDetailState>> = combine(recipeResource, favoritesResource) { recipeRes, favoritesRes ->
        if (recipeRes is Resource.Error) return@combine Resource.Error(recipeRes.message)
        if (recipeRes !is Resource.Success) return@combine Resource.Loading
        val favorites = (favoritesRes as? Resource.Success)?.data.orEmpty()
        Resource.Success(RecipeDetailState(recipeRes.data, recipeRes.data?.id in favorites))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    fun toggleFavorite() {
        val currentUid = uid ?: return
        val isFavoriteNow = (state.value as? Resource.Success)?.data?.isFavorite ?: false
        viewModelScope.launch { recipeRepository.setFavorite(currentUid, recipeId, !isFavoriteNow) }
    }
}
