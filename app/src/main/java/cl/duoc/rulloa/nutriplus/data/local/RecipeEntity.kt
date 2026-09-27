package cl.duoc.rulloa.nutriplus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.rulloa.nutriplus.data.Recipe

private const val INGREDIENTS_SEPARATOR = "|"

/**
 * Copia local de una receta del catálogo. RecipeRepository la mantiene sincronizada con
 * /recipes; RecipeContentProvider (que necesita responder de forma síncrona) lee solo de aquí.
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val calories: Int,
    val category: String,
    val day: String,
    val ingredients: String,
    val preparation: String
)

fun Recipe.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    title = title,
    description = description,
    calories = calories,
    category = category,
    day = day,
    ingredients = ingredients.joinToString(INGREDIENTS_SEPARATOR),
    preparation = preparation
)

fun RecipeEntity.toRecipe(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    calories = calories,
    category = category,
    day = day,
    ingredients = if (ingredients.isEmpty()) emptyList() else ingredients.split(INGREDIENTS_SEPARATOR),
    preparation = preparation
)
