package cl.duoc.rulloa.nutriplus.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import cl.duoc.rulloa.nutriplus.MainActivity
import cl.duoc.rulloa.nutriplus.R
import cl.duoc.rulloa.nutriplus.data.Recipe
import cl.duoc.rulloa.nutriplus.data.local.AppDatabase
import cl.duoc.rulloa.nutriplus.data.local.toRecipe
import cl.duoc.rulloa.nutriplus.data.todaySpanishDayName
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class NutriPlusWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_nutriplus)
        val uid = Firebase.auth.currentUser?.uid

        val recipe = uid?.let { todaysRecipe(context, it) }
        when {
            uid == null -> {
                views.setTextViewText(R.id.widget_recipe_name, context.getString(R.string.widget_login_required))
                views.setTextViewText(R.id.widget_recipe_calories, "")
                views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_login_required))
            }
            recipe == null -> {
                views.setTextViewText(R.id.widget_recipe_name, context.getString(R.string.widget_no_recipe))
                views.setTextViewText(R.id.widget_recipe_calories, "")
                views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_no_recipe))
            }
            else -> {
                views.setTextViewText(R.id.widget_recipe_name, recipe.title)
                val summary = "${recipe.calories} kcal · ${recipe.category}"
                views.setTextViewText(R.id.widget_recipe_calories, summary)
                views.setContentDescription(R.id.widget_root, "${recipe.title}. $summary. Toca para ver la receta.")
            }
        }

        views.setOnClickPendingIntent(R.id.widget_root, buildPendingIntent(context, widgetId, recipe?.id))
        appWidgetManager.updateAppWidget(widgetId, views)
    }

    /** Abre NutriPlus directo en la receta (deep link) si hay una para hoy; si no, abre la app normal. */
    private fun buildPendingIntent(context: Context, widgetId: Int, recipeId: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            if (recipeId != null) {
                action = Intent.ACTION_VIEW
                data = Uri.parse("nutriplus://recipe/$recipeId")
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return PendingIntent.getActivity(
            context,
            widgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Lectura síncrona desde Room: la minuta del usuario y el catálogo, ambos ya cacheados localmente. */
    private fun todaysRecipe(context: Context, uid: String): Recipe? {
        val database = AppDatabase.getInstance(context)
        val today = todaySpanishDayName()
        val recipeIds = database.minutaDao().getRecipeIdsForDaySync(uid, today)
        if (recipeIds.isEmpty()) return null
        return database.recipeDao().getByIdsSync(recipeIds).firstOrNull()?.toRecipe()
    }
}
