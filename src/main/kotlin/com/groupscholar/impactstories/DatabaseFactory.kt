package com.groupscholar.impactstories

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun connectFromEnv(): Database {
        val url = System.getenv("GS_DB_URL")?.trim().orEmpty()
        val user = System.getenv("GS_DB_USER")?.trim().orEmpty()
        val password = System.getenv("GS_DB_PASSWORD")?.trim().orEmpty()

        if (url.isEmpty() || user.isEmpty() || password.isEmpty()) {
            error("Missing GS_DB_URL, GS_DB_USER, or GS_DB_PASSWORD env vars.")
        }

        val jdbcUrl = if (url.startsWith("jdbc:")) url else "jdbc:postgresql://$url"

        return Database.connect(
            url = jdbcUrl,
            driver = "org.postgresql.Driver",
            user = user,
            password = password
        )
    }

    fun connectWith(url: String, user: String, password: String, driver: String): Database {
        return Database.connect(url = url, driver = driver, user = user, password = password)
    }

    fun migrate() {
        transaction {
            connection.prepareStatement("CREATE SCHEMA IF NOT EXISTS \"gs_impact_stories\"", false)
                .executeUpdate()
            SchemaUtils.create(Stories, Metrics, Tags, StoryTags)
        }
    }
}
