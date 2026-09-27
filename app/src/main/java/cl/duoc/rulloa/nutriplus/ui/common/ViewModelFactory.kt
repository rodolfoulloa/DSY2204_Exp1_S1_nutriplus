package cl.duoc.rulloa.nutriplus.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory genérica para crear ViewModels que reciben sus repositorios por constructor
 * (el proyecto no usa un framework de inyección de dependencias; ver ServiceLocator).
 */
class ViewModelFactory<VM : ViewModel>(private val creator: () -> VM) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}
