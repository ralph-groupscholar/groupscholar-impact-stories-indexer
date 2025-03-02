package com.groupscholar.impactstories

import java.math.BigDecimal
import java.time.Instant
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.cli.required

fun main(args: Array<String>) {
    val parser = ArgParser("impact-stories-indexer")

    parser.subcommands(
        MigrateCommand(),
        SeedCommand(),
        AddStoryCommand(),
        ListStoriesCommand(),
        AddMetricCommand(),
        ListMetricsCommand(),
        AddTagCommand(),
        AssignTagCommand(),
        ListTagsCommand(),
        OutcomeSummaryCommand()
    )

    parser.parse(args)
}

private fun connectAndMigrate() {
    DatabaseFactory.connectFromEnv()
    DatabaseFactory.migrate()
}

private fun formatStory(row: StoryRow): String {
    return "${row.id} | ${row.title} | ${row.program} | ${row.outcome} | ${row.location} | ${row.createdAt}"
}

private class MigrateCommand : Subcommand("migrate", "Create schema and tables.") {
    override fun execute() {
        connectAndMigrate()
        println("Migration complete.")
    }
}

private class SeedCommand : Subcommand("seed", "Insert sample impact stories, metrics, and tags.") {
    private val force by option(ArgType.Boolean, shortName = "f", description = "Clear existing data first.")
        .default(false)

    override fun execute() {
        connectAndMigrate()
        val repository = StoryRepository()
        val result = SeedData.seed(repository, force)
        println(result.message)
    }
}

private class AddStoryCommand : Subcommand("add-story", "Add a new impact story.") {
    private val title by option(ArgType.String, description = "Story title").required()
    private val summary by option(ArgType.String, description = "Short summary").required()
    private val program by option(ArgType.String, description = "Program name").required()
    private val outcome by option(ArgType.String, description = "Outcome label").required()
    private val location by option(ArgType.String, description = "Location").required()

    override fun execute() {
        connectAndMigrate()
        val repository = StoryRepository()
        val storyId = repository.addStory(
            title = title,
            summary = summary,
            program = program,
            outcome = outcome,
            location = location,
            createdAt = Instant.now()
        )
        println("Added story with id $storyId")
    }
}

private class ListStoriesCommand : Subcommand("list-stories", "List impact stories.") {
    override fun execute() {
        connectAndMigrate()
        val repository = StoryRepository()
        val stories = repository.listStories()
        if (stories.isEmpty()) {
            println("No stories found.")
            return
        }
        stories.forEach { println(formatStory(it)) }
    }
}

private class AddMetricCommand : Subcommand("add-metric", "Add a metric to a story.") {
    private val storyId by option(ArgType.Int, description = "Story id").required()
    private val metricName by option(ArgType.String, description = "Metric name").required()
    private val value by option(ArgType.String, description = "Metric value").required()
    private val unit by option(ArgType.String, description = "Metric unit").required()

    override fun execute() {
        connectAndMigrate()
        val repository = StoryRepository()
        val story = repository.getStory(storyId)
        require(story != null) { "Story $storyId not found." }
        val metricId = repository.addMetric(storyId, metricName, BigDecimal(value), unit)
        println("Added metric $metricId to story $storyId")
    }
}

private class ListMetricsCommand : Subcommand("list-metrics", "List metrics, optionally for a story.") {
    private val storyId by option(ArgType.Int, description = "Story id (optional)")

    override fun execute() {
        connectAndMigrate()
        val repository = StoryRepository()
        val metrics = repository.listMetrics(storyId)
        if (metrics.isEmpty()) {
            println("No metrics found.")
            return
        }
        metrics.forEach { metric ->
            println("${metric.id} | story ${metric.storyId} | ${metric.name} | ${metric.value} ${metric.unit}")
        }
    }
}

private class AddTagCommand : Subcommand("add-tag", "Create a tag label.") {
    private val label by option(ArgType.String, description = "Tag label").required()

    override fun execute() {
        connectAndMigrate()
        val repository = StoryRepository()
        val tagId = repository.addTag(label)
        println("Tag id $tagId ready.")
    }
}

private class AssignTagCommand : Subcommand("assign-tag", "Assign a tag to a story.") {
    private val storyId by option(ArgType.Int, description = "Story id").required()
    private val tagId by option(ArgType.Int, description = "Tag id").required()

    override fun execute() {
        connectAndMigrate()
        val repository = StoryRepository()
        val story = repository.getStory(storyId)
        require(story != null) { "Story $storyId not found." }
        repository.assignTag(storyId, tagId)
        println("Assigned tag $tagId to story $storyId")
    }
}

private class ListTagsCommand : Subcommand("list-tags", "List tags, optionally for a story.") {
    private val storyId by option(ArgType.Int, description = "Story id (optional)")

    override fun execute() {
        connectAndMigrate()
        val repository = StoryRepository()
        val tags = repository.listTags(storyId)
        if (tags.isEmpty()) {
            println("No tags found.")
            return
        }
        tags.forEach { tag -> println("${tag.id} | ${tag.label}") }
    }
}

private class OutcomeSummaryCommand : Subcommand("outcome-summary", "Summarize outcomes by story count.") {
    override fun execute() {
        connectAndMigrate()
        val repository = StoryRepository()
        val rows = repository.outcomeSummary()
        if (rows.isEmpty()) {
            println("No outcomes found.")
            return
        }
        rows.forEach { row -> println("${row.outcome} | ${row.storyCount}") }
    }
}
