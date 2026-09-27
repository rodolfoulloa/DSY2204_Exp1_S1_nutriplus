package cl.duoc.rulloa.nutriplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface MinutaDao {
    // Síncrono a propósito: lo usa el Widget para resolver la receta de hoy sin red.
    @Query("SELECT recipeId FROM minuta_assignments WHERE uid = :uid AND day = :day")
    fun getRecipeIdsForDaySync(uid: String, day: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(assignments: List<MinutaAssignmentEntity>)

    @Query("DELETE FROM minuta_assignments WHERE uid = :uid")
    fun clearForUser(uid: String)

    @Transaction
    fun replaceForUser(uid: String, assignments: List<MinutaAssignmentEntity>) {
        clearForUser(uid)
        insertAll(assignments)
    }
}
