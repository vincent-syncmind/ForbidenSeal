package com.vincent.forbiddenseal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vincent.forbiddenseal.ui.GameScreen
import com.vincent.forbiddenseal.ui.GameViewModel
import com.vincent.forbiddenseal.ui.LevelSelectionScreen
import com.vincent.forbiddenseal.ui.theme.ForbiddenSealTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ForbiddenSealTheme {
                var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
                val viewModel: GameViewModel = viewModel()

                when (currentScreen) {
                    is AppScreen.Home -> {
                        LevelSelectionScreen(
                            onLevelSelected = { index ->
                                viewModel.loadLevel(index)
                                currentScreen = AppScreen.Game
                            }
                        )
                    }
                    is AppScreen.Game -> {
                        GameScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = AppScreen.Home }
                        )
                    }
                }
            }
        }
    }
}

sealed class AppScreen {
    object Home : AppScreen()
    object Game : AppScreen()
}
