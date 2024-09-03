package com.andreasmichaelides.logger.data

import android.util.Log
import com.andreasmichaelides.logger.domain.MastodonLogger
import javax.inject.Inject

class MastodonLoggerImpl @Inject constructor() : MastodonLogger {

    override fun logDebug(caller: Any, message: String) {
        Log.d(caller.javaClass.simpleName, message)
    }

    override fun logError(caller: Any, message: String) {
        Log.e(caller.javaClass.simpleName, message)
    }

    override fun logError(caller: Any, throwable: Throwable) {
        Log.e(caller.javaClass.simpleName, null, throwable)
    }
}