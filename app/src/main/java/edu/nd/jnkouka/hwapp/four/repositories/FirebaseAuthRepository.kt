package edu.nd.jnkouka.hwapp.four.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementation of [AuthRepository] using Firebase Authentication.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUserFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override fun isUserSignedIn(): Boolean = auth.currentUser != null

    override suspend fun signInWithEmail(email: String, password: String) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            Log.v("Authentication", "Sign in successful: ${auth.currentUser?.email}")
        } catch (e: Exception) {
            Log.v("Authentication", "Sign in unsuccessful: ${e.message}")
            throw e
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String) {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Log.v("Authentication", "Sign up successful ${auth.currentUser}")
        } catch (e: Exception) {
            Log.v("Authentication", "Error signing up: ${e.message}")
            throw e
        }
    }

    override fun signOut() {
        auth.signOut()
    }
}