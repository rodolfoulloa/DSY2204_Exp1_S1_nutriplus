package cl.duoc.rulloa.nutriplus

import android.app.Application
import cl.duoc.rulloa.nutriplus.data.ServiceLocator
import cl.duoc.rulloa.nutriplus.widget.WidgetRefresher
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.initialize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * Application de NutriPlus. Inicializa Firebase una sola vez por proceso, activa la
 * persistencia en disco de Realtime Database y mantiene una sincronización de fondo
 * hacia Room mientras el proceso está vivo, para que RecipeContentProvider y el Widget
 * (que solo pueden leer de forma síncrona) tengan siempre el catálogo y la minuta
 * del día al alcance sin depender de que una pantalla esté abierta.
 */
class NutriPlusApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        Firebase.database.setPersistenceEnabled(true)
        ServiceLocator.init(this)
        startBackgroundSync()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startBackgroundSync() {
        val recipeRepository = ServiceLocator.recipeRepository
        val minutaRepository = ServiceLocator.minutaRepository
        val authRepository = ServiceLocator.authRepository

        applicationScope.launch {
            recipeRepository.seedCatalogIfEmpty()
        }
        applicationScope.launch {
            recipeRepository.observeRecipes().collectLatest { /* efecto lateral: escribe en Room */ }
        }
        applicationScope.launch {
            // Login, logout y cuenta eliminada cambian lo que muestra el widget.
            authRepository.observeAuthState().collectLatest { WidgetRefresher.requestUpdate(this@NutriPlusApplication) }
        }
        applicationScope.launch {
            authRepository.observeAuthState()
                .flatMapLatest { user -> user?.uid?.let(minutaRepository::observeMinuta) ?: emptyFlow() }
                .collectLatest { /* efecto lateral: escribe en Room */ }
        }
    }

    private fun <T> emptyFlow() = kotlinx.coroutines.flow.emptyFlow<T>()
}
