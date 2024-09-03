package com.andreasmichaelides.logger.domain

interface MastodonLogger {

    fun logDebug(caller: Any, message: String)

    fun logError(caller: Any, message: String)

    fun logError(caller: Any, throwable: Throwable)

}