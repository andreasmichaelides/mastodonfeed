package com.andreasmichaelides.mastodonfeed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreasmichaelides.api.domain.GetCurrentTimeInMillisUseCase
import com.andreasmichaelides.api.domain.GetFeedItemsUseCase
import com.andreasmichaelides.logger.domain.MastodonLogger
import com.andreasmichaelides.mastodonfeed.ExpiredFeedsCheckDelayInSecondsDuration
import com.andreasmichaelides.mastodonfeed.IoDispatcherCoroutineContext
import com.andreasmichaelides.mastodonfeed.LifeSpanInSecondsLong
import com.andreasmichaelides.mastodonfeed.ViewModelSingleThreadCoroutineContext
import com.andreasmichaelides.mastodonfeed.domain.IsConnectedToTheInternetUseCase
import com.andreasmichaelides.mastodonfeed.presentation.mapper.FeedStateToFeedUiModelMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    @ViewModelSingleThreadCoroutineContext coroutineContext: CoroutineContext,
    @IoDispatcherCoroutineContext ioDispatcherCoroutineContext: CoroutineContext,
    @LifeSpanInSecondsLong lifeSpanInSeconds: Long,
    @ExpiredFeedsCheckDelayInSecondsDuration expiredFeedsCheckDelayInSecondsDuration: Duration,
    private val getFeedItemsUseCase: GetFeedItemsUseCase,
    private val getCurrentTimeInMillisUseCase: GetCurrentTimeInMillisUseCase,
    private val isConnectedToTheInternetUseCase: IsConnectedToTheInternetUseCase,
    private val feedStateToFeedUiModelMapper: FeedStateToFeedUiModelMapper,
    private val mastodonLogger: MastodonLogger,
) : ViewModel() {

    private val input = MutableSharedFlow<Input<FeedState>>()
    private val action = MutableSharedFlow<Action>()
    private val feedState = MutableStateFlow(getInitialFeedState(lifeSpanInSeconds))
    private val uiModelStateFlow = MutableStateFlow(getInitialFeedState())
    val uiModel: StateFlow<FeedUiModel> = uiModelStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(coroutineContext) {
                input.collect { input ->
                    mastodonLogger.logDebug(this@MainActivityViewModel, Thread.currentThread().name)
                    // Updating the state with the newly transformed one
                    feedState.update { input.transform(it) }

                    // Executing any actions, that are the outcome of the Input, after the State has been updated
                    if (input is InputWithActions<*, *>) {
                        val inputWithActions = input as InputWithActions<*, *>
                        inputWithActions.getActionsExecutedAfterStateUpdate().forEach {
                            action.emit(it)
                        }
                    }
                }
            }
        }

        // Whenever the viewModel state is updated, then the UiModel will update too
        viewModelScope.launch {
            withContext(coroutineContext) {
                feedState.map { feedStateToFeedUiModelMapper(it) }
                    .collect { updatedUiModel ->
                        uiModelStateFlow.update { updatedUiModel }
                    }
            }
        }

        // The getFeedItemsUseCase can run on a separate thread, to perform its network task. The result will be handled
        // by the input on the viewModel flow
        viewModelScope.launch {
            withContext(ioDispatcherCoroutineContext) {
                action.filter { it is Action.LoadItems }
                    .flatMapLatest {
                        getFeedItemsUseCase()
                            // The implementation of the library I'm using to fetch the Mastodon feeds, after a couple of disconnects form the internet,
                            // it freezes and then throws an exception. I am handling this case here, where the flow is re-subscribed and the
                            // library reconnects and starts emitting new items again. In any case, it is a good practise to catch any
                            // exceptions in our flows, so we log and handle unforeseen cases in our apps
                            .retryWhen { cause, _ ->
                                mastodonLogger.logError(this@MainActivityViewModel, "GetFeedItemsUseCase error: $cause")
                                val retryIfIsSocketTimeoutException = cause is IllegalStateException
                                mastodonLogger.logDebug(
                                    this@MainActivityViewModel,
                                    "GetFeedItemsUseCase will retry: $retryIfIsSocketTimeoutException"
                                )
                                retryIfIsSocketTimeoutException
                            }
                            .onCompletion { mastodonLogger.logDebug(this@MainActivityViewModel, "GetFeedItemsUseCase flow completed") }
                    }
                    .collect {
                        input.emit(FeedInput.FeedItemLoadedInput(it))
                    }
            }
        }

        // A very basic flow that emits depending on the duration provided, to remove the expired Feeds
        viewModelScope.launch {
            withContext(coroutineContext) {
                flow {
                    while (true) {
                        emit(Unit)
                        delay(expiredFeedsCheckDelayInSecondsDuration)
                    }
                }.collect {
                    input.emit(FeedInput.RemoveExpiredFeedsInput(getCurrentTimeInMillisUseCase()))
                }
            }
        }

        viewModelScope.launch {
            withContext(coroutineContext) {
                isConnectedToTheInternetUseCase()
                    .onEach { mastodonLogger.logDebug(this@MainActivityViewModel, "isConnectedToTheInternetUseCase: $it") }
                    .collect { isConnected ->
                        input.emit(FeedInputWithActions.OnInternetConnectionStateChanged(isConnectedToTheInternet = isConnected))
                    }
            }
        }
    }

    fun onSearch(filter: String) {
        viewModelScope.launch {
            withContext(coroutineContext) {
                input.emit(FeedInput.SearchInput(filter))
            }
        }
    }
}