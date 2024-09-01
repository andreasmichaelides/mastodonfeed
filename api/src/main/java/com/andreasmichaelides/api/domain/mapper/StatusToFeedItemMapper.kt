package com.andreasmichaelides.api.domain.mapper

import com.andreasmichaelides.api.domain.StatusItem
import social.bigbone.api.entity.Status
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class StatusToFeedItemMapper @Inject constructor() {

    operator fun invoke(status: Status): StatusItem {
        return StatusItem(
            id = status.id,
            content = status.content,
            userName = status.account?.username.orEmpty(),
            displayName = status.account?.displayName.orEmpty(),
            avatarUrl = status.account?.avatar.orEmpty(),
            imageUrl = "",
            linkUrl = status.url,
            createdDate = status.createdAt.mostPreciseInstantOrNull()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.now()
        )
    }

}