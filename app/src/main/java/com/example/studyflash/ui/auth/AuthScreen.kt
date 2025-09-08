package com.example.studyflash.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AuthScreen(
    padding: PaddingValues,
    onAuthenticated: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val user by vm.user.collectAsStateWithLifecycle()
    var mode by remember { mutableStateOf(AuthMode.Login) }

    var email by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        if (user != null) onAuthenticated()
    }

    Scaffold { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (mode == AuthMode.Login) "Entrar" else "Cadastrar",
                style = MaterialTheme.typography.titleLarge
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mode == AuthMode.Login,
                    onClick = { mode = AuthMode.Login },
                    label = { Text("Login") }
                )
                FilterChip(
                    selected = mode == AuthMode.SignUp,
                    onClick = { mode = AuthMode.SignUp },
                    label = { Text("Cadastro") }
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (mode == AuthMode.SignUp) {
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirmar senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            vm.errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))

            if (mode == AuthMode.Login) {
                Button(
                    onClick = { vm.signIn(email.trim(), pass) { onAuthenticated() } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Entrar") }

                TextButton(
                    onClick = { mode = AuthMode.SignUp },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Quero me cadastrar") }

            } else {
                Button(
                    onClick = {
                        when {
                            pass != confirm -> vm.setError("As senhas não coincidem")
                            pass.length < 6 -> vm.setError("Senha deve ter pelo menos 6 caracteres")
                            else -> vm.signUp(email.trim(), pass) { onAuthenticated() }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cadastrar") }

                TextButton(
                    onClick = { mode = AuthMode.Login },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Já tenho conta") }
            }
        }
    }
}

private enum class AuthMode { Login, SignUp }
