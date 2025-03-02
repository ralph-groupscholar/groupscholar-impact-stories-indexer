package com.groupscholar.impactstories

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Stories : IntIdTable("gs_impact_stories.stories") {
    val title = varchar("title", 200)
    val summary = text("summary")
    val program = varchar("program", 120)
    val outcome = varchar("outcome", 120)
    val location = varchar("location", 120)
    val createdAt = timestamp("created_at")
}

object Metrics : IntIdTable("gs_impact_stories.metrics") {
    val storyId = reference("story_id", Stories, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 140)
    val value = decimal("value", 12, 2)
    val unit = varchar("unit", 40)
}

object Tags : IntIdTable("gs_impact_stories.tags") {
    val label = varchar("label", 80).uniqueIndex()
}

object StoryTags : Table("gs_impact_stories.story_tags") {
    val storyId = reference("story_id", Stories, onDelete = ReferenceOption.CASCADE)
    val tagId = reference("tag_id", Tags, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(storyId, tagId)
}
