package io.appmetrica.analytics.impl.profile

import io.appmetrica.analytics.impl.utils.limitation.Trimmer
import io.appmetrica.analytics.impl.utils.validation.Validator
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger

class StringSetUpdatePatcher(
    keyPrefix: String,
    values: List<String>,
    valuesCountLimit: Int,
    valueTrimmer: Trimmer<String>,
    keyValidator: Validator<String>,
    saver: BaseSavingStrategy
) : UserProfileUpdatePatcher {

    private val patchers = values
        .toSet()
        .toList()
        .take(valuesCountLimit)
        .mapIndexed { index, value ->
            StringUpdatePatcher(
                "${keyPrefix}_$index",
                value,
                valueTrimmer,
                keyValidator,
                saver
            )
        }

    override fun apply(userProfileStorage: UserProfileStorage) {
        patchers.forEach { it.apply(userProfileStorage) }
    }

    override fun setPublicLogger(publicLogger: PublicLogger) {
        patchers.forEach { it.setPublicLogger(publicLogger) }
    }
}
