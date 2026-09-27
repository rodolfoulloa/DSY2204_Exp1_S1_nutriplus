package cl.duoc.rulloa.nutriplus.data.repository

import android.content.Context
import cl.duoc.rulloa.nutriplus.data.Resource
import cl.duoc.rulloa.nutriplus.data.local.MinutaAssignmentEntity
import cl.duoc.rulloa.nutriplus.data.local.MinutaDao
import cl.duoc.rulloa.nutriplus.widget.WidgetRefresher
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Minuta semanal del usuario: /users/{uid}/minuta/{dia}/{recipeId} = fecha de asignación.
 * Cada actualización se refleja en Room (para el Widget) y, tras una escritura exitosa,
 * se pide un refresco inmediato del Widget.
 */
class MinutaRepository(
    private val minutaDao: MinutaDao,
    private val appContext: Context,
    private val database: FirebaseDatabase = Firebase.database
) {
    private fun minutaRef(uid: String) = database.getReference("users").child(uid).child("minuta")

    /** Día -> ids de recetas asignadas ese día. */
    fun observeMinuta(uid: String): Flow<Resource<Map<String, List<String>>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val byDay = snapshot.children.associate { daySnapshot ->
                    val day = daySnapshot.key.orEmpty()
                    val recipeIds = daySnapshot.children.mapNotNull { it.key }
                    day to recipeIds
                }
                trySend(Resource.Success(byDay))
                launch(Dispatchers.IO) {
                    runCatching {
                        val entities = byDay.flatMap { (day, recipeIds) ->
                            recipeIds.map { recipeId ->
                                MinutaAssignmentEntity(uid, day, recipeId, System.currentTimeMillis())
                            }
                        }
                        minutaDao.replaceForUser(uid, entities)
                        // También cubre cambios hechos desde otro dispositivo.
                        WidgetRefresher.requestUpdate(appContext)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.toMessage()))
            }
        }
        minutaRef(uid).addValueEventListener(listener)
        awaitClose { minutaRef(uid).removeEventListener(listener) }
    }

    suspend fun assignRecipe(uid: String, day: String, recipeId: String): Result<Unit> = runCatching {
        minutaRef(uid).child(day).child(recipeId).setValue(ServerValue.TIMESTAMP).await()
        WidgetRefresher.requestUpdate(appContext)
    }

    suspend fun removeRecipe(uid: String, day: String, recipeId: String): Result<Unit> = runCatching {
        minutaRef(uid).child(day).child(recipeId).removeValue().await()
        WidgetRefresher.requestUpdate(appContext)
    }

    /** Reemplaza la receta asignada a un día por otra (usado por "cambiar receta"). */
    suspend fun changeRecipe(uid: String, day: String, oldRecipeId: String, newRecipeId: String): Result<Unit> =
        runCatching {
            minutaRef(uid).child(day).child(oldRecipeId).removeValue().await()
            minutaRef(uid).child(day).child(newRecipeId).setValue(ServerValue.TIMESTAMP).await()
            WidgetRefresher.requestUpdate(appContext)
        }

    /** Ids de recetas asignadas hoy para [uid], leídos directo de la caché local (sin red). */
    fun todaysRecipeIdsSync(uid: String, day: String): List<String> =
        minutaDao.getRecipeIdsForDaySync(uid, day)
}
