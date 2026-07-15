package com.vincent.forbiddenseal.game

import kotlin.math.abs

class SealEngine(private val level: SealLevel) {
    private val nodeByPoint = level.nodes.associateBy { it.point }
    private val entrance = level.nodes.first { it.type == NodeType.ENTRANCE }.point
    private val core = level.nodes.first { it.type == NodeType.CORE }.point
    private val mirrorPoints = level.nodes.filter { it.type == NodeType.MIRROR }.map { it.point }

    fun tap(current: SealAttempt, point: GridPoint): SealAttempt {
        if (current.completed || current.isFailed) return current
        if (current.selectedPath.isEmpty()) {
            return if (point == entrance) current.copy(selectedPath = listOf(point), message = "灵力已入阵。")
            else current.copy(message = "需从入口开始试探。")
        }

        val last = current.selectedPath.last()
        if (point == last) return current
        if (point in current.selectedPath) {
            val index = current.selectedPath.indexOf(point)
            return current.copy(selectedPath = current.selectedPath.take(index + 1), echoPath = emptyList(), message = "灵路已回退。")
        }
        if (abs(last.row - point.row) + abs(last.column - point.column) != 1) {
            return current.copy(message = "灵力只能流向上下左右相邻节点。")
        }

        val node = nodeByPoint.getValue(point)
        if (node.type == NodeType.DRAIN) return collapse("吞灵符文吸尽了灵气。")
        if (!elementOrderIsValid(current, node)) return collapse("五行逆乱，灵路崩散。")

        var newPath = current.selectedPath + point
        var echo = emptyList<GridPoint>()
        var message = "灵路稳定，继续推演。"

        when (node.type) {
            NodeType.MIRROR -> {
                message = "另一枚镜像符文正在复制你的灵路。"
            }
            NodeType.RESONANCE -> {
                val destination = level.nodes.firstOrNull {
                    it.type == NodeType.RESONANCE && it.pairId == node.pairId && it.point != point
                }?.point ?: return collapse("共鸣符文残缺，无法传送。")
                newPath = newPath + destination
                message = "两枚共鸣符文同时亮起，灵力已越过空间。"
            }
            else -> Unit
        }

        val mirrorCopy = computeMirrorCopyPath(newPath)
        if (mirrorCopy.errorMessage != null) {
            return collapse(mirrorCopy.errorMessage)
        }
        echo = mirrorCopy.echoPath
        val mirrorHitCore = core in echo

        val reachedCore = newPath.last() == core || mirrorHitCore
        return current.copy(
            selectedPath = newPath,
            echoPath = echo,
            completed = reachedCore,
            message = if (reachedCore) "禁制已解。你对这一卷的理解加深了。" else message,
        )
    }

    private fun elementOrderIsValid(current: SealAttempt, nextNode: SealNode): Boolean {
        val next = nextNode.element ?: return true
        val previous = current.selectedPath.asReversed().firstNotNullOfOrNull { nodeByPoint[it]?.element } ?: return true
        return previous.generates(next)
    }

    private data class MirrorCopyResult(
        val echoPath: List<GridPoint>,
        val errorMessage: String? = null,
    )

    private fun computeMirrorCopyPath(path: List<GridPoint>): MirrorCopyResult {
        if (mirrorPoints.isEmpty()) return MirrorCopyResult(emptyList())

        if (mirrorPoints.size >= 2) {
            val sourceMirror = path.firstOrNull { it in mirrorPoints } ?: return MirrorCopyResult(emptyList())
            val targetMirror = mirrorPoints.first { it != sourceMirror }
            val rowOffset = targetMirror.row - sourceMirror.row
            val columnOffset = targetMirror.column - sourceMirror.column
            val copiedPath = path.map { GridPoint(it.row + rowOffset, it.column + columnOffset) }

            if (copiedPath.any { it.row !in 0 until level.size || it.column !in 0 until level.size }) {
                return MirrorCopyResult(emptyList(), "镜灵复制超出阵盘，灵路崩散。")
            }
            if (copiedPath.any { copied -> nodeByPoint[copied]?.type == NodeType.DRAIN }) {
                return MirrorCopyResult(emptyList(), "镜灵复制触及吞灵禁，灵路崩散。")
            }

            return MirrorCopyResult(copiedPath)
        }

        val sourceMirror = mirrorPoints.first()
        if (sourceMirror !in path) return MirrorCopyResult(emptyList())

        val mirrored = GridPoint(sourceMirror.row, level.size - 1 - sourceMirror.column)
        val mirrorNode = nodeByPoint[mirrored]
        if (mirrorNode == null || mirrorNode.type == NodeType.DRAIN) {
            return MirrorCopyResult(emptyList(), "镜灵投影触及吞灵禁，灵路崩散。")
        }

        return MirrorCopyResult(listOf(sourceMirror, mirrored))
    }

