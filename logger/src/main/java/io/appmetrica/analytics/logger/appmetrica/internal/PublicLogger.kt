package io.appmetrica.analytics.logger.appmetrica.internal

import android.content.Context
import io.appmetrica.analytics.logger.common.BaseReleaseLogger

class PublicLogger(partialApiKey: String) : BaseReleaseLogger("AppMetrica", "[$partialApiKey]") {

    private constructor() : this("")

    companion object {
        private val ANONYMOUS_INSTANCE: PublicLogger = PublicLogger()

        @JvmStatic
        fun getAnonymousInstance() = ANONYMOUS_INSTANCE
        fun init(context: Context) {
            BaseReleaseLogger.init(context)
        }
    }
}
