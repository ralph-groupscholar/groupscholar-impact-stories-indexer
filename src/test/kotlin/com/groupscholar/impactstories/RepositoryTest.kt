package com.groupscholar.impactstories

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
        assertEquals("Scholar completes apprenticeship", story.title)
        assertEquals("Scholar finished an apprenticeship with industry partner.", story.summary)

        val detail = repository.getStoryDetail(storyId)
        assertNotNull(detail)
        assertEquals("Apprenticeship Pathway", detail.story.program)
        assertEquals(1, detail.metrics.size)
        assertEquals("Placement rate", detail.metrics.first().name)
        assertEquals(1, detail.tags.size)
        assertEquals("employment", detail.tags.first().label)

        val summary = repository.outcomeSummary()
        assertEquals(1, summary.size)
        assertEquals("Employment", summary.first().outcome)
        assertEquals(1, summary.first().storyCount)

        val programSummary = repository.programSummary()
        assertEquals(1, programSummary.size)
        assertEquals("Apprenticeship Pathway", programSummary.first().program)
        assertEquals(1, programSummary.first().storyCount)
        assertTrue(programSummary.first().latestStoryAt != null)
    }

    @Test
    fun `creates story briefs and program summaries`() {
        connectTestDb()
        val repository = StoryRepository()

        val storyA = repository.addStory(
            title = "Scholar leads community workshop",
            summary = "Scholar taught a 12-week coding series for local students.",
            program = "Community Impact",
            outcome = "Leadership",
            location = "Detroit, MI"
        )
        val storyB = repository.addStory(
            title = "Internship placement with partner org",
            summary = "Scholar placed in a summer internship with partner organization.",
            program = "Career Launch",
            outcome = "Employment",
            location = "Raleigh, NC"
        )

        repository.addMetric(storyA, "Participants", BigDecimal("45"), "students")
        repository.addMetric(storyA, "Completion rate", BigDecimal("88.00"), "%")
        val tagId = repository.addTag("community")
        repository.assignTag(storyA, tagId)

        val storyDetail = repository.getStoryDetail(storyA)
        assertNotNull(storyDetail)
        assertEquals("Community Impact", storyDetail.story.program)
        assertEquals(2, storyDetail.metrics.size)
        assertEquals(1, storyDetail.tags.size)

        val tagSearch = repository.searchStories(
            program = null,
            outcome = null,
            location = null,
            tag = "community",
            query = null
        )
        assertEquals(1, tagSearch.size)

        val querySearch = repository.searchStories(
            program = null,
            outcome = null,
            location = null,
            tag = null,
            query = "internship"
        )
        assertEquals(1, querySearch.size)

        val programSummary = repository.programSummary()
        assertEquals(2, programSummary.size)
        assertEquals(1, programSummary.first { it.program == "Community Impact" }.storyCount)
        assertEquals(1, programSummary.first { it.program == "Career Launch" }.storyCount)
        assertNotNull(programSummary.first { it.program == "Community Impact" }.latestStoryAt)

        assertTrue(programSummary.all { it.latestStoryAt != null })
        assertEquals(storyB, repository.getStory(storyB)?.id)
    }

    @Test
    fun `exports stories with metrics and tags`() {
        connectTestDb()
        val repository = StoryRepository()

        val storyId = repository.addStory(
            title = "Scholar launches research initiative",
            summary = "Scholar partnered with faculty to launch a civic tech lab.",
            program = "Research Fellows",
            outcome = "Leadership",
            location = "Oakland, CA"
        )

        repository.addMetric(storyId, "Participants", BigDecimal("18"), "scholars")
        val tagId = repository.addTag("research")
        repository.assignTag(storyId, tagId)

        val exports = repository.exportStories()
        assertTrue(exports.isNotEmpty())
        val exported = exports.first { it.story.id == storyId }
        assertEquals("Research Fellows", exported.story.program)
        assertEquals(1, exported.metrics.size)
        assertEquals("Participants", exported.metrics.first().name)
        assertEquals(1, exported.tags.size)
        assertEquals("research", exported.tags.first().label)
    }
}
