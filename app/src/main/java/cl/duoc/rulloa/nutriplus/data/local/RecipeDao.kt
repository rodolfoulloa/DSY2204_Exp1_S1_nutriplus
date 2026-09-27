package cl.duoc.rulloa.nutriplus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RecipeDao {
    // Síncronos a propósito: los usa RecipeContentProvider.query(), que no puede suspender.
    @Query("SELECT * FROM recipes")
    fun getAllSync(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getByIdSync(id: String): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE id IN (:ids)")
    fun getByIdsSync(ids: List<String>): List<RecipeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(recipes: List<RecipeEntity>)

    @Query("DELETE FROM recipes")
    fun clearAll()

    @Query("DELETE FROM recipes WHERE id = :id")
    fun deleteById(id: String)

    @Transaction
    fun replaceAll(recipes: List<RecipeEntity>) {
        clearAll()
        insertAll(recipes)
    }
}
