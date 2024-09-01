package com.andreasmichaelides.api.domain

import java.time.LocalDate

data class StatusItem(
    val id: String,
    val content: String,
    val userName: String,
    val displayName: String,
    val avatarUrl: String,
    val imageUrl: String,
    val linkUrl: String,
    val createdDate: LocalDate
)
