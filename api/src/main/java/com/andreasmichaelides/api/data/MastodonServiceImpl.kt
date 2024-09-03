package com.andreasmichaelides.api.data

import com.andreasmichaelides.api.MastodonAccessTokenString
import com.andreasmichaelides.api.MastodonInstanceNameString
import com.andreasmichaelides.api.domain.StatusItem
import com.andreasmichaelides.api.domain.mapper.StatusToFeedItemMapper
import com.andreasmichaelides.logger.domain.MastodonLogger
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
    private val mastodonLogger: MastodonLogger,
) : MastodonService {

    override fun getPublicStream(): Flow<StatusItem> {

        // Using callbackFlow, to wrap the callback from the library and handle its state, when the flow is completed
        return callbackFlow {
            try {
                val client = MastodonClient.Builder(instanceName)
                    .accessToken(accessToken)
                    .build()

                val closeable = client.streaming.federatedPublic(
                    onlyMedia = false,
                    callback = {
                        when (it) {
                            is TechnicalEvent -> {
                                mastodonLogger.logDebug(this@MastodonServiceImpl, "TechnicalEvent: $it")
                                when (it) {
                                    is TechnicalEvent.Closed,
                                    is TechnicalEvent.Failure -> close()
                                    else -> Unit
                                }
                            }

                            is MastodonApiEvent -> when (it) {
                                is MastodonApiEvent.GenericMessage -> mastodonLogger.logDebug(this@MastodonServiceImpl, "Generic message: $it")
                                is MastodonApiEvent.StreamEvent -> {
                                    when (it.event) {
                                        is ParsedStreamEvent.StatusCreated -> {
                                            val feedItem =
                                                statusToFeedItemMapper((it.event as ParsedStreamEvent.StatusCreated).createdStatus)
                                            trySend(feedItem)
                                        }

                                        else -> {
                                            mastodonLogger.logDebug(this@MastodonServiceImpl, "Received other event: ${it.event}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                awaitClose {
                    closeable.close()
                    mastodonLogger.logDebug(this@MastodonServiceImpl, "Closed")
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                mastodonLogger.logError(this@MastodonServiceImpl, exception)
                println("Exception event: $exception")
                error(exception)
            }
        }
    }
}