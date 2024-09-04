package com.andreasmichaelides.mastodonfeed.presentation

import com.andreasmichaelides.api.domain.FeedItem
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.LocalDate


class FeedInputTest {

    @Test
    fun `given FeedState when filtered with an empty string then filtered items match the source items`() {
        val filter = ""
        val searchInput = FeedInput.SearchInput(filter)
        val feedItem = FeedItem(
            id = "",
            content = "",
            userName = "",
            displayName = "MegaMan",
            avatarUrl = "",
            imageUrl = "",
            linkUrl = "",
            createdDate = LocalDate.now(),
            addedDateInMillis = 10
        )
        val feedItems = listOf(feedItem, feedItem.copy(id = "34", displayName = "Youtuber"))
        val stateToBeTransformed = getInitialFeedStateModel(lifeSpanInSeconds = 1).copy(feedItems = feedItems, filteredFeedItems = feedItems)
        val expectedState = stateToBeTransformed.copy()

        val actual = searchInput.transform(stateToBeTransformed)

        assertEquals(expectedState, actual)
    }

    @Test
    fun `given FeedState when filtered with a filter with blank spaces string then filtered items match the source items`() {
        val filter = "     "
        val searchInput = FeedInput.SearchInput(filter)
        val feedItem = FeedItem(
            id = "",
            content = "",
            userName = "",
            displayName = "MegaMan",
            avatarUrl = "",
            imageUrl = "",
            linkUrl = "",
            createdDate = LocalDate.now(),
            addedDateInMillis = 10
        )
        val feedItems = listOf(feedItem, feedItem.copy(id = "34", displayName = "Youtuber"))
        val stateToBeTransformed = getInitialFeedStateModel(lifeSpanInSeconds = 1).copy(feedItems = feedItems, filteredFeedItems = feedItems)
        val expectedState = stateToBeTransformed.copy()

        val actual = searchInput.transform(stateToBeTransformed)

        assertEquals(expectedState, actual)
    }

    @Test
    fun `given FeedState when filtered with a filter matching displayName then filtered items should be as expected`() {
        val filter = "man"
        val searchInput = FeedInput.SearchInput(filter)
        val firstFeedItem = FeedItem(
            id = "",
            content = "",
            userName = "",
            displayName = "MegaMan",
            avatarUrl = "",
            imageUrl = "",
            linkUrl = "",
            createdDate = LocalDate.now(),
            addedDateInMillis = 10
        )
        val feedItems = listOf(firstFeedItem, firstFeedItem.copy(id = "34", displayName = "Youtuber"))
        val stateToBeTransformed = getInitialFeedStateModel(lifeSpanInSeconds = 1).copy(feedItems = feedItems, filteredFeedItems = listOf(firstFeedItem))
        val expectedState = stateToBeTransformed.copy(filter = "man")

        val actual = searchInput.transform(stateToBeTransformed)

        assertEquals(expectedState, actual)
    }

    @Test
    fun `given FeedState when filtered with a filter matching displayName of secondItem then filtered items should be as expected`() {
        val filter = "OU"
        val searchInput = FeedInput.SearchInput(filter)
        val firstFeedItem = FeedItem(
            id = "",
            content = "",
            userName = "",
            displayName = "MegaMan",
            avatarUrl = "",
            imageUrl = "",
            linkUrl = "",
            createdDate = LocalDate.now(),
            addedDateInMillis = 10
        )
        val secondFeedItem = firstFeedItem.copy(id = "34", displayName = "Youtuber")
        val feedItems = listOf(firstFeedItem, secondFeedItem)
        val stateToBeTransformed = getInitialFeedStateModel(lifeSpanInSeconds = 1).copy(feedItems = feedItems, filteredFeedItems = listOf(secondFeedItem))
        val expectedState = stateToBeTransformed.copy(filter = "OU")

        val actual = searchInput.transform(stateToBeTransformed)

        assertEquals(expectedState, actual)
    }

    @Test
    fun `given FeedState when filtered with a filter matching both item content then filtered items should be as expected`() {
        val filter = "Capcom"
        val searchInput = FeedInput.SearchInput(filter)
        val firstFeedItem = FeedItem(
            id = "",
            content = "Mega Man is a Japanese science fiction video game franchise created by Capcom, starring a character named \"Mega Man\".",
            userName = "",
            displayName = "MegaMan",
            avatarUrl = "",
            imageUrl = "",
            linkUrl = "",
            createdDate = LocalDate.now(),
            addedDateInMillis = 10
        )
        val secondFeedItem = firstFeedItem.copy(id = "34", displayName = "Youtuber")
        val feedItems = listOf(firstFeedItem, secondFeedItem)
        val stateToBeTransformed = getInitialFeedStateModel(lifeSpanInSeconds = 1).copy(feedItems = feedItems, filteredFeedItems = feedItems)
        val expectedState = stateToBeTransformed.copy(filter = "Capcom")

        val actual = searchInput.transform(stateToBeTransformed)

        assertEquals(expectedState, actual)
    }

    @Test
    fun `given FeedState when filtered with a filter not matching anything then filtered items should be empty`() {
        val filter = "test"
        val searchInput = FeedInput.SearchInput(filter)
        val firstFeedItem = FeedItem(
            id = "",
            content = "",
            userName = "",
            displayName = "MegaMan",
            avatarUrl = "",
            imageUrl = "",
            linkUrl = "",
            createdDate = LocalDate.now(),
            addedDateInMillis = 10
        )
        val secondFeedItem = firstFeedItem.copy(id = "34", displayName = "Youtuber")
        val feedItems = listOf(firstFeedItem, secondFeedItem)
        val stateToBeTransformed = getInitialFeedStateModel(lifeSpanInSeconds = 1).copy(feedItems = feedItems, filteredFeedItems = emptyList())
        val expectedState = stateToBeTransformed.copy(filter = "test")

        val actual = searchInput.transform(stateToBeTransformed)

        assertEquals(expectedState, actual)
    }
}