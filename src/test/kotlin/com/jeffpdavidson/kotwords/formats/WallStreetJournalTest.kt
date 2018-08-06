package com.jeffpdavidson.kotwords.formats

import com.jeffpdavidson.kotwords.readBinaryResource
import com.jeffpdavidson.kotwords.readUtf8Resource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class WallStreetJournalTest {
    @Test
    fun basicPuzzle() {
        Assertions.assertArrayEquals(
                WallStreetJournalTest::class.readBinaryResource("puz/test-nocircles.puz"),
                WallStreetJournal(WallStreetJournalTest::class.readUtf8Resource("wsj/test-nocircles.json"),
                        includeDateInTitle = false)
                        .asCrossword().toAcrossLiteBinary())
    }
}