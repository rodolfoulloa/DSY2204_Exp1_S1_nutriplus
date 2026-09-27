package cl.duoc.rulloa.nutriplus.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Fuerza un refresco del widget de NutriPlus. Los repositorios lo llaman después de escribir
 * cambios que afectan lo que el widget muestra (la minuta del día).
 */
object WidgetRefresher {
    fun requestUpdate(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, NutriPlusWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return
        val intent = Intent(context, NutriPlusWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}
