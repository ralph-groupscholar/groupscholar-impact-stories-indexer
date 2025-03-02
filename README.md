# Group Scholar Impact Stories Indexer

A Kotlin CLI that captures, tags, and summarizes impact stories for Group Scholar programs. It creates a structured, queryable library of outcomes, metrics, and narratives so weekly briefings and donor updates are faster to assemble.

## Features
- Store impact stories with program, outcome, and location context
- Attach quantitative metrics to each story
- Tag stories for faster retrieval
- Generate outcome summaries for reporting
- Seed sample data for instant usefulness

## Tech Stack
- Kotlin 2.0
- Kotlinx CLI
- Exposed ORM
- PostgreSQL
- JUnit + H2 (tests)

## Getting Started

### Environment
Set database credentials via environment variables (never commit them):

```
export GS_DB_URL="db-host:23947/dbname"
export GS_DB_USER="your-user"
export GS_DB_PASSWORD="your-password"
```

For local development, point `GS_DB_URL` to a local PostgreSQL instance. Production credentials should be supplied via your deployment environment.

### Commands

```
./gradlew run --args="migrate"
./gradlew run --args="seed"
./gradlew run --args="seed --force"
./gradlew run --args="list-stories"
./gradlew run --args="add-story --title=\"Title\" --summary=\"Summary\" --program=\"Program\" --outcome=\"Outcome\" --location=\"Location\""
./gradlew run --args="add-metric --story-id=1 --metric-name=\"scholars supported\" --value=12 --unit=students"
./gradlew run --args="list-metrics --story-id=1"
./gradlew run --args="list-tags"
./gradlew run --args="add-tag --label=\"first-gen\""
./gradlew run --args="assign-tag --story-id=1 --tag-id=2"
./gradlew run --args="outcome-summary"
```

## Testing

```
./gradlew test
```

## Project Structure
- `src/main/kotlin/com/groupscholar/impactstories`: CLI, database, and repository
- `src/test/kotlin/com/groupscholar/impactstories`: repository tests

## Notes
- Tables are namespaced under the `gs_impact_stories` schema in PostgreSQL.
- `seed` skips when data already exists; use `seed --force` to refresh.
