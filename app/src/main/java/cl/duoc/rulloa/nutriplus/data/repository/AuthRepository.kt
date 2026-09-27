package cl.duoc.rulloa.nutriplus.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Traduce las excepciones de Firebase Auth a mensajes en español simples, pensados para
 * personas con poca experiencia usando apps (Semana 7, objetivo 2).
 */
fun Throwable.toAuthMessage(): String = when (this) {
    is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres."
    is FirebaseAuthInvalidCredentialsException -> "El correo o la contraseña no son correctos."
    is FirebaseAuthInvalidUserException -> "No encontramos una cuenta con ese correo."
    is FirebaseAuthUserCollisionException -> "Ya existe una cuenta registrada con ese correo."
    is FirebaseAuthRecentLoginRequiredException ->
        "Por seguridad, vuelve a iniciar sesión antes de eliminar tu cuenta."
    is FirebaseNetworkException -> "Sin conexión a internet. Revisa tu red e intenta de nuevo."
    else -> message ?: "Ocurrió un error inesperado. Intenta de nuevo."
}

class AuthRepository(
    private val auth: FirebaseAuth = Firebase.auth
) {
    val currentUser: FirebaseUser? get() = auth.currentUser
    val currentUid: String? get() = auth.currentUser?.uid

    /** Emite el usuario actual cada vez que cambia la sesión (login, logout, registro). */
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await().user
            ?: error("No se pudo iniciar sesión.")
    }

    suspend fun register(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("No se pudo crear la cuenta.")
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    fun logout() = auth.signOut()

    suspend fun deleteAccount(): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("No hay una sesión activa.")
        user.delete().await()
    }
}
