package com.andreasmichaelides.mastodonfeed.presentation

import com.andreasmichaelides.api.domain.FeedItem
import java.time.Duration

interface UiModel

interface StateModel

sealed interface Action {
    data object LoadItems : Action
}

interface Input<T : StateModel> {
    fun transform(stateModel: T): T
}

interface InputWithActions<T : StateModel, A : Action> : Input<T> {
    fun getActionsExecutedAfterStateUpdate(): List<A>
}

data class FeedUiModel(
    val uiFeedItems: List<FeedItem>,
) : UiModel

data class FeedState(
    val feedItems: List<FeedItem>,
    val filteredFeedItems: List<FeedItem>,
    val filter: String,
    val lifespanInSeconds: Long,
    val isConnectedToTheInternet: Boolean
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
            return if (stateModel.isConnectedToTheInternet) {
                val updatedFeedItems = stateModel.feedItems.filter {
                    val expiryTimeInMillis = it.addedDateInMillis + Duration.ofSeconds(stateModel.lifespanInSeconds).toMillis()
                    expiryTimeInMillis >= currentTimeInMillis
                }

                stateModel.copy(
                    feedItems = updatedFeedItems,
                    filteredFeedItems = filterFeedItems(updatedFeedItems, stateModel.filter)
                )
            } else {
                stateModel
            }
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

sealed interface FeedInputWithActions : InputWithActions<FeedState, Action> {

    data class OnInternetConnectionStateChanged(val isConnectedToTheInternet: Boolean) : FeedInputWithActions {
        override fun getActionsExecutedAfterStateUpdate(): List<Action> {
            return if (isConnectedToTheInternet) {
                listOf(Action.LoadItems)
            } else {
                emptyList()
            }
        }

        override fun transform(stateModel: FeedState): FeedState {
            return stateModel.copy(
                isConnectedToTheInternet = isConnectedToTheInternet
            )
        }
    }
}