package com.example.studyflash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.studyflash.navigation.AppNavGraph
import com.example.studyflash.ui.theme.StudyflashTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyflashTheme {
                Surface {
                    AppNavGraph()
                }
            }
        }
    }
}
