package com.andreasmichaelides.mastodonfeed.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreasmichaelides.api.domain.GetFeetItemsUseCase
import com.andreasmichaelides.mastodonfeed.LifeSpanInSecondsLong
import com.andreasmichaelides.mastodonfeed.ViewModelSingleThreadCoroutineContext
import com.andreasmichaelides.api.domain.GetCurrentTimeInMillisUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
    private val getFeedItemsUseCase: GetFeetItemsUseCase,
    private val getCurrentTimeInMillisUseCase: GetCurrentTimeInMillisUseCase
) : ViewModel() {


    private val input = MutableSharedFlow<FeedInput>()
    private val action = MutableSharedFlow<Action>()
    private val feedState = MutableStateFlow(
        FeedState(
            feedItems = emptyList(),
            filteredFeedItems = emptyList(),
            filter = "",
            lifespanInSeconds = lifeSpanInSeconds
        )
    )
    private val uiModelStateFlow = MutableStateFlow(FeedUiModel(emptyList()))
    val uiModel = uiModelStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(coroutineContext) {
                input.map { action ->
                    action to feedState.value
                }.map { it.first.transform(it.second) }
//                .scan(FeedAction.InitialAction to FeedState(emptyList()), object : SuspendFunction2<Pair<FeedAction, FeedState>, Pair<FeedAction, FeedState>, Pair<FeedAction, FeedState>> {
//                    override suspend fun invoke(
//                        previousValue: Pair<FeedAction, FeedState>,
//                        newValue: Pair<FeedAction, FeedState>
//                    ): Pair<FeedAction, FeedState> {
//                        return newValue.first to newValue.first.transform(newValue.second)
//                    }
//                })

                    .collect { updatedUiModel ->
                        feedState.update { updatedUiModel }
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
            withContext(coroutineContext) {
                action.filter { it is Action.LoadItems }
                    .flatMapConcat { getFeedItemsUseCase() }
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
    }

//    override fun onCleared() {
//        super.onCleared()
//        coroutineContext.clo
//    }

    fun loadFeedItemsStream() {
        viewModelScope.launch {
            action.emit(Action.LoadItems)
        }
    }

}