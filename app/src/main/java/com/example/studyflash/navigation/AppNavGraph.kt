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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studyflash.ui.auth.AuthScreen
import com.example.studyflash.ui.auth.AuthViewModel
import com.example.studyflash.ui.cards.CardsScreen
import com.example.studyflash.ui.create.CreateFlashcardScreen
import com.example.studyflash.ui.home.HomeScreen
import com.example.studyflash.ui.study.StudyScreen

sealed class Dest(val route: String) {
    data object Auth  : Dest("auth")
    data object Home  : Dest("home")
    data object Create: Dest("create")
    data object Study : Dest("study")
    data object Cards : Dest("cards")
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    val backEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backEntry?.destination?.route
    val showTopBar = currentRoute != Dest.Auth.route
    val canNavigateBack = navController.previousBackStackEntry != null

    val authVm: AuthViewModel = hiltViewModel()
    val user by authVm.user.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text("StudyFlash") },
                    navigationIcon = {
                        if (canNavigateBack) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar"
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (user == null) Dest.Auth.route else Dest.Home.route
        ) {
            composable(Dest.Auth.route) {
                AuthScreen(
                    padding = padding,
                    onAuthenticated = {
                        navController.navigate(Dest.Home.route) {
                            popUpTo(Dest.Auth.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Dest.Home.route) {
                HomeScreen(
                    padding = padding,
                    onCreate = { navController.navigate(Dest.Create.route) },
                    onStudy  = { navController.navigate(Dest.Study.route) },
                    onCards  = { navController.navigate(Dest.Cards.route) }
                )
            }
            composable(Dest.Create.route) {
                // 👇 trocado onDone -> onSaved
                CreateFlashcardScreen(
                    padding = padding,
                    onSaved = { navController.popBackStack() }
                )
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
