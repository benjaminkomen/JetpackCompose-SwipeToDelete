package com.benjaminkomen.composelab

import java.util.UUID

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isImportant: Boolean = false
)
