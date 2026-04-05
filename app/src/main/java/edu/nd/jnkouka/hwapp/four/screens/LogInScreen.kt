package edu.nd.jnkouka.hwapp.four.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LogInScreen(
    loginViewModel: LogInViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (loginViewModel.isLoggedIn) {
            Text(text = "Logged in as ${loginViewModel.currentUser?.email} !")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { loginViewModel.signOut() }) {
                Text("Sign Out")
            }
        } else {
            TextField(
                value = loginViewModel.email,
                onValueChange = { loginViewModel.onEmailChange(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = loginViewModel.password,
                onValueChange = { loginViewModel.onPasswordChange(it) },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (loginViewModel.isLoading) {
                CircularProgressIndicator()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        scope.launch{
                            val success = loginViewModel.signIn()
                            if (success) {
                                onLoginSuccess()
                            }
                        }
                    }) {
                        Text("Sign In")
                    }
                    Button(onClick = {
                        scope.launch{
                            val success = loginViewModel.signUp()
                            if (success) {
                                onLoginSuccess()
                            }
                        }
                    }) {
                        Text("Sign Up")
                    }
                }
            }

            loginViewModel.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}