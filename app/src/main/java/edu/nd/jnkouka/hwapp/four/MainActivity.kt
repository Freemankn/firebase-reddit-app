package edu.nd.jnkouka.hwapp.four

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import edu.nd.jnkouka.hwapp.four.repositories.FirebaseAuthRepository
import edu.nd.jnkouka.hwapp.four.repositories.FirebaseRedditRepository
import edu.nd.jnkouka.hwapp.four.screens.ListScreen
import edu.nd.jnkouka.hwapp.four.screens.ListViewModel
import edu.nd.jnkouka.hwapp.four.screens.LogInScreen
import edu.nd.jnkouka.hwapp.four.screens.LogInViewModel
import edu.nd.jnkouka.hwapp.four.screens.PostScreen
import edu.nd.jnkouka.hwapp.four.screens.PostViewModel
import edu.nd.jnkouka.hwapp.four.screens.Routes
import edu.nd.jnkouka.hwapp.four.ui.theme.HWStarterRepoTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Connect to firebase services
        val auth = Firebase.auth // Authentication for log-in/log-out
        val firestore = Firebase.firestore // Firestore for access to cloud database


        // Create repository objects
        val authRepository = FirebaseAuthRepository(auth)
        val redditRepository = FirebaseRedditRepository(firestore, auth)


        // Create view models
        @Suppress("UNCHECKED_CAST")
        val logInViewModel: LogInViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LogInViewModel(authRepository) as T
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        val listViewModel: ListViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ListViewModel(redditRepository, authRepository) as T
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        val postViewModel: PostViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PostViewModel(redditRepository, authRepository) as T
                }
            }
        }


        // draw screen
        enableEdgeToEdge()
        setContent {
            HWStarterRepoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavRedditHost(
                        loginViewModel = logInViewModel,
                        postViewModel = postViewModel,
                        listViewModel = listViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun NavRedditHost(
    loginViewModel: LogInViewModel,
    postViewModel: PostViewModel,
    listViewModel: ListViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (loginViewModel.isLoggedIn){
            Routes.LIST
        } else {
            Routes.LOGIN
        },
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LogInScreen(
                loginViewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.LIST)
                }
            )
        }

        composable(Routes.LIST) {
            ListScreen(
                listViewModel = listViewModel,
                onPostClick = { postId: String ->
                    navController.navigate(Routes.post(postId))
                },
                onSignOut = {
                    loginViewModel.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LIST) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.POST) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            postViewModel.loadPost(postId)

            PostScreen(
                postViewModel = postViewModel,
                postId = postId,
                onBack = { navController.popBackStack() },
                onSignOut = {
                    loginViewModel.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LIST) { inclusive = true }
                    }
                }
            )
        }
    }
}