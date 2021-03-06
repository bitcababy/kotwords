package com.jeffpdavidson.kotwords.model

import com.jeffpdavidson.kotwords.formats.CrosswordCompilerApplet
import com.jeffpdavidson.kotwords.formats.Jpz

// TODO: Validate data structures.
data class Puzzle(
        val title: String,
        val creator: String,
        val copyright: String,
        val description: String,
        val grid: List<List<Cell>>,
        val clues: List<ClueList>,
        val hasHtmlClues: Boolean = false,
        val crosswordSolverSettings: CrosswordSolverSettings,
        val puzzleType: PuzzleType = PuzzleType.CROSSWORD
) {

    data class CrosswordSolverSettings(
            val cursorColor: String,
            val selectedCellsColor: String,
            val completionMessage: String
    )

    enum class CellType {
        REGULAR,
        BLOCK,
        CLUE,
        VOID
    }

    enum class BackgroundShape {
        NONE,
        CIRCLE
    }

    enum class BorderDirection {
        TOP,
        LEFT,
        RIGHT,
        BOTTOM
    }

    data class Cell(
            val x: Int,
            val y: Int,
            val solution: String = "",
            val backgroundColor: String = "",
            val number: String = "",
            val topRightNumber: String = "",
            val cellType: CellType = CellType.REGULAR,
            val backgroundShape: BackgroundShape = BackgroundShape.NONE,
            val borderDirections: Set<BorderDirection> = setOf()
    )

    data class Word(
            val id: Int,
            val cells: List<Cell>
    )

    data class Clue(
            val word: Word,
            val number: String,
            val text: String
    )

    data class ClueList(
            val title: String,
            val clues: List<Clue>
    )

    enum class PuzzleType {
        CROSSWORD,
        ACROSTIC
    }

    /** Returns this puzzle as a JPZ file. */
    fun asJpzFile(): Jpz {
        val jpzGrid = Jpz.RectangularPuzzle.Crossword.Grid(
                width = grid[0].size,
                height = grid.size,
                cell = grid.mapIndexed { y, row ->
                    row.mapIndexed { x, cell ->
                        val type = when (cell.cellType) {
                            CellType.BLOCK -> "block"
                            CellType.CLUE -> "clue"
                            CellType.VOID -> "void"
                            else -> null
                        }
                        val backgroundShape = if (cell.backgroundShape == BackgroundShape.CIRCLE) "circle" else null
                        // Crossword Solver only renders top and left borders, so if we have a right border, apply it
                        // as a left border on the square to the right (if we're not at the right edge), and if we have
                        // a bottom border, apply it as a top border on the square to the bottom (if we're not at the
                        // bottom edge).
                        val topBorder = cell.borderDirections.contains(BorderDirection.TOP)
                                || (y > 0 && grid[y - 1][x].borderDirections.contains(BorderDirection.BOTTOM))
                        val bottomBorder = cell.borderDirections.contains(BorderDirection.BOTTOM) && y == grid.size - 1
                        val leftBorder = cell.borderDirections.contains(BorderDirection.LEFT)
                                || (x > 0 && grid[y][x - 1].borderDirections.contains(BorderDirection.RIGHT))
                        val rightBorder = cell.borderDirections.contains(BorderDirection.RIGHT) && x == grid[y].size - 1
                        Jpz.RectangularPuzzle.Crossword.Grid.Cell(
                                x = cell.x,
                                y = cell.y,
                                solution = if (cell.solution.isEmpty()) null else cell.solution,
                                backgroundColor = if (cell.backgroundColor.isEmpty()) null else cell.backgroundColor,
                                number = if (cell.number.isEmpty()) null else cell.number,
                                type = type,
                                solveState = if (cell.cellType == CellType.CLUE) cell.solution else null,
                                topRightNumber = if (cell.topRightNumber.isEmpty()) null else cell.topRightNumber,
                                backgroundShape = backgroundShape,
                                topBar = if (topBorder) true else null,
                                bottomBar = if (bottomBorder) true else null,
                                leftBar = if (leftBorder) true else null,
                                rightBar = if (rightBorder) true else null
                        )
                    }
                }.flatten()
        )

        val words = clues.flatMap { clueList ->
            clueList.clues.map { clue ->
                Jpz.RectangularPuzzle.Crossword.Word(
                        id = clue.word.id,
                        cells = clue.word.cells.map { cell ->
                            Jpz.RectangularPuzzle.Crossword.Word.Cells(cell.x, cell.y)
                        }
                )
            }
        }

        val jpzClues = clues.map { clueList ->
            Jpz.RectangularPuzzle.Crossword.Clues(
                    title = Jpz.RectangularPuzzle.Crossword.Clues.Title(listOf(Jpz.B(listOf(clueList.title)))),
                    clues = clueList.clues.map { clue ->
                        val htmlClues = if (hasHtmlClues) clue.text else formatClue(clue.text)
                        Jpz.RectangularPuzzle.Crossword.Clues.Clue(
                                word = clue.word.id,
                                number = clue.number,
                                text = Jpz.htmlToSnippet(htmlClues)
                        )
                    })
        }

        val crossword = Jpz.RectangularPuzzle.Crossword(jpzGrid, words, jpzClues)

        return CrosswordCompilerApplet(
                appletSettings = CrosswordCompilerApplet.AppletSettings(
                        cursorColor = crosswordSolverSettings.cursorColor,
                        selectedCellsColor = crosswordSolverSettings.selectedCellsColor,
                        completion = CrosswordCompilerApplet.AppletSettings.Completion(
                                message = crosswordSolverSettings.completionMessage
                        )
                ),
                rectangularPuzzle = Jpz.RectangularPuzzle(
                        metadata = Jpz.RectangularPuzzle.Metadata(
                                title = if (title.isBlank()) null else title,
                                creator = if (creator.isBlank()) null else creator,
                                copyright = if (copyright.isBlank()) null else copyright,
                                description = if (description.isBlank()) null else description
                        ),
                        crossword = if (puzzleType == PuzzleType.CROSSWORD) crossword else null,
                        acrostic = if (puzzleType == PuzzleType.ACROSTIC) crossword else null
                )
        )
    }

    companion object {
        fun fromCrossword(
                crossword: Crossword,
                crosswordSolverSettings: CrosswordSolverSettings
        ): Puzzle {
            val gridMap = mutableMapOf<Pair<Int, Int>, Cell>()
            Crossword.forEachSquare(crossword.grid) { x, y, clueNumber, _, _, square ->
                if (square == BLACK_SQUARE) {
                    gridMap[x to y] = Cell(x + 1, y + 1, cellType = CellType.BLOCK)
                } else {
                    val solution =
                            if (square.solutionRebus.isEmpty()) {
                                "${square.solution}"
                            } else {
                                square.solutionRebus
                            }
                    val number = if (clueNumber != null) "$clueNumber" else ""
                    val backgroundShape =
                            if (square.isCircled) {
                                BackgroundShape.CIRCLE
                            } else {
                                BackgroundShape.NONE
                            }
                    gridMap[x to y] =
                            Cell(
                                    x + 1, y + 1,
                                    solution = solution,
                                    number = number,
                                    backgroundShape = backgroundShape
                            )
                }
            }
            val grid = mutableListOf<List<Cell>>()
            crossword.grid.indices.forEach { y ->
                val row = mutableListOf<Cell>()
                crossword.grid[y].indices.forEach { x ->
                    row.add(gridMap[x to y]!!)
                }
                grid.add(row)
            }

            val acrossClues = mutableListOf<Clue>()
            val downClues = mutableListOf<Clue>()
            Crossword.forEachNumberedSquare(crossword.grid) { x, y, number, isAcross, isDown ->
                if (isAcross) {
                    val word = mutableListOf<Cell>()
                    var i = x
                    while (i < crossword.grid[y].size && crossword.grid[y][i] != BLACK_SQUARE) {
                        word.add(grid[y][i])
                        i++
                    }
                    acrossClues.add(
                            Clue(
                                    Word(number, word), "$number",
                                    crossword.acrossClues[number] ?: error("No across clue for number $number")
                            )
                    )
                }
                if (isDown) {
                    val word = mutableListOf<Cell>()
                    var j = y
                    while (j < crossword.grid.size && crossword.grid[j][x] != BLACK_SQUARE) {
                        word.add(grid[j][x])
                        j++
                    }
                    downClues.add(
                            Clue(
                                    Word(1000 + number, word), "$number",
                                    crossword.downClues[number] ?: error("No down clue for number $number")
                            )
                    )
                }
            }

            return Puzzle(
                    crossword.title,
                    crossword.author,
                    crossword.copyright,
                    crossword.notes,
                    grid,
                    listOf(ClueList("Across", acrossClues), ClueList("Down", downClues)),
                    crosswordSolverSettings = crosswordSolverSettings
            )
        }

        /**
         * Format a raw clue as valid inner HTML for a JPZ file.
         *
         * <p>Invalid XML characters are escaped, and text surrounded by asterisks is italicized.
         */
        private fun formatClue(rawClue: String): String {
            return rawClue.replace("&", "&amp;").replace("<", "&lt;")
                    .replace("\\*([^*]+)\\*".toRegex(), "<i>$1</i>")
        }
    }
}