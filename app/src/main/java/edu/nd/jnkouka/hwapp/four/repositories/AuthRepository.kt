package edu.nd.jnkouka.hwapp.four.repositories

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining authentication operations.
 */
interface AuthRepository {
    val currentUserFlow: Flow<FirebaseUser?>
    val currentUser: FirebaseUser?

    fun isUserSignedIn(): Boolean
    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signUpWithEmail(email: String, password: String)
    fun signOut()
}