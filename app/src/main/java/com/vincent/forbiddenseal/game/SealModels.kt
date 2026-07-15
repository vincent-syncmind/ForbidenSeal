package com.vincent.forbiddenseal.game

data class GridPoint(val row: Int, val column: Int)

enum class ElementType(val label: String) {
    WOOD("木"), FIRE("火"), EARTH("土"), METAL("金"), WATER("水");

    fun generates(next: ElementType): Boolean = when (this) {
        WOOD -> next == FIRE
        FIRE -> next == EARTH
        EARTH -> next == METAL
        METAL -> next == WATER
        WATER -> next == WOOD
    }
}

enum class NodeType { EMPTY, ENTRANCE, CORE, DRAIN, MIRROR, RESONANCE, ELEMENT }

data class SealNode(
    val point: GridPoint,
    val type: NodeType = NodeType.EMPTY,
    val pairId: Int? = null,
    val element: ElementType? = null,
)

data class SealLevel(
    val id: Int,
    val title: String,
    val teachingText: String,
    val size: Int = 4,
    val nodes: List<SealNode>,
)

data class SealAttempt(
    val selectedPath: List<GridPoint> = emptyList(),
    val echoPath: List<GridPoint> = emptyList(),
    val completed: Boolean = false,
    val isFailed: Boolean = false,
    val message: String = "点击入口，或按住入口直接画出灵路。",
)
