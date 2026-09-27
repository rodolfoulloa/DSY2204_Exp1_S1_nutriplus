package cl.duoc.rulloa.nutriplus.data.repository

import com.google.firebase.database.DatabaseError

/** Traduce los errores de Realtime Database a mensajes simples en español. */
fun DatabaseError.toMessage(): String = when (code) {
    DatabaseError.NETWORK_ERROR -> "Sin conexión a internet. Mostrando los últimos datos guardados."
    DatabaseError.PERMISSION_DENIED -> "No tienes permiso para ver esta información. Inicia sesión de nuevo."
    DatabaseError.DISCONNECTED -> "Sin conexión a internet. Mostrando los últimos datos guardados."
    else -> message
}
