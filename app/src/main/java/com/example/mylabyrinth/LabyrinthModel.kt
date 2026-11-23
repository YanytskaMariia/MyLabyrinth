package com.example.mylabyrinth

import java.util.LinkedList
import java.util.Queue

// Клас для моделі лабіринту: карта, позиція гравця, генерація та пошук шляху
class LabyrinthModel(
    val rows: Int = 11,
    val cols: Int = 11
) {

    enum class MoveResult {
        MOVED, WALL_HIT, WIN
    }

    // 0 — прохід, 1 — стіна, 2 — вихід
    private var grid: Array<IntArray> = Array(rows) { IntArray(cols) { 1 } }

    var playerRow: Int = 1
        private set
    var playerCol: Int = 1
        private set

    var hintPath: List<Pair<Int, Int>> = emptyList()
        private set

    init {
        generateMaze()
        AppLogger.i("LabyrinthModel initialized. Size: ${rows}x${cols}")
    }

    // Метод для отримання значення клітинки
    fun getCell(row: Int, col: Int): Int = grid[row][col]

    // Метод для руху гравця
    fun movePlayer(dRow: Int, dCol: Int): MoveResult {
        val nextRow = playerRow + dRow
        val nextCol = playerCol + dCol

        if (nextRow !in 0 until rows || nextCol !in 0 until cols) {
            return MoveResult.WALL_HIT
        }

        val targetCell = grid[nextRow][nextCol]
        if (targetCell == 1) {
            return MoveResult.WALL_HIT
        }

        playerRow = nextRow
        playerCol = nextCol
        hintPath = emptyList()

        if (targetCell == 2) {
            return MoveResult.WIN
        }

        return MoveResult.MOVED
    }

    // Метод для перезапуску гри з новим лабіринтом
    fun resetGame() {
        playerRow = 1
        playerCol = 1
        hintPath = emptyList()
        generateMaze()
        AppLogger.i("New maze generated")
    }

    // Метод для генерації лабіринту (Recursive Backtracker)
    private fun generateMaze() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                grid[r][c] = 1
            }
        }

        carvePassagesFrom(1, 1)

        // вихід у правому нижньому куті
        grid[rows - 2][cols - 2] = 2
    }

    // Метод для рекурсивного прориву проходів
    private fun carvePassagesFrom(r: Int, c: Int) {
        grid[r][c] = 0

        // напрямки з кроком 2 у випадковому порядку
        val directions = listOf(
            Pair(-2, 0),
            Pair(2, 0),
            Pair(0, -2),
            Pair(0, 2)
        ).shuffled()

        for ((dr, dc) in directions) {
            val nextR = r + dr
            val nextC = c + dc

            if (nextR in 1 until rows - 1 && nextC in 1 until cols - 1 && grid[nextR][nextC] == 1) {
                // пробиваємо стіну між клітинками
                grid[r + dr / 2][c + dc / 2] = 0
                carvePassagesFrom(nextR, nextC)
            }
        }
    }

    // Метод для пошуку шляху до виходу (BFS)
    fun findPathToExit() {
        val queue: Queue<Pair<Int, Int>> = LinkedList()
        // звідки прийшли в кожну клітинку (для відновлення шляху)
        val cameFrom = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>?>()

        val start = Pair(playerRow, playerCol)
        queue.add(start)
        cameFrom[start] = null

        var exitPos: Pair<Int, Int>? = null
        val directions = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: break

            if (grid[current.first][current.second] == 2) {
                exitPos = current
                break
            }

            for ((dr, dc) in directions) {
                val next = Pair(current.first + dr, current.second + dc)
                if (next.first in 0 until rows && next.second in 0 until cols) {
                    if (grid[next.first][next.second] != 1 && !cameFrom.containsKey(next)) {
                        queue.add(next)
                        cameFrom[next] = current
                    }
                }
            }
        }

        // відновлення шляху від виходу до гравця
        val path = mutableListOf<Pair<Int, Int>>()
        var curr = exitPos
        while (curr != null && curr != start) {
            path.add(curr)
            curr = cameFrom[curr]
        }

        hintPath = path.reversed()
        AppLogger.i("Path found! Steps to exit: ${hintPath.size}")
    }
}