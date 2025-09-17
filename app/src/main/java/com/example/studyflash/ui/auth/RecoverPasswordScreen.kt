package com.example.studyflash.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverPasswordScreen(
    padding: PaddingValues,
    onBackToAuth: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val host = remember { SnackbarHostState() }
    val scope: CoroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }

    Scaffold(snackbarHost = { SnackbarHost(host) }) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Recuperar senha", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail cadastrado") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val e = email.trim()
                    if (e.isBlank()) {
                        scope.launch { host.showSnackbar("Informe um e-mail") }
                    } else {
                        vm.sendPasswordReset(e) { ok, msg ->
                            scope.launch {
                                if (ok) {
                                    host.showSnackbar("Enviamos um link para $e")
                                    onBackToAuth()
                                } else {
                                    host.showSnackbar(msg ?: "Falha ao enviar e-mail")
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Enviar link de recuperação") }
        }
    }
}
