package com.andreasmichaelides.api

import com.andreasmichaelides.api.domain.MastodonRepository
import com.andreasmichaelides.api.data.MastodonRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class MastodonRepositoryModule {

    @Binds
    abstract fun bindMastodonRepository(mastodonRepositoryImpl: MastodonRepositoryImpl): MastodonRepository

}