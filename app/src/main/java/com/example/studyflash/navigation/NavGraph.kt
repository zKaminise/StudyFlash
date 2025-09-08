@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.studyflash.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studyflash.ui.create.CreateFlashcardScreen
import com.example.studyflash.ui.home.HomeScreen

sealed class Dest(val route: String) {
    data object Home : Dest("home")
    data object Create : Dest("create")
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("StudyFlash") }) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Home.route
        ) {
            composable(Dest.Home.route) {
                HomeScreen(padding = padding, onCreate = { navController.navigate(Dest.Create.route) })
            }
            composable(Dest.Create.route) {
                CreateFlashcardScreen(padding = padding, onDone = { navController.popBackStack() })
            }
        }
    }
}
