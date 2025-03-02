package com.groupscholar.impactstories

import java.math.BigDecimal
import java.time.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class StoryRepository {
    fun addStory(
        title: String,
        summary: String,
        program: String,
        outcome: String,
        location: String,
        createdAt: Instant = Instant.now()
    ): Int = transaction {
        Stories.insertAndGetId {
            it[Stories.title] = title
            it[Stories.summary] = summary
            it[Stories.program] = program
            it[Stories.outcome] = outcome
            it[Stories.location] = location
            it[Stories.createdAt] = createdAt
        }.value
    }

    fun listStories(): List<StoryRow> = transaction {
        Stories.selectAll()
            .orderBy(Stories.createdAt, SortOrder.DESC)
            .map { row ->
                StoryRow(
                    id = row[Stories.id].value,
                    title = row[Stories.title],
                    program = row[Stories.program],
                    outcome = row[Stories.outcome],
                    location = row[Stories.location],
                    createdAt = row[Stories.createdAt]
                )
            }
    }

    fun getStory(storyId: Int): StoryRow? = transaction {
        Stories.selectAll().where { Stories.id eq storyId }
            .limit(1)
            .map { row ->
                StoryRow(
                    id = row[Stories.id].value,
                    title = row[Stories.title],
                    program = row[Stories.program],
                    outcome = row[Stories.outcome],
                    location = row[Stories.location],
                    createdAt = row[Stories.createdAt]
                )
            }
            .firstOrNull()
    }

    fun storyCount(): Long = transaction { Stories.selectAll().count() }

    fun addMetric(storyId: Int, name: String, value: BigDecimal, unit: String): Int = transaction {
        Metrics.insertAndGetId {
            it[Metrics.storyId] = storyId
            it[Metrics.name] = name
            it[Metrics.value] = value
            it[Metrics.unit] = unit
        }.value
    }

    fun listMetrics(storyId: Int?): List<MetricRow> = transaction {
        val query = if (storyId == null) Metrics.selectAll() else Metrics.selectAll().where { Metrics.storyId eq storyId }
        query
            .orderBy(Metrics.id, SortOrder.ASC)
            .map { row ->
                MetricRow(
                    id = row[Metrics.id].value,
                    storyId = row[Metrics.storyId].value,
                    name = row[Metrics.name],
                    value = row[Metrics.value],
                    unit = row[Metrics.unit]
                )
            }
    }

    fun metricCount(): Long = transaction { Metrics.selectAll().count() }

    fun addTag(label: String): Int = transaction {
        Tags.insertIgnore {
            it[Tags.label] = label
        }
        Tags.selectAll().where { Tags.label eq label }
            .first()[Tags.id].value
    }

    fun assignTag(storyId: Int, tagId: Int) = transaction {
        StoryTags.insertIgnore {
            it[StoryTags.storyId] = storyId
            it[StoryTags.tagId] = tagId
        }
    }

    fun listTags(storyId: Int?): List<TagRow> = transaction {
        if (storyId == null) {
            Tags.selectAll().orderBy(Tags.label, SortOrder.ASC).map { row ->
                TagRow(id = row[Tags.id].value, label = row[Tags.label])
            }
        } else {
            (Tags innerJoin StoryTags).selectAll().where { StoryTags.storyId eq storyId }
                .orderBy(Tags.label, SortOrder.ASC)
                .map { row -> TagRow(id = row[Tags.id].value, label = row[Tags.label]) }
        }
    }

    fun outcomeSummary(): List<OutcomeSummaryRow> = transaction {
        Stories.select(Stories.outcome, Stories.id.count())
            .groupBy(Stories.outcome)
            .orderBy(Stories.id.count(), SortOrder.DESC)
            .map { row ->
                OutcomeSummaryRow(
                    outcome = row[Stories.outcome],
                    storyCount = row[Stories.id.count()]
                )
            }
    }

    fun clearAll() = transaction {
        StoryTags.deleteAll()
        Metrics.deleteAll()
        Stories.deleteAll()
        Tags.deleteAll()
    }
}

data class StoryRow(
    val id: Int,
    val title: String,
    val program: String,
    val outcome: String,
    val location: String,
    val createdAt: Instant
)

data class MetricRow(
    val id: Int,
    val storyId: Int,
    val name: String,
    val value: BigDecimal,
    val unit: String
)

data class TagRow(
    val id: Int,
    val label: String
)

data class OutcomeSummaryRow(
    val outcome: String,
    val storyCount: Long
)
