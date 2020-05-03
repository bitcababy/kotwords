package com.jeffpdavidson.kotwords.model

import kotlin.test.Test
import kotlin.test.assertEquals

// TODO: Expand test coverage
class TwistsAndTurnsTest {
    @Test
    fun jpzGeneration() {
        // TODO: Figure out how to load this from a resource file.
        val expected = "<?xml version=1.0?>" +
                "<crossword-compiler-applet xmlns=\"http://crossword.info/xml/crossword-compiler\">" +
                "<applet-settings cursor-color=\"#00b100\" selected-cells-color=\"#80ff80\">" +
                "<completion friendly-submit=\"false\" only-if-correct=\"true\">All done!</completion>" +
                "<actions graphical-buttons=\"false\" wide-buttons=\"false\" buttons-layout=\"left\">" +
                "<reveal-word label=\"Reveal Word\"/>" +
                "<reveal-letter label=\"Reveal Letter\"/>" +
                "<check label=\"Check\"/>" +
                "<solution label=\"Solution\"/>" +
                "<pencil label=\"Pencil\"/>" +
                "</actions>" +
                "</applet-settings>" +
                "<rectangular-puzzle xmlns=\"http://crossword.info/xml/rectangular-puzzle\"" +
                " alphabet=\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\">" +
                "<metadata>" +
                "<title>Test title</title>" +
                "<creator>Test creator</creator>" +
                "<copyright>Test copyright</copyright>" +
                "<description>Test description</description>" +
                "</metadata>" +
                "<crossword>" +
                "<grid width=\"2\" height=\"2\">" +
                "<grid-look numbering-scheme=\"normal\"/>" +
                "<cell x=\"1\" y=\"1\" solution=\"A\" background-color=\"#FFFFFF\" number=\"1\"/>" +
                "<cell x=\"2\" y=\"1\" solution=\"B\" background-color=\"#999999\"/>" +
                "<cell x=\"1\" y=\"2\" solution=\"D\" background-color=\"#999999\"/>" +
                "<cell x=\"2\" y=\"2\" solution=\"C\" background-color=\"#FFFFFF\" number=\"2\"/>" +
                "</grid>" +
                "<word id=\"1\"><cells x=\"1\" y=\"1\"/><cells x=\"2\" y=\"1\"/></word>" +
                "<word id=\"2\"><cells x=\"2\" y=\"2\"/><cells x=\"1\" y=\"2\"/></word>" +
                "<word id=\"1001\"><cells x=\"1\" y=\"1\"/></word>" +
                "<word id=\"1002\"><cells x=\"2\" y=\"1\"/></word>" +
                "<word id=\"1003\"><cells x=\"1\" y=\"2\"/></word>" +
                "<word id=\"1004\"><cells x=\"2\" y=\"2\"/></word>" +
                "<clues ordering=\"normal\">" +
                "<title><b>Turns</b></title>" +
                "<clue word=\"1\" number=\"1\">Turn 1</clue>" +
                "<clue word=\"2\" number=\"2\">Turn 2</clue>" +
                "</clues>" +
                "<clues ordering=\"normal\">" +
                "<title><b>Twists</b></title>" +
                "<clue word=\"1001\" number=\"1\">Twist 1</clue>" +
                "<clue word=\"1002\" number=\"2\">Twist 2</clue>" +
                "<clue word=\"1003\" number=\"3\">Twist 3</clue>" +
                "<clue word=\"1004\" number=\"4\">Twist 4</clue>" +
                "</clues>" +
                "</crossword>" +
                "</rectangular-puzzle>" +
                "</crossword-compiler-applet>"

        val puzzle = TwistsAndTurns.fromRawInput(
                "Test title",
                "Test creator",
                "Test copyright",
                "Test description",
                "2",
                "2",
                "1",
                "AB CD",
                "Turn 1\nTurn 2",
                "Twist 1\nTwist 2\nTwist 3\nTwist 4",
                "#ffffff",
                "#888888",
                CrosswordSolverSettings("#00b100", "#80ff80", "All done!"))

        assertEquals(expected, puzzle.asJpz().asXmlString())
    }
}