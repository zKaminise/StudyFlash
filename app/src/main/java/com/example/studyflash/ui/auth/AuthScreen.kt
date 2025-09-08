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
    var email by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }

    val canGo = email.isNotBlank() && pass.length >= 6
    val user by vm.user.collectAsStateWithLifecycle()

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
            Text("Autenticação", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pass, onValueChange = { pass = it },
                label = { Text("Senha (mín. 6)")},
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { vm.signIn(email.trim(), pass, onAuthenticated) },
                    enabled = canGo, modifier = Modifier.weight(1f)
                ) { Text("Entrar") }

                OutlinedButton(
                    onClick = { vm.signUp(email.trim(), pass, onAuthenticated) },
                    enabled = canGo, modifier = Modifier.weight(1f)
                ) { Text("Cadastrar") }
            }

            vm.errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
