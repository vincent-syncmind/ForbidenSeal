package com.vincent.forbiddenseal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vincent.forbiddenseal.game.GridPoint
import com.vincent.forbiddenseal.game.NodeType
import com.vincent.forbiddenseal.game.SealLevel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onBack: () -> Unit
) {
    val attempt = viewModel.attempt

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("禁制录", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))
            Text(viewModel.level.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                viewModel.level.teachingText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )
            Spacer(Modifier.height(28.dp))

            SealBoard(
                level = viewModel.level,
                path = attempt.selectedPath,
                echoPath = attempt.echoPath,
                onTap = viewModel::onNodeTapped,
            )

            Spacer(Modifier.height(24.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = when {
                    attempt.completed -> MaterialTheme.colorScheme.primaryContainer
                    attempt.isFailed -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = attempt.message,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = viewModel::reset) {
                Text("重新推演")
            }
        }
    }
}

@Composable
private fun SealBoard(
    level: SealLevel,
    path: List<GridPoint>,
    echoPath: List<GridPoint>,
    onTap: (GridPoint) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
            )
            .padding(20.dp),
    ) {
        val cellSize = maxWidth / level.size
        val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        val activeColor = MaterialTheme.colorScheme.primary
        val echoColor = MaterialTheme.colorScheme.tertiary

        Canvas(Modifier.fillMaxSize()) {
            val stepX = size.width / level.size
            val stepY = size.height / level.size

            level.nodes.forEach { node ->
                val center = Offset(
                    x = (node.point.column + 0.5f) * stepX,
                    y = (node.point.row + 0.5f) * stepY,
                )
                drawCircle(lineColor, radius = stepX * 0.08f, center = center)
            }

            path.zipWithNext().forEach { (from, to) ->
                drawLine(
                    color = activeColor,
                    start = Offset((from.column + 0.5f) * stepX, (from.row + 0.5f) * stepY),
                    end = Offset((to.column + 0.5f) * stepX, (to.row + 0.5f) * stepY),
                    strokeWidth = stepX * 0.09f,
                    cap = StrokeCap.Round,
                )
            }

            echoPath.zipWithNext().forEach { (from, to) ->
                drawLine(
                    color = echoColor,
                    start = Offset((from.column + 0.5f) * stepX, (from.row + 0.5f) * stepY),
                    end = Offset((to.column + 0.5f) * stepX, (to.row + 0.5f) * stepY),
                    strokeWidth = stepX * 0.06f,
                    cap = StrokeCap.Round,
                )
            }
        }

        level.nodes.forEach { node ->
            val selected = node.point in path
            Box(
                modifier = Modifier
                    .offset(
                        x = cellSize * node.point.column,
                        y = cellSize * node.point.row,
                    )
                    .size(cellSize)
                    .clickable { onTap(node.point) },
                contentAlignment = Alignment.Center,
            ) {
                when (node.type) {
                    NodeType.ENTRANCE -> RuneNode("入", selected, filled = true)
                    NodeType.CORE -> RuneNode("眼", selected, filled = false)
                    NodeType.DRAIN -> RuneNode("泄", selected, filled = false)
                    NodeType.MIRROR -> RuneNode("镜", selected, filled = false)
                    NodeType.RESONANCE -> RuneNode("星", selected, filled = false)
                    NodeType.ELEMENT -> RuneNode(node.element?.label ?: "?", selected, filled = false)
                    NodeType.EMPTY -> {
                        if (selected) {
                            Box(
                                Modifier
                                    .size(18.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RuneNode(label: String, selected: Boolean, filled: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(46.dp)
            .then(
                if (filled || selected) {
                    Modifier.background(primary, CircleShape)
                } else {
                    Modifier.background(Color.Transparent, CircleShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            if (!filled && !selected) {
                drawCircle(primary, style = Stroke(width = 3.dp.toPx()))
            }
        }
        Text(
            label,
            color = if (filled || selected) MaterialTheme.colorScheme.onPrimary else primary,
            fontWeight = FontWeight.Bold,
        )
    }
}
