package com.groupscholar.impactstories

import java.math.BigDecimal
import java.time.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
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
                    summary = row[Stories.summary],
                    program = row[Stories.program],
                    outcome = row[Stories.outcome],
                    location = row[Stories.location],
                    createdAt = row[Stories.createdAt]
                )
            }
    }

    fun searchStories(
        program: String?,
        outcome: String?,
        location: String?,
        tag: String?,
        query: String?
    ): List<StoryRow> = transaction {
        var filters: Op<Boolean> = Op.TRUE
        if (!program.isNullOrBlank()) {
            filters = filters and (Stories.program eq program)
        }
        if (!outcome.isNullOrBlank()) {
            filters = filters and (Stories.outcome eq outcome)
        }
        if (!location.isNullOrBlank()) {
            filters = filters and (Stories.location eq location)
        }
        if (!query.isNullOrBlank()) {
            val pattern = "%$query%"
            filters = filters and ((Stories.title like pattern) or (Stories.summary like pattern))
        }

        val baseQuery = if (!tag.isNullOrBlank()) {
            (Stories innerJoin StoryTags innerJoin Tags)
                .selectAll()
                .where { filters and (Tags.label eq tag) }
                .withDistinct()
        } else {
            Stories.selectAll().where { filters }
        }

        baseQuery.orderBy(Stories.createdAt, SortOrder.DESC)
            .map { row ->
                StoryRow(
                    id = row[Stories.id].value,
                    title = row[Stories.title],
                    summary = row[Stories.summary],
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
                    summary = row[Stories.summary],
                    program = row[Stories.program],
                    outcome = row[Stories.outcome],
                    location = row[Stories.location],
                    createdAt = row[Stories.createdAt]
                )
            }
            .firstOrNull()
    }

    fun getStoryDetail(storyId: Int): StoryDetail? = transaction {
        Stories.selectAll().where { Stories.id eq storyId }
            .limit(1)
            .map { row ->
                StoryRow(
                    id = row[Stories.id].value,
                    title = row[Stories.title],
                    summary = row[Stories.summary],
                    program = row[Stories.program],
                    outcome = row[Stories.outcome],
                    location = row[Stories.location],
                    createdAt = row[Stories.createdAt]
                )
            }
            .firstOrNull()
            ?.let { story ->
                val metrics = Metrics.selectAll().where { Metrics.storyId eq storyId }
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
                val tags = (Tags innerJoin StoryTags).selectAll().where { StoryTags.storyId eq storyId }
                    .orderBy(Tags.label, SortOrder.ASC)
                    .map { row -> TagRow(id = row[Tags.id].value, label = row[Tags.label]) }
                StoryDetail(story = story, metrics = metrics, tags = tags)
            }
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

    fun programSummary(): List<ProgramSummaryRow> = transaction {
        val countExpr = Stories.id.count()
        val maxExpr = Stories.createdAt.max()
        Stories.select(Stories.program, countExpr, maxExpr)
            .groupBy(Stories.program)
            .orderBy(countExpr to SortOrder.DESC, Stories.program to SortOrder.ASC)
            .map { row ->
                ProgramSummaryRow(
                    program = row[Stories.program],
                    storyCount = row[countExpr],
                    latestStoryAt = row[maxExpr]
                )
            }
    }

    fun exportStories(): List<StoryExportRow> = transaction {
        val stories = Stories.selectAll()
            .orderBy(Stories.createdAt, SortOrder.DESC)
            .map { row ->
                StoryRow(
                    id = row[Stories.id].value,
                    title = row[Stories.title],
                    summary = row[Stories.summary],
                    program = row[Stories.program],
                    outcome = row[Stories.outcome],
                    location = row[Stories.location],
                    createdAt = row[Stories.createdAt]
                )
            }

        stories.map { story ->
            val metrics = Metrics.selectAll().where { Metrics.storyId eq story.id }
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
            val tags = (Tags innerJoin StoryTags).selectAll().where { StoryTags.storyId eq story.id }
                .orderBy(Tags.label, SortOrder.ASC)
                .map { row -> TagRow(id = row[Tags.id].value, label = row[Tags.label]) }
            StoryExportRow(story = story, metrics = metrics, tags = tags)
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
    val summary: String,
    val program: String,
    val outcome: String,
    val location: String,
    val createdAt: Instant
)

data class StoryDetail(
    val story: StoryRow,
    val metrics: List<MetricRow>,
    val tags: List<TagRow>
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

data class ProgramSummaryRow(
    val program: String,
    val storyCount: Long,
    val latestStoryAt: Instant?
)

data class StoryExportRow(
    val story: StoryRow,
    val metrics: List<MetricRow>,
    val tags: List<TagRow>
)
