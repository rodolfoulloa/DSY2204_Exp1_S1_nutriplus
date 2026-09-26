package cl.duoc.rulloa.nutriplus

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.initialize

/**
 * Application de NutriPlus. Inicializa Firebase una sola vez por proceso y activa la
 * persistencia en disco de Realtime Database, para que la Minuta, el catálogo de recetas
 * y los dispositivos guardados sigan disponibles (en modo lectura) sin conexión.
 */
class NutriPlusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        Firebase.database.setPersistenceEnabled(true)
    }
}
