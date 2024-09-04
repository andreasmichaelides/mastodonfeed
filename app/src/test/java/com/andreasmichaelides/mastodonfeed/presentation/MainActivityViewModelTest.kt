package com.andreasmichaelides.mastodonfeed.presentation

import app.cash.turbine.test
import com.andreasmichaelides.api.domain.FeedItem
import com.andreasmichaelides.api.domain.GetCurrentTimeInMillisUseCase
import com.andreasmichaelides.api.domain.GetFeedItemsUseCase
import com.andreasmichaelides.logger.domain.MastodonLogger
import com.andreasmichaelides.mastodonfeed.domain.IsConnectedToTheInternetUseCase
import com.andreasmichaelides.mastodonfeed.presentation.mapper.FeedStateToFeedUiModelMapper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModelTest {

    @MockK
    lateinit var getFeedItemsUseCase: GetFeedItemsUseCase

    @MockK
    lateinit var getCurrentTimeInMillisUseCase: GetCurrentTimeInMillisUseCase

    @MockK
    lateinit var isConnectedToTheInternetUseCase: IsConnectedToTheInternetUseCase

    @MockK
    lateinit var feedStateToFeedUiModelMapper: FeedStateToFeedUiModelMapper

    @MockK(relaxed = true)
    lateinit var mastodonLogger: MastodonLogger

    private lateinit var viewModel: MainActivityViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val singleThreadTestDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given viewModel initialised, when reading uiModel then is equal to the initial value`() {
        // Given
        val expected = getInitialFeedUiModel()
        initViewModel()
        // When
        val actual = viewModel.uiModel.value
        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `given items not emitted, when internet connection is available, then items are emitted and update the UiState`() = runTest {
        val feedItem = initialFeedItem()
        val initialFeedState = getInitialFeedStateModel(1)
        val feedStateWithInternetConnection =
            initialFeedState.copy(feedItems = listOf(feedItem), filteredFeedItems = listOf(feedItem), isConnectedToTheInternet = true)
        val firstExpected = getInitialFeedUiModel()
        val secondExpected = getInitialFeedUiModel().copy(uiFeedItems = listOf(getTestUiFeedItem()))
        val internetConnectionFlow = MutableStateFlow(false)

        coEvery { isConnectedToTheInternetUseCase() } returns internetConnectionFlow.asStateFlow()
        coEvery { getFeedItemsUseCase() } returns flowOf(feedItem)
        coEvery { feedStateToFeedUiModelMapper(initialFeedState) } returns firstExpected
        coEvery { feedStateToFeedUiModelMapper(feedStateWithInternetConnection) } returns secondExpected
        coEvery { getCurrentTimeInMillisUseCase() } returns 1

        initViewModel()
        viewModel.uiModel.test {
            internetConnectionFlow.emit(true)
            singleThreadTestDispatcher.scheduler.advanceTimeBy(1)
            assertEquals(firstExpected, awaitItem())
            assertEquals(secondExpected, awaitItem())
        }
    }

    @Test
    fun `given items in State, when items expire, then items are removed`() = runTest {
        val feedItem = initialFeedItem().copy(addedDateInMillis = 0)
        val initialFeedState = getInitialFeedStateModel(1)
        val feedStateWithInternetConnection =
            initialFeedState.copy(feedItems = listOf(feedItem), filteredFeedItems = listOf(feedItem), isConnectedToTheInternet = true)
        val feedStateWithRemovedItem = initialFeedState.copy(isConnectedToTheInternet = true)
        val firstExpected = getInitialFeedUiModel()
        val secondExpected = getInitialFeedUiModel().copy(uiFeedItems = listOf(getTestUiFeedItem()))
        val thirdExpected = getInitialFeedUiModel().copy(uiFeedItems = listOf(getTestUiFeedItem().copy(displayName = "Third item")))

        // Returns true to trigger feed items to be emitted
        coEvery { isConnectedToTheInternetUseCase() } returns flowOf(true)
        coEvery { getFeedItemsUseCase() } returns flowOf(feedItem)
        coEvery { feedStateToFeedUiModelMapper(initialFeedState) } returns firstExpected
        coEvery { feedStateToFeedUiModelMapper(feedStateWithInternetConnection) } returns secondExpected
        coEvery { feedStateToFeedUiModelMapper(feedStateWithRemovedItem) } returns thirdExpected
        coEvery { getCurrentTimeInMillisUseCase() } returns 1001

        initViewModel()
        viewModel.uiModel.test {
            singleThreadTestDispatcher.scheduler.advanceTimeBy(10)
            assertEquals(firstExpected, awaitItem())
            assertEquals(secondExpected, awaitItem())
            singleThreadTestDispatcher.scheduler.advanceTimeBy(991)
            assertEquals(thirdExpected, awaitItem())
        }
    }

    private fun initViewModel() {
        viewModel = MainActivityViewModel(
            coroutineContext = singleThreadTestDispatcher,
            ioDispatcherCoroutineContext = testDispatcher,
            lifeSpanInSeconds = 1,
            expiredFeedsCheckDelayInSecondsDuration = Duration.ofSeconds(1),
            getFeedItemsUseCase = getFeedItemsUseCase,
            getCurrentTimeInMillisUseCase = getCurrentTimeInMillisUseCase,
            isConnectedToTheInternetUseCase = isConnectedToTheInternetUseCase,
            feedStateToFeedUiModelMapper = feedStateToFeedUiModelMapper,
            mastodonLogger = mastodonLogger
        )
    }

    private fun initialFeedItem() = FeedItem(
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

    private fun getTestUiFeedItem() = UiFeedItem(
        displayName = "MegaMan",
        content = mockk(),
        avatarUrl = "",
        userName = ""
    )
}