package com.andreasmichaelides.api.domain

import social.bigbone.MastodonClient
import social.bigbone.api.entity.streaming.MastodonApiEvent
import social.bigbone.api.entity.streaming.TechnicalEvent

class MastodonRepositoryImpl : MastodonRepository {

    override suspend fun streamFeedItems() {
        // Instantiate client
        val client = MastodonClient.Builder("mas.to")
            .accessToken("FAXrj30FBUM99tCr2LXo63TKZR6M_RFHFiSN2x8H9jM")
            .build()


        client.streaming.federatedPublic(
            onlyMedia = false,
            callback = {
                when (it) {
                    is TechnicalEvent -> println("Technical event: $it")
                    is MastodonApiEvent -> when (it) {
                        is MastodonApiEvent.GenericMessage -> println("GenericMessage: $it")
                        is MastodonApiEvent.StreamEvent -> println("StreamEvent: $it")
                    }
                    TechnicalEvent.Open -> println("Technical event: $it")
                }
            }
        )
    }
}