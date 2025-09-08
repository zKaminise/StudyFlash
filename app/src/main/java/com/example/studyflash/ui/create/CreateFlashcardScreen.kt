package com.example.studyflash.ui.create


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreateFlashcardScreen(nav: NavHostController, vm: CreateFlashcardViewModel = hiltViewModel()) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("FRONT_BACK") }


    Scaffold(topBar = { TopAppBar(title = { Text("Criar Flashcard") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo") })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = front, onValueChange = { front = it }, label = { Text("Frente") })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = back, onValueChange = { back = it }, label = { Text("Verso") })
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                vm.save(type, front.ifBlank { null }, back.ifBlank { null })
                nav.popBackStack()
            }) { Text("Salvar") }
        }
    }
}