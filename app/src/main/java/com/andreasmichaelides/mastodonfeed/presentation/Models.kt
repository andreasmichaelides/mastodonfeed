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
    val uiFeedItems: List<UiFeedItem>,
    val searchFilter: String
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
            val trimmedFilter = filter.trim()
            return stateModel.copy(
                filter = trimmedFilter,
                filteredFeedItems = filterFeedItems(stateModel.feedItems, trimmedFilter)
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
            val lowerCaseFilter = filter.lowercase()
            feedItems.filter {
                it.content.lowercase().contains(lowerCaseFilter)
                        || it.userName.lowercase().contains(lowerCaseFilter)
                        || it.displayName.lowercase().contains(lowerCaseFilter)
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

fun getInitialFeedStateModel(lifeSpanInSeconds: Long) = FeedState(
    feedItems = emptyList(),
    filteredFeedItems = emptyList(),
    filter = "",
    lifespanInSeconds = lifeSpanInSeconds,
    isConnectedToTheInternet = false
)

fun getInitialFeedUiModel() = FeedUiModel(uiFeedItems = emptyList(), searchFilter = "")