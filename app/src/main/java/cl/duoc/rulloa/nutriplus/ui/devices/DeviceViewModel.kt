package cl.duoc.rulloa.nutriplus.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rulloa.nutriplus.data.Device
import cl.duoc.rulloa.nutriplus.data.Resource
import cl.duoc.rulloa.nutriplus.data.repository.AuthRepository
import cl.duoc.rulloa.nutriplus.data.repository.DeviceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** CRUD de "Mis dispositivos" (BuscarDispositivo): guardar, listar, renombrar y eliminar. */
class DeviceViewModel(
    private val deviceRepository: DeviceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val uid: String? get() = authRepository.currentUid

    val devicesState: StateFlow<Resource<List<Device>>> =
        (uid?.let { deviceRepository.observeDevices(it) } ?: flowOf(Resource.Success(emptyList())))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    fun addDevice(name: String, type: String) {
        val currentUid = uid ?: return
        viewModelScope.launch { deviceRepository.addDevice(currentUid, name, type) }
    }

    fun renameDevice(deviceId: String, newName: String, newType: String) {
        val currentUid = uid ?: return
        viewModelScope.launch { deviceRepository.renameDevice(currentUid, deviceId, newName, newType) }
    }

    fun deleteDevice(deviceId: String) {
        val currentUid = uid ?: return
        viewModelScope.launch { deviceRepository.deleteDevice(currentUid, deviceId) }
    }
}
