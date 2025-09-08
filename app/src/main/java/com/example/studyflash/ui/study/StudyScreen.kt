package com.example.studyflash.ui.study

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun StudyScreen(
    padding: PaddingValues,
    vm: StudyViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    var flipped by remember { mutableStateOf(false) }   // controla frente/verso do cartão

    LaunchedEffect(Unit) { vm.loadNext() }

    val card by vm.current.collectAsState()
    val options by vm.options.collectAsState()
    val selectedIndex by vm.selectedIndex.collectAsState()
    val isCorrect by vm.isCorrect.collectAsState()

    // reset quando muda o cartão
    LaunchedEffect(card?.id) { flipped = false }

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (card == null) {
            Text("Nenhum cartão devido agora. Volte mais tarde ou crie mais cartões.")
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Voltar para Home")
            }
            return@Column
        }

        // ===== Cartão com flip =====
        val rotation by animateFloatAsState(if (flipped) 180f else 0f, label = "flip")
        val density = androidx.compose.ui.platform.LocalDensity.current.density

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable(enabled = selectedIndex == null) { /* desabilita clique direto; usamos seleção */ },
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Frente (pergunta)
                Text(
                    text = card!!.frontText ?: "(sem texto)",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            } else {
                // Verso (resposta) – desespelhar
                Box(
                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = card!!.backText ?: "(sem resposta)",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // ===== Opções (MCQ) =====
        Text("Escolha a resposta correta:")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEachIndexed { index, opt ->
                val chosen = selectedIndex == index
                val showColors = selectedIndex != null
                val correctAnswer = (card!!.backText ?: "").ifBlank { "Sem resposta" }
                val correctThis = opt == correctAnswer

                val bg = when {
                    showColors && correctThis -> Color(0xFF2E7D32) // verde
                    showColors && chosen && !correctThis -> Color(0xFFC62828) // vermelho
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
                val fg = if (showColors && (correctThis || chosen && !correctThis)) Color.White
                else MaterialTheme.colorScheme.onSecondaryContainer

                Button(
                    onClick = {
                        if (selectedIndex == null) {
                            vm.choose(index)
                            // após escolher, vira o cartão para mostrar a resposta
                            flipped = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = bg,
                        contentColor = fg
                    ),
                    enabled = selectedIndex == null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(opt)
                }
            }
        }

        // ===== Próximo =====
        if (selectedIndex != null) {
            Button(
                onClick = {
                    vm.commitAnswer { flipped = false }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Próximo")
            }
        }
    }
}
