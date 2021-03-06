package com.jeffpdavidson.kotwords.model

import com.jeffpdavidson.kotwords.formats.AcrossLite
import com.jeffpdavidson.kotwords.model.DownsOnly.getDirectionToClearForDownsOnly
import com.jeffpdavidson.kotwords.readBinaryResource
import com.jeffpdavidson.kotwords.runTest
import org.junit.Test
import kotlin.test.assertEquals

class DownsOnlyTest {
    @Test
    fun getDirectionToClearForDownsOnly() = runTest {
        val crossword = AcrossLite(
                readBinaryResource(DownsOnlyTest::class, "puz/test.puz")
        ).asCrossword()
        assertEquals(DownsOnly.ClueDirection.DOWN, crossword.getDirectionToClearForDownsOnly())
    }
}