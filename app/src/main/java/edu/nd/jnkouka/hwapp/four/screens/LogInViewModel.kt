package edu.nd.jnkouka.hwapp.four.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.nd.jnkouka.hwapp.four.repositories.FirebaseAuthRepository
import kotlinx.coroutines.launch

class LogInViewModel(
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isLoggedIn by mutableStateOf(authRepository.isUserSignedIn())
    var currentUser by mutableStateOf(authRepository.currentUser)


    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    suspend fun signIn(): Boolean {
        isLoading = true
        errorMessage = null
        return try{
            authRepository.signInWithEmail(email, password)
            isLoggedIn = authRepository.isUserSignedIn()
            if (!isLoggedIn) {
                errorMessage = "Sign in failed. Please check your credentials."
            }
            currentUser = authRepository.currentUser
            isLoggedIn
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "An error occurred"
            false
        } finally {
            isLoading = false
        }
    }

    suspend fun signUp(): Boolean {
        isLoading = true
        errorMessage = null
        return try {
            authRepository.signUpWithEmail(email, password)
            isLoggedIn = authRepository.isUserSignedIn()
            if (!isLoggedIn) {
                errorMessage = "Sign up failed."
            }
            currentUser = authRepository.currentUser
            isLoggedIn
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "An error occurred"
            false
        } finally {
            isLoading = false
        }
    }

    fun signOut() {
        authRepository.signOut()
        isLoggedIn = false
        currentUser = null
        email = ""
        password = ""
    }
}