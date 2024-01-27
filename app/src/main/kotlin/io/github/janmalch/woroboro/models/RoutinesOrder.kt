package io.github.janmalch.woroboro.models

enum class RoutinesOrder {
    // Name changes must also update the SQL queries!
    NameAsc,
    NameDesc,
    LastRunRecently,
    LastRunLongAgo,
}
