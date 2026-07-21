package com.pequenomorrison.grpc.util

import com.pequenomorrison.grpc.books.Category

object CategoryMapper {
    private val byDatabaseName = mapOf(
        "fiction" to Category.CATEGORY_FICTION,
        "fantasy" to Category.CATEGORY_FANTASY,
        "science_fiction" to Category.CATEGORY_SCIENCE_FICTION,
        "mystery" to Category.CATEGORY_MYSTERY,
        "thriller" to Category.CATEGORY_THRILLER,
        "romance" to Category.CATEGORY_ROMANCE,
        "horror" to Category.CATEGORY_HORROR,
        "adventure" to Category.CATEGORY_ADVENTURE,
        "history" to Category.CATEGORY_HISTORY,
        "biography" to Category.CATEGORY_BIOGRAPHY,
        "science" to Category.CATEGORY_SCIENCE,
        "technology" to Category.CATEGORY_TECHNOLOGY,
        "self_help" to Category.CATEGORY_SELF_HELP,
        "children" to Category.CATEGORY_CHILDREN,
        "comics" to Category.CATEGORY_COMICS,
    )

    fun fromDatabase(name: String): Category = byDatabaseName[normalize(name)] ?: Category.CATEGORY_UNSPECIFIED
    fun toDatabase(category: Category): String? = byDatabaseName.entries.firstOrNull { it.value == category }?.key
    private fun normalize(value: String) = value.trim().lowercase().replace('-', '_').replace(' ', '_')
}
