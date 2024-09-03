package com.andreasmichaelides.logger

import com.andreasmichaelides.logger.data.MastodonLoggerImpl
import com.andreasmichaelides.logger.domain.MastodonLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class LoggerModule {

    @Binds
    abstract fun bindLogger(mastodonLoggerImpl: MastodonLoggerImpl): MastodonLogger

}