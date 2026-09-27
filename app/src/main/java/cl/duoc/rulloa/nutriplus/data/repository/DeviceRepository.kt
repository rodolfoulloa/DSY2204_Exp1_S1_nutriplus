package cl.duoc.rulloa.nutriplus.data.repository

import cl.duoc.rulloa.nutriplus.data.Device
import cl.duoc.rulloa.nutriplus.data.Resource
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** CRUD de los dispositivos guardados por el usuario en /users/{uid}/devices. */
class DeviceRepository(
    private val database: FirebaseDatabase = Firebase.database
) {
    private fun devicesRef(uid: String) = database.getReference("users").child(uid).child("devices")

    fun observeDevices(uid: String): Flow<Resource<List<Device>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val devices = snapshot.children.mapNotNull { it.getValue(Device::class.java) }
                    .sortedBy { it.createdAt }
                trySend(Resource.Success(devices))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.toMessage()))
            }
        }
        devicesRef(uid).addValueEventListener(listener)
        awaitClose { devicesRef(uid).removeEventListener(listener) }
    }

    suspend fun addDevice(uid: String, name: String, type: String): Result<Unit> = runCatching {
        val ref = devicesRef(uid).push()
        val id = ref.key ?: error("No se pudo generar el dispositivo.")
        ref.setValue(
            mapOf(
                "id" to id,
                "name" to name,
                "type" to type,
                "createdAt" to ServerValue.TIMESTAMP
            )
        ).await()
    }

    suspend fun renameDevice(uid: String, deviceId: String, newName: String, newType: String): Result<Unit> =
        runCatching {
            devicesRef(uid).child(deviceId).updateChildren(
                mapOf("name" to newName, "type" to newType)
            ).await()
        }

    suspend fun deleteDevice(uid: String, deviceId: String): Result<Unit> = runCatching {
        devicesRef(uid).child(deviceId).removeValue().await()
    }
}
