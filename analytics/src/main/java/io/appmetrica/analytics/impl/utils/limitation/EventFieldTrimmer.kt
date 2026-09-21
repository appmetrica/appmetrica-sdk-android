package io.appmetrica.analytics.impl.utils.limitation

import io.appmetrica.analytics.coreutils.internal.limitation.BytesTrimmer
import io.appmetrica.analytics.coreutils.internal.limitation.DummyTrimmer
import io.appmetrica.analytics.coreutils.internal.limitation.StringTrimmer
import io.appmetrica.analytics.coreutils.internal.limitation.Trimmer
import io.appmetrica.analytics.impl.EventTrimPolicy
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger

internal object EventFieldTrimmer {

    fun nameTrimmer(logger: PublicLogger, policy: EventTrimPolicy): Trimmer<String> {
        return when (policy) {
            EventTrimPolicy.NONE -> DummyTrimmer()
            EventTrimPolicy.STANDARD,
            EventTrimPolicy.EXTENDED -> StringTrimmer(
                EventLimitationProcessor.EVENT_NAME_MAX_LENGTH,
                "event name",
                logger
            )
        }
    }

    fun valueBytesTrimmer(logger: PublicLogger, policy: EventTrimPolicy): Trimmer<ByteArray> {
        return when (policy) {
            EventTrimPolicy.NONE -> DummyTrimmer()
            EventTrimPolicy.EXTENDED -> BytesTrimmer(
                EventLimitationProcessor.REPORT_EXTENDED_VALUE_MAX_SIZE,
                "event extended value",
                logger
            )
            EventTrimPolicy.STANDARD -> BytesTrimmer(
                EventLimitationProcessor.REPORT_VALUE_MAX_SIZE,
                "event value bytes",
                logger
            )
        }
    }

    fun profileIdTrimmer(logger: PublicLogger, policy: EventTrimPolicy): Trimmer<String> {
        return when (policy) {
            EventTrimPolicy.NONE -> DummyTrimmer()
            EventTrimPolicy.STANDARD,
            EventTrimPolicy.EXTENDED -> StringTrimmer(
                EventLimitationProcessor.USER_PROFILE_ID_MAX_LENGTH,
                "user profile id",
                logger
            )
        }
    }
}
