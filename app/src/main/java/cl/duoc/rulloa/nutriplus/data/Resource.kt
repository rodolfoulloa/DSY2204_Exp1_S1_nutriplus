package cl.duoc.rulloa.nutriplus.data

/**
 * Envoltorio de estado para todo lo que se lee de Firebase: permite que las pantallas
 * muestren carga, vacío y error (incluida la falta de conexión) de forma consistente.
 */
sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String) : Resource<Nothing>
}
