package cl.duoc.rulloa.nutriplus.data.local

import androidx.room.Entity

/**
 * Copia local de la asignación "receta -> día" del usuario actual (/users/{uid}/minuta).
 * Solo se usa para que el Widget pueda leer, de forma síncrona, la receta de hoy.
 */
@Entity(tableName = "minuta_assignments", primaryKeys = ["uid", "day", "recipeId"])
data class MinutaAssignmentEntity(
    val uid: String,
    val day: String,
    val recipeId: String,
    val assignedAt: Long
)
