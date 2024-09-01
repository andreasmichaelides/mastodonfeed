package com.andreasmichaelides.api.data

import com.andreasmichaelides.api.MastodonAccessTokenString
import com.andreasmichaelides.api.MastodonInstanceNameString
import com.andreasmichaelides.api.domain.StatusItem
import com.andreasmichaelides.api.domain.mapper.StatusToFeedItemMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import social.bigbone.MastodonClient
import social.bigbone.api.entity.streaming.MastodonApiEvent
import social.bigbone.api.entity.streaming.ParsedStreamEvent
import social.bigbone.api.entity.streaming.TechnicalEvent
import javax.inject.Inject

class MastodonServiceImpl @Inject constructor(
    @MastodonInstanceNameString private val instanceName: String,
    @MastodonAccessTokenString private val accessToken: String,
    private val statusToFeedItemMapper: StatusToFeedItemMapper,
    private val scope: CoroutineScope,
) : MastodonService {

    override fun getPublicStream(): Flow<StatusItem> {
        val stateFlow = MutableSharedFlow<StatusItem>()

        val client = MastodonClient.Builder(instanceName)
            .accessToken(accessToken)
            .build()

        client.streaming.federatedPublic(
            onlyMedia = false,
            callback = {
                when (it) {
                    is TechnicalEvent -> println("Technical event: $it")
                    is MastodonApiEvent -> when (it) {
                        is MastodonApiEvent.GenericMessage -> println("GenericMessage: $it")
                        is MastodonApiEvent.StreamEvent -> {
                            when (it.event) {
                                is ParsedStreamEvent.StatusCreated -> {
                                    val feedItem =
                                        statusToFeedItemMapper((it.event as ParsedStreamEvent.StatusCreated).createdStatus)
                                    scope.launch {
                                        stateFlow.emit(feedItem)
                                    }
                                }

                                else -> {
                                    // TODO log
                                }
                            }
                        }
                    }
                }
            }
        )
        return stateFlow
    }
}