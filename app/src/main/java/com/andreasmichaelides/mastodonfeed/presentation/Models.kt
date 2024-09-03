package com.andreasmichaelides.mastodonfeed.presentation

import android.util.Log
import com.andreasmichaelides.api.domain.FeedItem
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalUnit

interface UiModel

interface StateModel

sealed interface Action {
    data object LoadItems : Action
}

interface Input<T : StateModel> {
    fun transform(stateModel: T): T
}

data class FeedUiModel(
    val uiFeedItems: List<FeedItem>,
) : UiModel

data class FeedState(
    val feedItems: List<FeedItem>,
    val filteredFeedItems: List<FeedItem>,
    val filter: String,
    val lifespanInSeconds: Long
) : StateModel

sealed interface FeedInput : Input<FeedState> {

    data class FeedItemLoadedInput(val feedItem: FeedItem) : FeedInput {
        override fun transform(stateModel: FeedState): FeedState {
            val feedItems = stateModel.feedItems.toMutableList().apply {
                add(feedItem)
            }
            val filteredItems = filterFeedItems(feedItems, stateModel.filter)
            return stateModel.copy(
                feedItems = feedItems,
                filteredFeedItems = filteredItems
            )
        }
    }

    data class SearchInput(val filter: String) : FeedInput {
        override fun transform(stateModel: FeedState): FeedState {
            return stateModel.copy(
                filter = filter,
                filteredFeedItems = filterFeedItems(stateModel.feedItems, filter)
            )
        }
    }

    data class RemoveExpiredFeedsInput(val currentTimeInMillis: Long) : FeedInput {
        override fun transform(stateModel: FeedState): FeedState {
            val updatedFeedItems = stateModel.feedItems.filter {

                val expiryTimeInMillis = it.addedDateInMillis + Duration.ofSeconds(stateModel.lifespanInSeconds).toMillis()
//                Log.d("Pafto", "Added date: ${it.addedDate} expiry $expiryDate")
//                expiryDate.isAfter(timeNow)
                expiryTimeInMillis >= currentTimeInMillis
            }


            return stateModel.copy(
                feedItems = updatedFeedItems,
                filteredFeedItems = filterFeedItems(updatedFeedItems, stateModel.filter)
            )
        }
    }

    fun filterFeedItems(feedItems: List<FeedItem>, filter: String) =
        if (filter.isEmpty()) {
            feedItems
        } else {
            feedItems.filter {
                it.content.contains(filter) || it.userName.contains(filter) || it.displayName.contains(
                    filter
                )
            }
        }
}