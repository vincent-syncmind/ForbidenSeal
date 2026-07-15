package com.vincent.forbiddenseal.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.vincent.forbiddenseal.game.DemoLevels
import com.vincent.forbiddenseal.game.GridPoint
import com.vincent.forbiddenseal.game.SealAttempt
import com.vincent.forbiddenseal.game.SealEngine

class GameViewModel : ViewModel() {
    private val levels = DemoLevels.all
    private var levelIndex by mutableStateOf(0)

    var level by mutableStateOf(levels[levelIndex])
        private set

    private var engine = SealEngine(level)

    var attempt by mutableStateOf(SealAttempt())
        private set

    fun onNodeTapped(point: GridPoint) {
        val nextAttempt = engine.tap(attempt, point)

        if (nextAttempt.completed && levelIndex < levels.lastIndex) {
            loadLevel(levelIndex + 1)
            attempt = SealAttempt(message = "上一禁已破，进入${level.title}。点击入口继续推演。")
            return
        }

        attempt = nextAttempt
    }

    fun loadLevel(index: Int) {
        if (index in levels.indices) {
            levelIndex = index
            level = levels[levelIndex]
            engine = SealEngine(level)
            attempt = SealAttempt()
        }
    }

    fun reset() {
        attempt = SealAttempt()
    }
}
