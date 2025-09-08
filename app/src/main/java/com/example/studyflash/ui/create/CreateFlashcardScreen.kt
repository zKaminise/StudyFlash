package com.example.studyflash.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CreateFlashcardScreen(
    padding: PaddingValues,
    onSaved: () -> Unit,
    vm: CreateFlashcardViewModel = hiltViewModel()
) {
    var question by remember { mutableStateOf(TextFieldValue("")) }
    var correct  by remember { mutableStateOf(TextFieldValue("")) }
    var wrong1   by remember { mutableStateOf(TextFieldValue("")) }
    var wrong2   by remember { mutableStateOf(TextFieldValue("")) }
    var wrong3   by remember { mutableStateOf(TextFieldValue("")) }

    val canSave = question.text.isNotBlank() &&
            correct.text.isNotBlank()  &&
            wrong1.text.isNotBlank()   &&
            wrong2.text.isNotBlank()   &&
            wrong3.text.isNotBlank()

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Novo flashcard (Múltipla Escolha)", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("Pergunta") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = correct,
                onValueChange = { correct = it },
                label = { Text("Resposta correta") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = wrong1,
                onValueChange = { wrong1 = it },
                label = { Text("Alternativa errada 1") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = wrong2,
                onValueChange = { wrong2 = it },
                label = { Text("Alternativa errada 2") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = wrong3,
                onValueChange = { wrong3 = it },
                label = { Text("Alternativa errada 3") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    vm.saveMcq(
                        question = question.text.trim(),
                        correct  = correct.text.trim(),
                        wrong1   = wrong1.text.trim(),
                        wrong2   = wrong2.text.trim(),
                        wrong3   = wrong3.text.trim(),
                        onDone   = onSaved
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Salvar") }
        }
    }
}
