package com.example.mylabyrinth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

// Клас для unit-тестів моделі лабіринту (запускаються на JVM, без емулятора)
class ExampleUnitTest {

    private lateinit var model: LabyrinthModel

    // Метод для створення нової моделі перед кожним тестом
    @Before
    fun setUp() {
        model = LabyrinthModel()
    }

    // Метод для пошуку напрямку, у якому від гравця немає стіни
    private fun findOpenDirection(): Pair<Int, Int> {
        val directions = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
        return directions.first { (dr, dc) ->
            model.getCell(model.playerRow + dr, model.playerCol + dc) != 1
        }
    }

    // --- Генерація лабіринту ---

    // Початкова позиція гравця — (1, 1), і це прохід
    @Test
    fun playerStartsAtTopLeftOnOpenCell() {
        assertEquals(1, model.playerRow)
        assertEquals(1, model.playerCol)
        assertEquals(0, model.getCell(1, 1))
    }

    // Усі клітинки по краях поля — стіни
    @Test
    fun mazeBorderIsAlwaysWall() {
        for (i in 0 until model.rows) {
            assertEquals(1, model.getCell(i, 0))
            assertEquals(1, model.getCell(i, model.cols - 1))
        }
        for (j in 0 until model.cols) {
            assertEquals(1, model.getCell(0, j))
            assertEquals(1, model.getCell(model.rows - 1, j))
        }
    }

    // Вихід існує, він один і стоїть у правому нижньому куті
    @Test
    fun exitExistsOnlyOnceInBottomRightCorner() {
        assertEquals(2, model.getCell(model.rows - 2, model.cols - 2))

        var exitCount = 0
        for (r in 0 until model.rows) {
            for (c in 0 until model.cols) {
                if (model.getCell(r, c) == 2) exitCount++
            }
        }
        assertEquals(1, exitCount)
    }

    // Модель підтримує інші непарні розміри
    @Test
    fun customSizeIsApplied() {
        val small = LabyrinthModel(7, 7)
        assertEquals(7, small.rows)
        assertEquals(7, small.cols)
        assertEquals(2, small.getCell(5, 5))
    }

    // Кожен згенерований лабіринт має розв'язок (перевіряємо багато разів)
    @Test
    fun everyGeneratedMazeIsSolvable() {
        repeat(50) {
            model.resetGame()
            model.findPathToExit()
            assertTrue("Maze must have a path to exit", model.hintPath.isNotEmpty())
        }
    }

    // --- Рух гравця ---

    // Гравець не може йти в стіну: позиція не змінюється, результат WALL_HIT
    @Test
    fun playerCannotMoveIntoWall() {
        // зверху і зліва від (1, 1) завжди межа поля (стіна)
        assertEquals(LabyrinthModel.MoveResult.WALL_HIT, model.movePlayer(-1, 0))
        assertEquals(LabyrinthModel.MoveResult.WALL_HIT, model.movePlayer(0, -1))

        assertEquals(1, model.playerRow)
        assertEquals(1, model.playerCol)
    }

    // Рух у вільну клітинку змінює позицію гравця
    @Test
    fun playerMovesIntoOpenCell() {
        val (dr, dc) = findOpenDirection()

        val result = model.movePlayer(dr, dc)

        assertEquals(LabyrinthModel.MoveResult.MOVED, result)
        assertEquals(1 + dr, model.playerRow)
        assertEquals(1 + dc, model.playerCol)
    }

    // Рух туди й назад повертає гравця на початкову клітинку
    @Test
    fun movingForwardAndBackReturnsToStart() {
        val (dr, dc) = findOpenDirection()

        model.movePlayer(dr, dc)
        model.movePlayer(-dr, -dc)

        assertEquals(1, model.playerRow)
        assertEquals(1, model.playerCol)
    }

    // --- Підказка ---

    // Спочатку підказки немає
    @Test
    fun hintIsEmptyAtStart() {
        assertTrue(model.hintPath.isEmpty())
    }

    // Після пошуку шляху підказка з'являється і закінчується на виході
    @Test
    fun hintPathIsBuiltAndEndsAtExit() {
        model.findPathToExit()

        assertTrue(model.hintPath.isNotEmpty())
        val last = model.hintPath.last()
        assertEquals(2, model.getCell(last.first, last.second))
    }

    // Шлях підказки не проходить через стіни й складається з сусідніх клітинок
    @Test
    fun hintPathIsContinuousAndHasNoWalls() {
        model.findPathToExit()

        var prev = Pair(model.playerRow, model.playerCol)
        for (step in model.hintPath) {
            assertNotEquals("Hint must not go through wall", 1, model.getCell(step.first, step.second))
            val distance = abs(step.first - prev.first) + abs(step.second - prev.second)
            assertEquals("Each step must be to a neighbour cell", 1, distance)
            prev = step
        }
    }

    // Підказка зникає, щойно гравець зробив крок
    @Test
    fun hintDisappearsAfterPlayerMoves() {
        model.findPathToExit()
        assertTrue(model.hintPath.isNotEmpty())

        val (dr, dc) = findOpenDirection()
        model.movePlayer(dr, dc)

        assertTrue(model.hintPath.isEmpty())
    }

    // Підказка залишається, якщо гравець вдарився об стіну (крок не зроблено)
    @Test
    fun hintStaysAfterWallHit() {
        model.findPathToExit()
        val before = model.hintPath

        model.movePlayer(-1, 0) // стіна

        assertEquals(before, model.hintPath)
    }

    // --- Перемога ---

    // Проходимо весь шлях підказки: останній крок має повернути WIN
    @Test
    fun walkingHintPathLeadsToWin() {
        model.findPathToExit()
        val path = model.hintPath // копія, бо рух очищає підказку

        var prev = Pair(model.playerRow, model.playerCol)
        for ((index, step) in path.withIndex()) {
            val result = model.movePlayer(step.first - prev.first, step.second - prev.second)
            val expected = if (index == path.lastIndex) {
                LabyrinthModel.MoveResult.WIN
            } else {
                LabyrinthModel.MoveResult.MOVED
            }
            assertEquals(expected, result)
            prev = step
        }
    }

    // --- Перезапуск ---

    // Після рестарту гравець на старті, підказка очищена
    @Test
    fun resetReturnsPlayerToStartAndClearsHint() {
        val (dr, dc) = findOpenDirection()
        model.movePlayer(dr, dc)
        model.findPathToExit()

        model.resetGame()

        assertEquals(1, model.playerRow)
        assertEquals(1, model.playerCol)
        assertTrue(model.hintPath.isEmpty())
    }

    // Після рестарту вихід знову на місці
    @Test
    fun resetKeepsExitInPlace() {
        model.resetGame()
        assertEquals(2, model.getCell(model.rows - 2, model.cols - 2))
        assertFalse(model.getCell(1, 1) == 1)
    }
}