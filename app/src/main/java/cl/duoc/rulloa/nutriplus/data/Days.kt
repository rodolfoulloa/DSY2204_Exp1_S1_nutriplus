package cl.duoc.rulloa.nutriplus.data

import java.util.Calendar

/** Los 7 días de la semana en español, en el orden en que se muestran en la Minuta. */
val WEEK_DAYS = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

/** Nombre del día de hoy en español, tal como lo espera Recipe.day y la Minuta. */
fun todaySpanishDayName(): String {
    val days = arrayOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    return days[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]
}
