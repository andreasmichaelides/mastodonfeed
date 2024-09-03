package com.andreasmichaelides.mastodonfeed.presentation

import android.text.Spanned

data class UiFeedItem(
    val displayName: String,
    val content: Spanned,
    val avatarUrl: String,
    val userName: String
)
