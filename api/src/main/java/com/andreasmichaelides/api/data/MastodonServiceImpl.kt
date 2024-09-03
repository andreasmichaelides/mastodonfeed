package com.andreasmichaelides.api.data

import com.andreasmichaelides.api.MastodonAccessTokenString
import com.andreasmichaelides.api.MastodonInstanceNameString
import com.andreasmichaelides.api.domain.StatusItem
import com.andreasmichaelides.api.domain.mapper.StatusToFeedItemMapper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import social.bigbone.MastodonClient
import social.bigbone.api.entity.streaming.MastodonApiEvent
import social.bigbone.api.entity.streaming.ParsedStreamEvent
import social.bigbone.api.entity.streaming.TechnicalEvent
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class MastodonServiceImpl @Inject constructor(
    @MastodonInstanceNameString private val instanceName: String,
    @MastodonAccessTokenString private val accessToken: String,
    private val statusToFeedItemMapper: StatusToFeedItemMapper,
) : MastodonService {

    override fun getPublicStream(): Flow<StatusItem> {
        return callbackFlow {
            try {
                val client = MastodonClient.Builder(instanceName)
                    .accessToken(accessToken)
                    .build()

                val closeable = client.streaming.federatedPublic(
                    onlyMedia = false,
                    callback = {
                        when (it) {
                            is TechnicalEvent -> when (it) {
                                is TechnicalEvent.Closed -> close()
                                is TechnicalEvent.Closing -> {}
                                is TechnicalEvent.Failure -> close()
                                TechnicalEvent.Open -> {}
                            }

                            is MastodonApiEvent -> when (it) {
                                is MastodonApiEvent.GenericMessage -> println("GenericMessage: $it")
                                is MastodonApiEvent.StreamEvent -> {
                                    when (it.event) {
                                        is ParsedStreamEvent.StatusCreated -> {
                                            val feedItem =
                                                statusToFeedItemMapper((it.event as ParsedStreamEvent.StatusCreated).createdStatus)
                                            trySend(feedItem)
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

                awaitClose { closeable.close() }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                println("Exception event: $exception")
                error(exception)
            }
        }
    }
}