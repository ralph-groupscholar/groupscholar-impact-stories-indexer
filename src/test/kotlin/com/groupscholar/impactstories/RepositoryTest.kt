package com.groupscholar.impactstories

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RepositoryTest {
    private fun connectTestDb() {
        DatabaseFactory.connectWith(
            url = "jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
            user = "sa",
            password = "",
            driver = "org.h2.Driver"
        )
        DatabaseFactory.migrate()
    }

    @Test
    fun `adds and summarizes stories`() {
        connectTestDb()
        val repository = StoryRepository()

        val storyId = repository.addStory(
            title = "Scholar completes apprenticeship",
            summary = "Scholar finished an apprenticeship with industry partner.",
            program = "Apprenticeship Pathway",
            outcome = "Employment",
            location = "Baltimore, MD"
        )

        repository.addMetric(storyId, "Placement rate", BigDecimal("100.00"), "%")
        val tagId = repository.addTag("employment")
        repository.assignTag(storyId, tagId)

        val story = repository.getStory(storyId)
        assertNotNull(story)

        val summary = repository.outcomeSummary()
        assertEquals(1, summary.size)
        assertEquals("Employment", summary.first().outcome)
        assertEquals(1, summary.first().storyCount)
    }
}
