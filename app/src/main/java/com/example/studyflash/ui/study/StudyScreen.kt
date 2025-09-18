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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import kotlin.math.max
import kotlin.math.min

@Composable
fun StudyScreen(
    padding: PaddingValues,
    vm: StudyViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    var flipped by remember { mutableStateOf(false) }

    // Inicia/continua sessão (x de y)
    LaunchedEffect(Unit) { vm.startOrContinue() }

    val card by vm.current.collectAsStateWithLifecycle()
    val options by vm.options.collectAsStateWithLifecycle()
    val selectedIndex by vm.selectedIndex.collectAsStateWithLifecycle()
    val isCorrect by vm.isCorrect.collectAsStateWithLifecycle()

    // progresso
    val total by vm.sessionTotal.collectAsStateWithLifecycle()
    val done by vm.answeredCount.collectAsStateWithLifecycle()
    val shownIndex = if (card == null) done else min(done + 1, max(total, 1))
    val progress = animateFloatAsState(
        targetValue = if (total <= 0) 0f else (done.toFloat() / total.toFloat()),
        label = "progress"
    )

    // reset quando muda o cartão
    LaunchedEffect(card?.id) { flipped = false }

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho X de Y + barra
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (total > 0) "$shownIndex/$total" else "Estudo",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.width(12.dp))
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (card == null) {
            Text("Nenhum cartão devido agora. Volte mais tarde ou crie mais cartões.")
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Voltar para Home")
            }
            return@Column
        }

        val c = card!!

        // --- BLOCO DA PERGUNTA/RESPOSTA (com flip no MCQ) ---
        if (c.type == "mcq") {
            McqSection(
                question = c.frontText.orEmpty(),
                correct = c.backText.orEmpty(),
                options = options,
                selectedIndex = selectedIndex,
                isCorrect = isCorrect,
                onChoose = { idx ->
                    vm.choose(idx)
                    flipped = true
                }
            )
        } else if (c.type == "front_back") {
            FrontBackSection(
                front = c.frontText.orEmpty(),
                back  = c.backText.orEmpty(),
                onGrade = { grade -> vm.commitGrade(grade) {} }
            )
        } else if (c.type == "free_text") {
            FreeTextSection(
                question = c.frontText.orEmpty(),
                answers = splitAnswers(c.backText),
                onGrade = { grade -> vm.commitGrade(grade) {} }
            )
        } else if (c.type == "cloze") {
            ClozeSection(
                text = c.frontText.orEmpty(),
                answers = splitAnswers(c.backText),
                onGrade = { grade -> vm.commitGrade(grade) {} }
            )
        } else {
            Text("Tipo desconhecido: ${c.type}")
        }

        // Para MCQ, após escolher, mostramos "Próximo"
        if (c.type == "mcq" && selectedIndex != null) {
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

    // Pergunta / resposta (cartão com flip)
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
            Box(
                modifier = Modifier.graphicsLayer { rotationY = 180f },
                contentAlignment = Alignment.Center
            ) {
                Text(correct, textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp))
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    Text("Escolha a resposta correta:", style = MaterialTheme.typography.labelLarge)

    // Alternativas
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
            ) { Text(opt, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
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
        Button(onClick = { revealed = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Mostrar resposta")
        }
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
        if (!checked) false else compareSets(splitAnswers(input), answers)
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
