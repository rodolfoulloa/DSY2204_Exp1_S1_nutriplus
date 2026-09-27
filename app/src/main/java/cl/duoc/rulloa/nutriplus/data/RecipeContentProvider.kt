package cl.duoc.rulloa.nutriplus.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import cl.duoc.rulloa.nutriplus.data.local.AppDatabase
import cl.duoc.rulloa.nutriplus.data.local.RecipeDao
import cl.duoc.rulloa.nutriplus.data.local.RecipeEntity
import cl.duoc.rulloa.nutriplus.data.local.toEntity
import cl.duoc.rulloa.nutriplus.data.local.toRecipe
import com.google.firebase.database.FirebaseDatabase

/**
 * Respalda las recetas con Room (no con una lista en memoria): query() es síncrono por
 * contrato de ContentProvider, y Room permite servir esos datos sin red. Las llamadas desde
 * otros procesos llegan en hilos Binder; quien lo consulte dentro de la app debe hacerlo
 * fuera del hilo principal (Room lo exige). Las escrituras (insert/update/
 * delete) actualizan Room de inmediato y además se propagan a Firebase en segundo plano,
 * para que el catálogo compartido quede consistente para el resto de los usuarios.
 */
class RecipeContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "cl.duoc.rulloa.nutriplus.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/recipes")

        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_CALORIES = "calories"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DAY = "day"
        const val COLUMN_INGREDIENTS = "ingredients"
        const val COLUMN_PREPARATION = "preparation"

        private const val INGREDIENTS_SEPARATOR = "|"

        val ALL_COLUMNS = arrayOf(
            COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_CALORIES,
            COLUMN_CATEGORY, COLUMN_DAY, COLUMN_INGREDIENTS, COLUMN_PREPARATION
        )

        private const val CODE_RECIPES = 1
        private const val CODE_RECIPE_ID = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "recipes", CODE_RECIPES)
            addURI(AUTHORITY, "recipes/*", CODE_RECIPE_ID)
        }

        fun uriForRecipe(id: String): Uri = CONTENT_URI.buildUpon().appendPath(id).build()
    }

    private lateinit var recipeDao: RecipeDao

    override fun onCreate(): Boolean {
        recipeDao = AppDatabase.getInstance(requireNotNull(context)).recipeDao()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val columns = projection ?: ALL_COLUMNS
        val recipes = when (uriMatcher.match(uri)) {
            CODE_RECIPE_ID -> listOfNotNull(recipeDao.getByIdSync(uri.lastPathSegment.orEmpty()))
            CODE_RECIPES -> recipeDao.getAllSync()
            else -> throw IllegalArgumentException("URI no soportada: $uri")
        }
        val cursor = MatrixCursor(columns)
        recipes.forEach { recipe ->
            cursor.addRow(columns.map { column -> recipe.valueFor(column) })
        }
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String = when (uriMatcher.match(uri)) {
        CODE_RECIPES -> "vnd.android.cursor.dir/vnd.$AUTHORITY.recipes"
        CODE_RECIPE_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.recipes"
        else -> throw IllegalArgumentException("URI no soportada: $uri")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        requireNotNull(values) { "ContentValues no puede ser nulo" }
        val id = values.getAsString(COLUMN_ID)?.takeIf { it.isNotBlank() }
            ?: FirebaseDatabase.getInstance().getReference("recipes").push().key
            ?: error("No se pudo generar un id de receta")
        val recipe = values.toRecipe(id)
        recipeDao.insertAll(listOf(recipe.toEntity()))
        propagateToFirebase(id, recipe)
        context?.contentResolver?.notifyChange(uri, null)
        return uriForRecipe(id)
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        requireNotNull(values) { "ContentValues no puede ser nulo" }
        val id = uri.lastPathSegment ?: return 0
        val current = recipeDao.getByIdSync(id)?.toRecipe() ?: return 0
        val updated = current.mergeWith(values)
        recipeDao.insertAll(listOf(updated.toEntity()))
        propagateToFirebase(id, updated)
        context?.contentResolver?.notifyChange(uri, null)
        return 1
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val id = uri.lastPathSegment ?: return 0
        recipeDao.getByIdSync(id) ?: return 0
        recipeDao.deleteById(id)
        FirebaseDatabase.getInstance().getReference("recipes").child(id).removeValue()
        context?.contentResolver?.notifyChange(uri, null)
        return 1
    }

    /** Escritura en segundo plano: no bloquea al llamador del Provider por una espera de red. */
    private fun propagateToFirebase(id: String, recipe: Recipe) {
        FirebaseDatabase.getInstance().getReference("recipes").child(id).setValue(recipe)
    }

    private fun Recipe.valueFor(column: String): Any = when (column) {
        COLUMN_ID -> id
        COLUMN_TITLE -> title
        COLUMN_DESCRIPTION -> description
        COLUMN_CALORIES -> calories
        COLUMN_CATEGORY -> category
        COLUMN_DAY -> day
        COLUMN_INGREDIENTS -> ingredients.joinToString(INGREDIENTS_SEPARATOR)
        COLUMN_PREPARATION -> preparation
        else -> throw IllegalArgumentException("Columna no soportada: $column")
    }

    private fun RecipeEntity.valueFor(column: String): Any = toRecipe().valueFor(column)

    private fun ContentValues.toRecipe(id: String): Recipe = Recipe(
        id = id,
        title = getAsString(COLUMN_TITLE) ?: "",
        description = getAsString(COLUMN_DESCRIPTION) ?: "",
        calories = getAsInteger(COLUMN_CALORIES) ?: 0,
        category = getAsString(COLUMN_CATEGORY) ?: "",
        day = getAsString(COLUMN_DAY) ?: "",
        ingredients = getAsString(COLUMN_INGREDIENTS)?.split(INGREDIENTS_SEPARATOR) ?: emptyList(),
        preparation = getAsString(COLUMN_PREPARATION) ?: ""
    )

    private fun Recipe.mergeWith(values: ContentValues): Recipe = copy(
        title = values.getAsString(COLUMN_TITLE) ?: title,
        description = values.getAsString(COLUMN_DESCRIPTION) ?: description,
        calories = values.getAsInteger(COLUMN_CALORIES) ?: calories,
        category = values.getAsString(COLUMN_CATEGORY) ?: category,
        day = values.getAsString(COLUMN_DAY) ?: day,
        ingredients = values.getAsString(COLUMN_INGREDIENTS)?.split(INGREDIENTS_SEPARATOR) ?: ingredients,
        preparation = values.getAsString(COLUMN_PREPARATION) ?: preparation
    )
}

fun Cursor.toRecipe(): Recipe {
    fun col(name: String) = getColumnIndexOrThrow(name)
    return Recipe(
        id = getString(col(RecipeContentProvider.COLUMN_ID)),
        title = getString(col(RecipeContentProvider.COLUMN_TITLE)),
        description = getString(col(RecipeContentProvider.COLUMN_DESCRIPTION)),
        calories = getInt(col(RecipeContentProvider.COLUMN_CALORIES)),
        category = getString(col(RecipeContentProvider.COLUMN_CATEGORY)),
        day = getString(col(RecipeContentProvider.COLUMN_DAY)),
        ingredients = getString(col(RecipeContentProvider.COLUMN_INGREDIENTS))?.split("|") ?: emptyList(),
        preparation = getString(col(RecipeContentProvider.COLUMN_PREPARATION))
    )
}
