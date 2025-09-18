@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.studyflash.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studyflash.ui.analytics.AnalyticsScreen
import com.example.studyflash.ui.auth.AuthScreen
import com.example.studyflash.ui.auth.AuthViewModel
import com.example.studyflash.ui.auth.RecoverPasswordScreen
import com.example.studyflash.ui.cards.CardsScreen
import com.example.studyflash.ui.create.CreateFlashcardScreen
import com.example.studyflash.ui.home.HomeScreen
import com.example.studyflash.ui.locations.EditLocationScreen
import com.example.studyflash.ui.locations.LocationsScreen
import com.example.studyflash.ui.profile.ProfileScreen
import com.example.studyflash.ui.study.StudyScreen

sealed class Dest(val route: String) {
    data object Auth         : Dest("auth")
    data object Recover      : Dest("recover")
    data object Home         : Dest("home")
    data object Create       : Dest("create")
    data object Study        : Dest("study")
    data object Cards        : Dest("cards")
    data object Profile      : Dest("profile")
    data object Locations    : Dest("locations")
    data object Analytics    : Dest("analytics")             // ⬅️ NOVO
    data object EditLocation : Dest("edit_location/{id}") {
        fun route(id: String) = "edit_location/$id"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    val authVm: AuthViewModel = hiltViewModel()
    val user by authVm.user.collectAsStateWithLifecycle()

    val back by navController.currentBackStackEntryAsState()
    val currentRoute = back?.destination?.route
    val canNavigateBack = navController.previousBackStackEntry != null
    val startDest = if (user == null) Dest.Auth.route else Dest.Home.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StudyFlash") },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    }
                },
                actions = {
                    if (currentRoute == Dest.Home.route && user != null) {
                        IconButton(onClick = { navController.navigate(Dest.Profile.route) }) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil")
                        }
                        IconButton(onClick = {
                            authVm.signOut {
                                navController.navigate(Dest.Auth.route) {
                                    popUpTo(Dest.Home.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Logout, contentDescription = "Sair")
                        }
                    }
                }
            )
        }
    ) { padding ->
        key(startDest) {
            NavHost(navController = navController, startDestination = startDest) {

                composable(Dest.Auth.route) {
                    AuthScreen(
                        padding = padding,
                        onAuthenticated = {
                            navController.navigate(Dest.Home.route) {
                                popUpTo(Dest.Auth.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onRecoverPassword = {
                            navController.navigate(Dest.Recover.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Dest.Recover.route) {
                    RecoverPasswordScreen(
                        padding = padding,
                        onBackToAuth = { navController.popBackStack() }
                    )
                }

                composable(Dest.Home.route) {
                    HomeScreen(
                        padding = padding,
                        onCreate    = { navController.navigate(Dest.Create.route) },
                        onStudy     = { navController.navigate(Dest.Study.route) },
                        onCards     = { navController.navigate(Dest.Cards.route) },
                        onLocations = { navController.navigate(Dest.Locations.route) },
                        onAnalytics = { navController.navigate(Dest.Analytics.route) } // ⬅️ usa a rota do sealed
                    )
                }

                composable(Dest.Analytics.route) {
                    AnalyticsScreen(padding = padding) // ⬅️ trocado de innerPadding para padding
                }

                composable(Dest.Create.route) {
                    CreateFlashcardScreen(
                        padding = padding,
                        onSaved = { navController.popBackStack() }
                    )
                }

                composable(Dest.Study.route) {
                    StudyScreen(
                        padding = padding,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Dest.Cards.route) {
                    CardsScreen(padding = padding)
                }

                composable(Dest.Profile.route) {
                    ProfileScreen(padding = padding)
                }

                composable(Dest.Locations.route) {
                    LocationsScreen(
                        padding = padding,
                        onEdit = { id -> navController.navigate(Dest.EditLocation.route(id)) }
                    )
                }

                composable(Dest.EditLocation.route) { entry ->
                    val id = entry.arguments?.getString("id") ?: return@composable
                    EditLocationScreen(
                        padding = padding,
                        locationId = id,
                        onDone = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
