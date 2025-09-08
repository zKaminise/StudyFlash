@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.studyflash.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studyflash.ui.cards.CardsScreen
import com.example.studyflash.ui.create.CreateFlashcardScreen
import com.example.studyflash.ui.home.HomeScreen
import com.example.studyflash.ui.study.StudyScreen

sealed class Dest(val route: String) {
    data object Home  : Dest("home")
    data object Create: Dest("create")
    data object Study : Dest("study")
    data object Cards : Dest("cards")
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    val canNavigateBack = navController.previousBackStackEntry != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StudyFlash") },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Home.route
        ) {
            composable(Dest.Home.route) {
                HomeScreen(
                    padding = padding,
                    onCreate = { navController.navigate(Dest.Create.route) },
                    onStudy  = { navController.navigate(Dest.Study.route) },
                    onCards  = { navController.navigate(Dest.Cards.route) }
                )
            }
            composable(Dest.Create.route) {
                CreateFlashcardScreen(padding = padding, onDone = { navController.popBackStack() })
            }
            composable(Dest.Study.route) {
                StudyScreen(padding = padding, onBack = { navController.popBackStack() })
            }
            composable(Dest.Cards.route) {
                CardsScreen(padding = padding)
            }
        }
    }
}
