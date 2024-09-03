package com.andreasmichaelides.mastodonfeed.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreasmichaelides.api.domain.GetCurrentTimeInMillisUseCase
import com.andreasmichaelides.api.domain.GetFeedItemsUseCase
import com.andreasmichaelides.mastodonfeed.LifeSpanInSecondsLong
import com.andreasmichaelides.mastodonfeed.ViewModelSingleThreadCoroutineContext
import com.andreasmichaelides.mastodonfeed.domain.IsConnectedToTheInternetUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModel @Inject constructor(
    @ViewModelSingleThreadCoroutineContext coroutineContext: CoroutineContext,
    @LifeSpanInSecondsLong lifeSpanInSeconds: Long,
    private val getFeedItemsUseCase: GetFeedItemsUseCase,
    private val getCurrentTimeInMillisUseCase: GetCurrentTimeInMillisUseCase,
    private val isConnectedToTheInternetUseCase: IsConnectedToTheInternetUseCase,
) : ViewModel() {

    private val input = MutableSharedFlow<Input<FeedState>>()
    private val action = MutableSharedFlow<Action>()
    private val feedState = MutableStateFlow(
        FeedState(
            feedItems = emptyList(),
            filteredFeedItems = emptyList(),
            filter = "",
            lifespanInSeconds = lifeSpanInSeconds,
            isConnectedToTheInternet = false
        )
    )
    private val uiModelStateFlow = MutableStateFlow(FeedUiModel(emptyList()))
    val uiModel = uiModelStateFlow.asStateFlow()

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
                feedState.map {
                    // TODO map ModelState to UiState
                    FeedUiModel(it.feedItems)
                }.collect {
                    Log.d("Pafto", "Item: ${it.uiFeedItems.size}")
                }
            }
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                action.filter { it is Action.LoadItems }
                    .flatMapLatest {
                        getFeedItemsUseCase()
                            .retryWhen { cause, _ ->
                                val retryIfSocketTimeoutException = cause is IllegalStateException
                                retryIfSocketTimeoutException
                            }
                            .onCompletion { Log.d("Pafto", "onCompletion") }
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
                .collect { isConnected ->
                    input.emit(FeedInputWithActions.OnInternetConnectionStateChanged(isConnectedToTheInternet = isConnected))
                }
        }
    }
}