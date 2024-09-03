package com.andreasmichaelides.mastodonfeed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreasmichaelides.api.domain.GetCurrentTimeInMillisUseCase
import com.andreasmichaelides.api.domain.GetFeedItemsUseCase
import com.andreasmichaelides.logger.domain.MastodonLogger
import com.andreasmichaelides.mastodonfeed.LifeSpanInSecondsLong
import com.andreasmichaelides.mastodonfeed.ViewModelSingleThreadCoroutineContext
import com.andreasmichaelides.mastodonfeed.domain.IsConnectedToTheInternetUseCase
import com.andreasmichaelides.mastodonfeed.presentation.mapper.FeedStateToFeedUiModelMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    @LifeSpanInSecondsLong lifeSpanInSeconds: Long,
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
                input.map { action -> action to feedState.value }
                    .map { it.first to it.first.transform(it.second) }
                    .collect { updatedUiModel ->
                        feedState.update { updatedUiModel.second }

                        if (updatedUiModel.first is InputWithActions<*, *>) {
                            val inputWithActions = updatedUiModel.first as InputWithActions<*, *>
                            inputWithActions.getActionsExecutedAfterStateUpdate().forEach {
                                action.emit(it)
                            }
                        }
                    }
            }
        }
        viewModelScope.launch {
            withContext(coroutineContext) {
                feedState.map { feedStateToFeedUiModelMapper(it) }
                    .collect { updatedUiModel ->
                    uiModelStateFlow.update { updatedUiModel }
                }
            }
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
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
                                mastodonLogger.logDebug(this@MainActivityViewModel, "GetFeedItemsUseCase will retry: $retryIfIsSocketTimeoutException")
                                retryIfIsSocketTimeoutException
                            }
                            .onCompletion { mastodonLogger.logDebug(this@MainActivityViewModel, "GetFeedItemsUseCase flow completed") }
                    }
                    .collect {
                        input.emit(FeedInput.FeedItemLoadedInput(it))
                    }
            }
        }

        viewModelScope.launch {
            withContext(coroutineContext) {
                flow {
                    while (true) {
                        emit(Unit)
                        delay(Duration.ofSeconds(1))
                    }
                }.collect {
                    input.emit(FeedInput.RemoveExpiredFeedsInput(getCurrentTimeInMillisUseCase()))
                }
            }
        }

        viewModelScope.launch {
            isConnectedToTheInternetUseCase()
                .onEach { mastodonLogger.logDebug(this@MainActivityViewModel, "isConnectedToTheInternetUseCase: $it") }
                .collect { isConnected ->
                    input.emit(FeedInputWithActions.OnInternetConnectionStateChanged(isConnectedToTheInternet = isConnected))
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