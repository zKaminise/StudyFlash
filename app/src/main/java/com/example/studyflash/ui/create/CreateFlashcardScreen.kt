package com.example.studyflash.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

// ✅ mover enum para topo do arquivo (enums não podem ser locais)
enum class CardType { MCQ, FRONT_BACK, CLOZE, FREE_TEXT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFlashcardScreen(
    padding: PaddingValues,
    onSaved: () -> Unit,
    vm: CreateFlashcardViewModel = hiltViewModel()
) {
    var selected by remember { mutableStateOf(CardType.MCQ) }

    // MCQ
    var question by remember { mutableStateOf(TextFieldValue("")) }
    var correct  by remember { mutableStateOf(TextFieldValue("")) }
    var wrong1   by remember { mutableStateOf(TextFieldValue("")) }
    var wrong2   by remember { mutableStateOf(TextFieldValue("")) }
    var wrong3   by remember { mutableStateOf(TextFieldValue("")) }

    // Frente/Verso
    var front by remember { mutableStateOf(TextFieldValue("")) }
    var back  by remember { mutableStateOf(TextFieldValue("")) }

    // Free text
    var freeQuestion by remember { mutableStateOf(TextFieldValue("")) }
    var freeAnswers  by remember { mutableStateOf(TextFieldValue("")) } // ; separa respostas

    // Cloze
    var clozeText    by remember { mutableStateOf(TextFieldValue("")) }
    var clozeAnswers by remember { mutableStateOf(TextFieldValue("")) } // ; separa respostas

    val canSave = when (selected) {
        CardType.MCQ -> question.text.isNotBlank() &&
                correct.text.isNotBlank() &&
                wrong1.text.isNotBlank() &&
                wrong2.text.isNotBlank() &&
                wrong3.text.isNotBlank()
        CardType.FRONT_BACK -> front.text.isNotBlank() && back.text.isNotBlank()
        CardType.FREE_TEXT  -> freeQuestion.text.isNotBlank() && freeAnswers.text.isNotBlank()
        CardType.CLOZE      -> clozeText.text.isNotBlank() && clozeAnswers.text.isNotBlank()
    }

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Novo flashcard", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selected == CardType.MCQ,
                    onClick = { selected = CardType.MCQ },
                    label = { Text("MCQ") }
                )
                FilterChip(
                    selected = selected == CardType.FRONT_BACK,
                    onClick = { selected = CardType.FRONT_BACK },
                    label = { Text("Frente/Verso") }
                )
                FilterChip(
                    selected = selected == CardType.CLOZE,
                    onClick = { selected = CardType.CLOZE },
                    label = { Text("Cloze") }
                )
                FilterChip(
                    selected = selected == CardType.FREE_TEXT,
                    onClick = { selected = CardType.FREE_TEXT },
                    label = { Text("Digite a resposta") }
                )
            }

            when (selected) {
                CardType.MCQ -> {
                    OutlinedTextField(
                        value = question, onValueChange = { question = it },
                        label = { Text("Pergunta") }, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = correct, onValueChange = { correct = it },
                        label = { Text("Resposta correta") }, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wrong1, onValueChange = { wrong1 = it },
                        label = { Text("Alternativa errada 1") }, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wrong2, onValueChange = { wrong2 = it },
                        label = { Text("Alternativa errada 2") }, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wrong3, onValueChange = { wrong3 = it },
                        label = { Text("Alternativa errada 3") }, modifier = Modifier.fillMaxWidth()
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
                    ) { Text("Salvar (MCQ)") }
                }

                CardType.FRONT_BACK -> {
                    OutlinedTextField(
                        value = front, onValueChange = { front = it },
                        label = { Text("Frente (pergunta)") }, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = back,  onValueChange = { back = it },
                        label = { Text("Verso (resposta)") },  modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { vm.saveFrontBack(front.text.trim(), back.text.trim(), onSaved) },
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Salvar (Frente/Verso)") }
                }

                CardType.FREE_TEXT -> {
                    OutlinedTextField(
                        value = freeQuestion, onValueChange = { freeQuestion = it },
                        label = { Text("Pergunta") }, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = freeAnswers, onValueChange = { freeAnswers = it },
                        label = { Text("Respostas válidas (separe com ;)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val answers = freeAnswers.text.split(";").map { it.trim() }.filter { it.isNotEmpty() }
                            vm.saveFreeText(freeQuestion.text.trim(), answers, onSaved)
                        },
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Salvar (Digite a resposta)") }
                }

                CardType.CLOZE -> {
                    OutlinedTextField(
                        value = clozeText, onValueChange = { clozeText = it },
                        label = { Text("Texto com lacunas (ex: 'A capital do Brasil é _____.')") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = clozeAnswers, onValueChange = { clozeAnswers = it },
                        label = { Text("Respostas das lacunas (separe com ;)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val answers = clozeAnswers.text.split(";").map { it.trim() }.filter { it.isNotEmpty() }
                            vm.saveCloze(clozeText.text.trim(), answers, onSaved)
                        },
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Salvar (Cloze)") }
                }
            }
        }
    }
}
