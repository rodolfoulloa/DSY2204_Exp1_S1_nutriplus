package com.example.myapplication.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class RecipeContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.myapplication.provider"
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
            addURI(AUTHORITY, "recipes/#", CODE_RECIPE_ID)
        }
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val columns = projection ?: ALL_COLUMNS
        val recipes = when (uriMatcher.match(uri)) {
            CODE_RECIPE_ID -> {
                val id = ContentUris.parseId(uri).toInt()
                MockData.weeklyRecipes.filter { it.id == id }
            }
            CODE_RECIPES -> MockData.weeklyRecipes
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
        val newId = (MockData.weeklyRecipes.maxOfOrNull { it.id } ?: 0) + 1
        MockData.weeklyRecipes.add(values.toRecipe(newId))
        context?.contentResolver?.notifyChange(uri, null)
        return ContentUris.withAppendedId(CONTENT_URI, newId.toLong())
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        requireNotNull(values) { "ContentValues no puede ser nulo" }
        val id = ContentUris.parseId(uri).toInt()
        val index = MockData.weeklyRecipes.indexOfFirst { it.id == id }
        if (index == -1) return 0
        MockData.weeklyRecipes[index] = MockData.weeklyRecipes[index].mergeWith(values)
        context?.contentResolver?.notifyChange(uri, null)
        return 1
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val id = ContentUris.parseId(uri).toInt()
        val removed = MockData.weeklyRecipes.removeAll { it.id == id }
        if (removed) context?.contentResolver?.notifyChange(uri, null)
        return if (removed) 1 else 0
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

    private fun ContentValues.toRecipe(id: Int): Recipe = Recipe(
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
        id = getInt(col(RecipeContentProvider.COLUMN_ID)),
        title = getString(col(RecipeContentProvider.COLUMN_TITLE)),
        description = getString(col(RecipeContentProvider.COLUMN_DESCRIPTION)),
        calories = getInt(col(RecipeContentProvider.COLUMN_CALORIES)),
        category = getString(col(RecipeContentProvider.COLUMN_CATEGORY)),
        day = getString(col(RecipeContentProvider.COLUMN_DAY)),
        ingredients = getString(col(RecipeContentProvider.COLUMN_INGREDIENTS))?.split("|") ?: emptyList(),
        preparation = getString(col(RecipeContentProvider.COLUMN_PREPARATION))
    )
}
