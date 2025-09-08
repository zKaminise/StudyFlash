package com.example.studyflash.ui.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CreateFlashcardScreen(
    padding: PaddingValues,
    onDone: () -> Unit,
    vm: CreateFlashcardViewModel = hiltViewModel()
) {
    var type by remember { mutableStateOf("FRONT_BACK") }
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }

    val host = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.padding(padding),
        snackbarHost = { SnackbarHost(host) }
    ) { inner ->
        Column(Modifier.padding(inner).padding(16.dp)) {

            OutlinedTextField(
                value = type, onValueChange = { type = it },
                label = { Text("Tipo (FRONT_BACK, CLOZE, TYPING, MCQ)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = front, onValueChange = { front = it },
                label = { Text("Frente / Pergunta") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = back, onValueChange = { back = it },
                label = { Text("Verso / Resposta (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                if (type.isBlank() || front.isBlank()) {
                    return@Button
                }
                vm.save(type, front, back.ifBlank { null })
                onDone()
            }) {
                Text("Salvar")
            }
        }
    }
}
