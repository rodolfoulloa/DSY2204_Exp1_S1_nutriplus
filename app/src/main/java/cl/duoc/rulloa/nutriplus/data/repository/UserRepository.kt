package cl.duoc.rulloa.nutriplus.data.repository

import cl.duoc.rulloa.nutriplus.data.Resource
import cl.duoc.rulloa.nutriplus.data.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** CRUD del perfil de usuario en /users/{uid}/profile. */
class UserRepository(
    private val database: com.google.firebase.database.FirebaseDatabase = Firebase.database
) {
    private fun profileRef(uid: String) = database.getReference("users").child(uid).child("profile")

    suspend fun createProfile(uid: String, profile: UserProfile): Result<Unit> = runCatching {
        profileRef(uid).setValue(profile).await()
    }

    fun observeProfile(uid: String): Flow<Resource<UserProfile?>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(Resource.Success(snapshot.getValue(UserProfile::class.java)))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.toMessage()))
            }
        }
        profileRef(uid).addValueEventListener(listener)
        awaitClose { profileRef(uid).removeEventListener(listener) }
    }

    suspend fun updateName(uid: String, newName: String): Result<Unit> = runCatching {
        profileRef(uid).child("name").setValue(newName).await()
    }

    /** Borra todos los datos del usuario (perfil, minuta, favoritos, dispositivos). */
    suspend fun deleteUserData(uid: String): Result<Unit> = runCatching {
        database.getReference("users").child(uid).removeValue().await()
    }
}
