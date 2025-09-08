package com.example.studyflash.navigation


import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studyflash.ui.analytics.AnalyticsScreen
import com.example.studyflash.ui.chat.ChatScreen
import com.example.studyflash.ui.create.CreateFlashcardScreen
import com.example.studyflash.ui.home.HomeScreen
import com.example.studyflash.ui.locations.LocationsScreen
import com.example.studyflash.ui.study.StudyScreen


sealed class Dest(val route: String) {
    data object Home : Dest("home")
    data object Create : Dest("create")
    data object Study : Dest("study")
    data object Locations : Dest("locations")
    data object Analytics : Dest("analytics")
    data object Chat : Dest("chat")
}


@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = Dest.Home.route) {
        composable(Dest.Home.route) { HomeScreen(navController) }
        composable(Dest.Create.route) { CreateFlashcardScreen(navController) }
        composable(Dest.Study.route) { StudyScreen(navController) }
        composable(Dest.Locations.route) { LocationsScreen(navController) }
        composable(Dest.Analytics.route) { AnalyticsScreen(navController) }
        composable(Dest.Chat.route) { ChatScreen(navController) }
    }
}