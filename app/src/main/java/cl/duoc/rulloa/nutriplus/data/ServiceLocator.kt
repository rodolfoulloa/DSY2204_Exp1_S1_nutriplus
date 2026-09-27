package cl.duoc.rulloa.nutriplus.data

import android.content.Context
import cl.duoc.rulloa.nutriplus.data.local.AppDatabase
import cl.duoc.rulloa.nutriplus.data.repository.AuthRepository
import cl.duoc.rulloa.nutriplus.data.repository.DeviceRepository
import cl.duoc.rulloa.nutriplus.data.repository.MinutaRepository
import cl.duoc.rulloa.nutriplus.data.repository.RecipeRepository
import cl.duoc.rulloa.nutriplus.data.repository.UserRepository

/**
 * Localizador de servicios manual (el proyecto no usa un framework de inyección de
 * dependencias). Los ViewModels obtienen los repositorios de aquí en vez de crear
 * clientes de Firebase directamente.
 */
object ServiceLocator {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }

    val authRepository: AuthRepository by lazy { AuthRepository() }
    val userRepository: UserRepository by lazy { UserRepository() }
    val recipeRepository: RecipeRepository by lazy { RecipeRepository(database.recipeDao()) }
    val minutaRepository: MinutaRepository by lazy { MinutaRepository(database.minutaDao(), appContext) }
    val deviceRepository: DeviceRepository by lazy { DeviceRepository() }
    val connectivityObserver: ConnectivityObserver by lazy { ConnectivityObserver(appContext) }
}
