package cl.duoc.rulloa.nutriplus.data.repository

import cl.duoc.rulloa.nutriplus.data.MockData
import cl.duoc.rulloa.nutriplus.data.Recipe
import cl.duoc.rulloa.nutriplus.data.Resource
import cl.duoc.rulloa.nutriplus.data.local.RecipeDao
import cl.duoc.rulloa.nutriplus.data.local.toEntity
import cl.duoc.rulloa.nutriplus.data.local.toRecipe
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Catálogo de recetas (/recipes) y favoritas de cada usuario (/users/{uid}/favorites).
 * Cada actualización del catálogo se refleja también en Room, para que
 * RecipeContentProvider (y el Widget) puedan leerla de forma síncrona y sin red.
 */
class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val database: FirebaseDatabase = Firebase.database
) {
    private val recipesRef get() = database.getReference("recipes")
    private fun favoritesRef(uid: String) = database.getReference("users").child(uid).child("favorites")

    /** Si /recipes está vacío, sube el catálogo semilla una sola vez. */
    suspend fun seedCatalogIfEmpty(): Result<Unit> = runCatching {
        val snapshot = recipesRef.get().await()
        if (!snapshot.exists()) {
            val seed = MockData.weeklyRecipes.associateBy { it.id }
            recipesRef.setValue(seed).await()
        }
    }

    fun observeRecipes(): Flow<Resource<List<Recipe>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipes = snapshot.children.mapNotNull { it.getValue(Recipe::class.java) }
                trySend(Resource.Success(recipes))
                // Refleja el catálogo en Room para que el Provider/Widget lo lean sin red.
                launch(Dispatchers.IO) { runCatching { syncToLocalCache(recipes) } }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.toMessage()))
            }
        }
        recipesRef.addValueEventListener(listener)
        awaitClose { recipesRef.removeEventListener(listener) }
    }

    suspend fun syncToLocalCache(recipes: List<Recipe>) = withContext(Dispatchers.IO) {
        recipeDao.replaceAll(recipes.map { it.toEntity() })
    }

    /** Lectura síncrona desde la caché local (Room), sin red. La usa el Widget. */
    fun getCachedByIdsSync(ids: List<String>): List<Recipe> =
        if (ids.isEmpty()) emptyList() else recipeDao.getByIdsSync(ids).map { it.toRecipe() }

    fun observeFavorites(uid: String): Flow<Resource<Set<String>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(Resource.Success(snapshot.children.mapNotNull { it.key }.toSet()))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.toMessage()))
            }
        }
        favoritesRef(uid).addValueEventListener(listener)
        awaitClose { favoritesRef(uid).removeEventListener(listener) }
    }

    suspend fun setFavorite(uid: String, recipeId: String, isFavorite: Boolean): Result<Unit> = runCatching {
        if (isFavorite) {
            favoritesRef(uid).child(recipeId).setValue(true).await()
        } else {
            favoritesRef(uid).child(recipeId).removeValue().await()
        }
    }
}
