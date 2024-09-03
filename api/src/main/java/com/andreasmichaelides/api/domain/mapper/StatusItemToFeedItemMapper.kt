package com.andreasmichaelides.api.domain.mapper

import com.andreasmichaelides.api.domain.FeedItem
import com.andreasmichaelides.api.domain.StatusItem
import com.andreasmichaelides.api.domain.TimeProvider
import java.time.LocalDateTime
import javax.inject.Inject

class StatusItemToFeedItemMapper @Inject constructor(private val timeProvider: TimeProvider) {

    operator fun invoke(status: StatusItem): FeedItem {
        return FeedItem(
            id = status.id,
            content = status.content,
            userName = status.userName,
            displayName = status.displayName,
            avatarUrl = status.avatarUrl,
            imageUrl = status.imageUrl,
            linkUrl = status.linkUrl,
            createdDate = status.createdDate,
            addedDateInMillis = timeProvider.getCurrentTimeInMillis()
        )
    }

}