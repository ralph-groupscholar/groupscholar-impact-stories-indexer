package com.groupscholar.impactstories

import java.math.BigDecimal

object SeedData {
    data class MetricSeed(val name: String, val value: BigDecimal, val unit: String)
    data class StorySeed(
        val title: String,
        val summary: String,
        val program: String,
        val outcome: String,
        val location: String,
        val metrics: List<MetricSeed>,
        val tags: List<String>
    )

    private val stories = listOf(
        StorySeed(
            title = "First-Gen Scholars Reach Graduation Milestone",
            summary = "Cohort of first-generation scholars completed capstone projects and graduated with peer mentoring support.",
            program = "First-Gen Success Track",
            outcome = "Graduation",
            location = "Chicago, IL",
            metrics = listOf(
                MetricSeed("Graduation rate", BigDecimal("92.50"), "%"),
                MetricSeed("Peer mentors activated", BigDecimal("18"), "mentors"),
                MetricSeed("Capstone projects", BigDecimal("24"), "projects")
            ),
            tags = listOf("first-gen", "mentoring", "graduation")
        ),
        StorySeed(
            title = "STEM Scholars Secure Industry Internships",
            summary = "Scholar cohort partnered with regional employers to land paid internships in emerging tech roles.",
            program = "STEM Bridge",
            outcome = "Internship Placement",
            location = "Austin, TX",
            metrics = listOf(
                MetricSeed("Internships secured", BigDecimal("31"), "placements"),
                MetricSeed("Average hourly wage", BigDecimal("24.75"), "USD"),
                MetricSeed("Employer partners", BigDecimal("7"), "partners")
            ),
            tags = listOf("stem", "employer", "workforce")
        ),
        StorySeed(
            title = "Transfer Scholars Increase Completion Rate",
            summary = "Wraparound advising helped transfer scholars persist through the first year after transfer.",
            program = "Transfer Momentum",
            outcome = "Retention",
            location = "Phoenix, AZ",
            metrics = listOf(
                MetricSeed("First-year retention", BigDecimal("88.00"), "%"),
                MetricSeed("Advising hours", BigDecimal("146"), "hours"),
                MetricSeed("Emergency grants", BigDecimal("12"), "grants")
            ),
            tags = listOf("transfer", "retention", "advising")
        ),
        StorySeed(
            title = "Community College Scholars Advance to Four-Year Programs",
            summary = "Scholars completed transfer pathways with personalized financial coaching and coaching pods.",
            program = "Pathway Launch",
            outcome = "Transfer",
            location = "Sacramento, CA",
            metrics = listOf(
                MetricSeed("Transfers completed", BigDecimal("19"), "students"),
                MetricSeed("Coaching sessions", BigDecimal("84"), "sessions"),
                MetricSeed("Aid packages optimized", BigDecimal("17"), "packages")
            ),
            tags = listOf("transfer", "financial-coaching", "community-college")
        )
    )

    fun seed(repository: StoryRepository, force: Boolean): SeedResult {
        if (!force && repository.storyCount() > 0) {
            return SeedResult(false, "Skipped seeding because stories already exist.")
        }

        if (force) {
            repository.clearAll()
        }

        stories.forEach { story ->
            val storyId = repository.addStory(
                title = story.title,
                summary = story.summary,
                program = story.program,
                outcome = story.outcome,
                location = story.location
            )
            story.metrics.forEach { metric ->
                repository.addMetric(storyId, metric.name, metric.value, metric.unit)
            }
            story.tags.forEach { tag ->
                val tagId = repository.addTag(tag)
                repository.assignTag(storyId, tagId)
            }
        }

        return SeedResult(true, "Seeded ${stories.size} stories.")
    }
}

data class SeedResult(val seeded: Boolean, val message: String)
