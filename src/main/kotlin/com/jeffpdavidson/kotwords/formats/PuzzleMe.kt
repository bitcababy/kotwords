package com.jeffpdavidson.kotwords.formats

import com.jeffpdavidson.kotwords.model.BLACK_SQUARE
import com.jeffpdavidson.kotwords.model.Crossword
import com.jeffpdavidson.kotwords.model.Square
import org.jsoup.Jsoup
import se.ansman.kotshi.JsonDefaultValue
import se.ansman.kotshi.JsonSerializable
import java.nio.charset.StandardCharsets
import java.util.Base64

var PUZZLE_DATA_REGEX = """\bwindow\.rawc\s*=\s*'([^']+)'""".toRegex()

/** Container for a puzzle in the PuzzleMe (Amuse Labs) format. */
class PuzzleMe(private val html: String) : Crosswordable {
    @JsonSerializable
    internal data class Clue(val clue: String)

    @JsonSerializable
    internal data class PlacedWord(val clue: Clue, val clueNum: Int, val acrossNotDown: Boolean)

    @JsonSerializable
    internal data class CellInfo(val x: Int, val y: Int, val isCircled: Boolean)

    @JsonSerializable
    internal data class Data(
            val title: String,
            val description: String,
            val copyright: String,
            val author: String,
            val box: List<List<String>>,
            @JsonDefaultValue val cellInfos: List<CellInfo>,
            val placedWords: List<PlacedWord>,
            // List of circled squares locations in the form [x, y]
            @JsonDefaultValue val backgroundShapeBoxes: List<List<Int>>)

    override fun asCrossword(): Crossword {
        return toCrossword(extractPuzzleJson(html))
    }

    companion object {
        internal fun extractPuzzleJson(html: String): String {
            // Look for "window.rawc = '[data]'" inside <script> tags; this is JSON puzzle data
            // encoded as Base64.
            Jsoup.parse(html).getElementsByTag("script").forEach {
                val matchResult = PUZZLE_DATA_REGEX.find(it.data())
                if (matchResult != null) {
                    return String(
                            Base64.getDecoder().decode(matchResult.groupValues[1]),
                            StandardCharsets.UTF_8)
                }
            }
            throw InvalidFormatException("Could not find puzzle data in PuzzleMe HTML")
        }

        internal fun toCrossword(json: String): Crossword {
            val data = JsonSerializer.fromJson(Data::class.java, json)
            val grid: MutableList<MutableList<Square>> = mutableListOf()
            val circledCells = data.cellInfos.filter { it.isCircled }.map { it -> it.x to it.y }
            for (y in 0 until data.box[0].size) {
                val row: MutableList<Square> = mutableListOf()
                for (x in 0 until data.box.size) {
                    if (data.box[x][y] == "\u0000") {
                        row.add(BLACK_SQUARE)
                    } else {
                        val solutionRebus = if (data.box[x][y].length > 1) data.box[x][y] else ""
                        val isCircled = data.backgroundShapeBoxes.contains(listOf(x, y))
                                || circledCells.contains(x to y)
                        row.add(Square(
                                solution = data.box[x][y][0],
                                solutionRebus = solutionRebus,
                                isCircled = isCircled))
                    }
                }
                grid.add(row)
            }
            return Crossword(
                    title = data.title,
                    author = data.author,
                    copyright = data.copyright,
                    notes = data.description,
                    grid = grid,
                    acrossClues = data.placedWords
                            .filter { it.acrossNotDown }.map { it.clueNum to it.clue.clue }.toMap(),
                    downClues = data.placedWords
                            .filter { !it.acrossNotDown }.map { it.clueNum to it.clue.clue }.toMap()
            )
        }
    }
}