    private fun collapse(reason: String) = SealAttempt(message = "$reason 请从入口重新推演。", isFailed = true)
}

object DemoLevels {
    private const val GRID_SIZE = 4

    private fun level(
        id: Int,
        title: String,
        text: String,
        entrance: GridPoint,
        core: GridPoint,
        special: Map<GridPoint, SealNode> = emptyMap(),
    ) = SealLevel(id, title, text, size = GRID_SIZE, nodes = buildList {
        repeat(GRID_SIZE) { r -> repeat(GRID_SIZE) { c ->
            val p = GridPoint(r, c)
            add(special[p] ?: SealNode(p, when (p) {
                entrance -> NodeType.ENTRANCE
                core -> NodeType.CORE
                else -> NodeType.EMPTY
            }))
        }}
    })

    private fun drain(p: GridPoint) = SealNode(p, NodeType.DRAIN)
    private fun mirror(p: GridPoint) = SealNode(p, NodeType.MIRROR)
    private fun resonance(p: GridPoint, pair: Int) = SealNode(p, NodeType.RESONANCE, pairId = pair)
    private fun element(p: GridPoint, e: ElementType) = SealNode(p, NodeType.ELEMENT, element = e)
    private fun randomDrains(count: Int, entrance: GridPoint, core: GridPoint): Map<GridPoint, SealNode> {
        return buildList {
            repeat(GRID_SIZE) { r ->
                repeat(GRID_SIZE) { c ->
                    val point = GridPoint(r, c)
                    if (point != entrance && point != core) add(point)
                }
            }
        }
            .shuffled()
            .take(count)
            .associateWith(::drain)
    }

    val all = listOf(
        level(1, "第一卷 · 引灵", "引灵者，当知灵路。", GridPoint(3,0), GridPoint(0,3)),
        level(
            2,
            "第二卷 · 吞灵",
            "灵盛则生，灵衰则灭。每次推演会随机生成 3 枚吞灵符文。",
            GridPoint(3,0),
            GridPoint(0,3),
            randomDrains(count = 3, entrance = GridPoint(3,0), core = GridPoint(0,3)),
        ),
        level(3, "第三卷 · 镜像", "触碰任一镜符后，另一枚镜符会复制当前灵路。若复制路径触及吞灵符，灵路崩散。", GridPoint(3,0), GridPoint(0,3), buildMap {
            put(GridPoint(2,1), mirror(GridPoint(2,1)))
            put(GridPoint(2,3), drain(GridPoint(2,3)))
            put(GridPoint(3,2), drain(GridPoint(3,2)))
            put(GridPoint(1,1), mirror(GridPoint(1,1)))
        }),
        level(4, "第四卷 · 共鸣", "天涯亦可比邻。踏入一枚星符，会从另一枚星符现身。", GridPoint(3,0), GridPoint(0,3), buildMap {
            put(GridPoint(3,1), resonance(GridPoint(3,1), 1))
            put(GridPoint(1,2), resonance(GridPoint(1,2), 1))
            listOf(GridPoint(2,0),GridPoint(2,1),GridPoint(2,2),GridPoint(2,3)).forEach { put(it, drain(it)) }
        }),
        level(5, "第五卷 · 五行", "五行循环，生生不息。依相生之序连接五枚灵符。", GridPoint(3,0), GridPoint(0,3), buildMap {
            put(GridPoint(3,1), element(GridPoint(3,1), ElementType.WOOD))
            put(GridPoint(3,2), element(GridPoint(3,2), ElementType.FIRE))
            put(GridPoint(2,2), element(GridPoint(2,2), ElementType.EARTH))
            put(GridPoint(1,2), element(GridPoint(1,2), ElementType.METAL))
            put(GridPoint(1,1), element(GridPoint(1,1), ElementType.WATER))
            listOf(GridPoint(2,0),GridPoint(2,1),GridPoint(1,0),GridPoint(0,1)).forEach { put(it, drain(it)) }
        }),
        level(6, "第六卷 · 古神禁", "古神之禁，不可言传。此前所悟，皆在此阵之中。", GridPoint(3,0), GridPoint(0,3), buildMap {
            put(GridPoint(3,1), element(GridPoint(3,1), ElementType.WOOD))
            put(GridPoint(2,1), element(GridPoint(2,1), ElementType.FIRE))
            put(GridPoint(2,2), mirror(GridPoint(2,2)))
            put(GridPoint(1,3), drain(GridPoint(1,3)))
            put(GridPoint(1,2), resonance(GridPoint(1,2), 9))
            put(GridPoint(0,2), resonance(GridPoint(0,2), 9))
            put(GridPoint(0,1), element(GridPoint(0,1), ElementType.EARTH))
            listOf(GridPoint(2,0), GridPoint(1,0), GridPoint(1,1), GridPoint(3,2)).forEach { put(it, drain(it)) }
        }),
    )
}
