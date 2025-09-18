package com.example.studyflash.ui.study

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studyflash.domain.spaced.StudyGrade
import java.text.Normalizer

@Composable
fun StudyScreen(
    padding: PaddingValues,
    vm: StudyViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    var flipped by remember { mutableStateOf(false) }

    // ✅ agora é startOrContinue() (inicia a sessão e calcula X de Y)
    LaunchedEffect(Unit) { vm.startOrContinue() }

    val card by vm.current.collectAsStateWithLifecycle()
    val options by vm.options.collectAsStateWithLifecycle()
    val selectedIndex by vm.selectedIndex.collectAsStateWithLifecycle()
    val isCorrect by vm.isCorrect.collectAsStateWithLifecycle()

    // reset quando muda o cartão
    LaunchedEffect(card?.id) { flipped = false }

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho de progresso "X de Y" + barra
        StudyProgress(vm = vm)

        if (card == null) {
            Text("Nenhum cartão devido agora. Volte mais tarde ou crie mais cartões.")
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Voltar para Home")
            }
            return@Column
        }

        when (card!!.type) {
            "mcq" -> McqSection(
                question = card!!.frontText.orEmpty(),
                correct = card!!.backText.orEmpty(),
                options = options,
                selectedIndex = selectedIndex,
                isCorrect = isCorrect
            ) {
                vm.choose(it)
                flipped = true
            }

            "front_back" -> FrontBackSection(
                front = card!!.frontText.orEmpty(),
                back  = card!!.backText.orEmpty(),
                onGrade = { grade -> vm.commitGrade(grade) {} }
            )

            "free_text" -> FreeTextSection(
                question = card!!.frontText.orEmpty(),
                answers = splitAnswers(card!!.backText),
                onGrade = { grade -> vm.commitGrade(grade) {} }
            )

            "cloze" -> ClozeSection(
                text = card!!.frontText.orEmpty(),
                answers = splitAnswers(card!!.backText),
                onGrade = { grade -> vm.commitGrade(grade) {} }
            )

            else -> Text("Tipo desconhecido: ${card!!.type}")
        }

        // Para MCQ, após escolher, mostramos "Próximo"
        if (card!!.type == "mcq" && selectedIndex != null) {
            Button(
                onClick = { vm.commitAnswer { flipped = false } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Próximo") }
        }
    }
}

/* ==== Seções por tipo ==== */

@Composable
private fun McqSection(
    question: String,
    correct: String,
    options: List<String>,
    selectedIndex: Int?,
    isCorrect: Boolean?,
    onChoose: (Int) -> Unit
) {
    // Flip visual quando responde
    var flipped by remember { mutableStateOf(false) }
    LaunchedEffect(selectedIndex) { flipped = selectedIndex != null }

    val rotation by animateFloatAsState(if (flipped) 180f else 0f, label = "flip")
    val density = LocalDensity.current.density

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = selectedIndex == null) { },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            Text(question, textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp))
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }, contentAlignment = Alignment.Center) {
                Text(correct, textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp))
            }
        }
    }

    Text("Escolha a resposta correta:")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, opt ->
            val chosen = selectedIndex == index
            val showColors = selectedIndex != null
            val correctThis = opt == correct

            val bg = when {
                showColors && correctThis -> Color(0xFF2E7D32)
                showColors && chosen && !correctThis -> Color(0xFFC62828)
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
            val fg = if (showColors && (correctThis || chosen && !correctThis)) Color.White
            else MaterialTheme.colorScheme.onSecondaryContainer

            Button(
                onClick = { if (selectedIndex == null) onChoose(index) },
                colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
                enabled = selectedIndex == null,
                modifier = Modifier.fillMaxWidth()
            ) { Text(opt) }
        }
    }
}

@Composable
private fun FrontBackSection(
    front: String,
    back: String,
    onGrade: (StudyGrade) -> Unit
) {
    var revealed by remember { mutableStateOf(false) }

    Text(
        front,
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    if (!revealed) {
        Button(onClick = { revealed = true }, modifier = Modifier.fillMaxWidth()) { Text("Mostrar resposta") }
    } else {
        Text(back, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        GradeRow(onGrade)
    }
}

@Composable
private fun FreeTextSection(
    question: String,
    answers: List<String>,
    onGrade: (StudyGrade) -> Unit
) {
    var input by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    val ok = remember(input, answers, checked) {
        checked && matchesAny(input, answers)
    }

    Text(
        question,
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        label = { Text("Digite sua resposta") },
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = { checked = true }, modifier = Modifier.fillMaxWidth()) { Text("Checar") }

    if (checked) {
        Text(
            if (ok) "✔ Resposta aceita" else "✖ Não bateu com nenhuma resposta esperada",
            color = if (ok) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
        Spacer(Modifier.height(8.dp))
        GradeRow(onGrade)
        Text("Respostas válidas: ${answers.joinToString(", ")}")
    }
}

@Composable
private fun ClozeSection(
    text: String,
    answers: List<String>,
    onGrade: (StudyGrade) -> Unit
) {
    var input by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    val ok = remember(input, answers, checked) {
        if (!checked) false
        else compareSets(splitAnswers(input), answers)
    }

    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        label = { Text("Preencha as lacunas (separe com ;)") },
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = { checked = true }, modifier = Modifier.fillMaxWidth()) { Text("Checar") }

    if (checked) {
        Text(
            if (ok) "✔ Todas as lacunas corretas" else "✖ Respostas não conferem",
            color = if (ok) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
        Spacer(Modifier.height(8.dp))
        GradeRow(onGrade)
        Text("Gabarito: ${answers.joinToString(", ")}")
    }
}

/* ==== UI de nota (autoavaliação) ==== */

@Composable
private fun GradeRow(onGrade: (StudyGrade) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { onGrade(StudyGrade.Again) }, modifier = Modifier.weight(1f)) { Text("Errei") }
        Button(onClick = { onGrade(StudyGrade.Hard) }, modifier = Modifier.weight(1f)) { Text("Difícil") }
        Button(onClick = { onGrade(StudyGrade.Good) }, modifier = Modifier.weight(1f)) { Text("Fácil") }
    }
}

/* ==== Helpers ==== */

private fun splitAnswers(back: String?): List<String> =
    back.orEmpty().split(";").map { it.trim() }.filter { it.isNotEmpty() }

private fun normalize(s: String): String {
    val n = Normalizer.normalize(s.trim().lowercase(), Normalizer.Form.NFD)
    return n.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
}

private fun matchesAny(input: String, answers: List<String>): Boolean {
    val norm = normalize(input)
    return answers.any { normalize(it) == norm }
}

private fun compareSets(user: List<String>, expected: List<String>): Boolean {
    val u = user.map(::normalize).toSet()
    val e = expected.map(::normalize).toSet()
    return u == e
}
