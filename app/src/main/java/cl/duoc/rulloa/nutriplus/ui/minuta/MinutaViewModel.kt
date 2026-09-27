package cl.duoc.rulloa.nutriplus.ui.minuta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rulloa.nutriplus.data.Recipe
import cl.duoc.rulloa.nutriplus.data.Resource
import cl.duoc.rulloa.nutriplus.data.WEEK_DAYS
import cl.duoc.rulloa.nutriplus.data.repository.AuthRepository
import cl.duoc.rulloa.nutriplus.data.repository.MinutaRepository
import cl.duoc.rulloa.nutriplus.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MinutaDay(val day: String, val recipes: List<Recipe>)

/** Lee la minuta del usuario y el catálogo, y permite asignar/cambiar/quitar recetas por día. */
class MinutaViewModel(
    private val minutaRepository: MinutaRepository,
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val uid: String? get() = authRepository.currentUid

    private val recipesResource = recipeRepository.observeRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    private val minutaResource = (uid?.let { minutaRepository.observeMinuta(it) } ?: flowOf(Resource.Success(emptyMap())))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    val catalogState: StateFlow<Resource<List<Recipe>>> = recipesResource

    /** Cada día de la semana con las recetas ya asignadas, listas para mostrar. */
    val minutaByDayState: StateFlow<Resource<List<MinutaDay>>> = combine(recipesResource, minutaResource) { recipesRes, minutaRes ->
        if (recipesRes is Resource.Error) return@combine Resource.Error(recipesRes.message)
        if (minutaRes is Resource.Error) return@combine Resource.Error(minutaRes.message)
        if (recipesRes !is Resource.Success || minutaRes !is Resource.Success) return@combine Resource.Loading

        val recipesById = recipesRes.data.associateBy { it.id }
        val byDay = WEEK_DAYS.map { day ->
            val ids = minutaRes.data[day].orEmpty()
            MinutaDay(day, ids.mapNotNull { recipesById[it] })
        }
        Resource.Success(byDay)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    fun assign(day: String, recipeId: String) {
        val currentUid = uid ?: return
        viewModelScope.launch { minutaRepository.assignRecipe(currentUid, day, recipeId) }
    }

    fun remove(day: String, recipeId: String) {
        val currentUid = uid ?: return
        viewModelScope.launch { minutaRepository.removeRecipe(currentUid, day, recipeId) }
    }

    fun change(day: String, oldRecipeId: String, newRecipeId: String) {
        val currentUid = uid ?: return
        viewModelScope.launch { minutaRepository.changeRecipe(currentUid, day, oldRecipeId, newRecipeId) }
    }
}
