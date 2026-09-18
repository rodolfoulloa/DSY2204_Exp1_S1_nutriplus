package com.example.myapplication.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.myapplication.R
import com.example.myapplication.data.Recipe
import com.example.myapplication.data.RecipeContentProvider
import com.example.myapplication.data.toRecipe
import java.util.Calendar

class NutriPlusWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val recipe = queryTodayRecipe(context)
        val views = RemoteViews(context.packageName, R.layout.widget_nutriplus)
        if (recipe != null) {
            views.setTextViewText(R.id.widget_recipe_name, recipe.title)
            views.setTextViewText(
                R.id.widget_recipe_calories,
                "${recipe.calories} kcal · ${recipe.category}"
            )
        } else {
            views.setTextViewText(R.id.widget_recipe_name, context.getString(R.string.widget_no_recipe))
            views.setTextViewText(R.id.widget_recipe_calories, "")
        }
        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun queryTodayRecipe(context: Context): Recipe? {
        val recipes = mutableListOf<Recipe>()
        context.contentResolver.query(RecipeContentProvider.CONTENT_URI, null, null, null, null)
            ?.use { cursor ->
                while (cursor.moveToNext()) {
                    recipes.add(cursor.toRecipe())
                }
            }
        val today = todaySpanishDayName()
        return recipes.firstOrNull { it.day.equals(today, ignoreCase = true) } ?: recipes.firstOrNull()
    }

    private fun todaySpanishDayName(): String {
        val days = arrayOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        return days[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]
    }
}
