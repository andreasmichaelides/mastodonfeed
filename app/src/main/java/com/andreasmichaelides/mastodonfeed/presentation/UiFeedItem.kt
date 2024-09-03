package com.andreasmichaelides.mastodonfeed.presentation

data class UiFeedItem(
    val displayName: String,
    val content: String,
    val avatarUrl: String,
    val userName: String
)
