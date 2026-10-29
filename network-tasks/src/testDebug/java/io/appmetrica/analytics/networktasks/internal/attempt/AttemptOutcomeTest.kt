package io.appmetrica.analytics.networktasks.internal.attempt

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AttemptOutcomeTest : CommonTest() {

    @Test
    fun readyToRun() {
        assertThat(AttemptOutcome.ReadyToRun.retry).isEqualTo(RetryState.RUN)
    }

    @Test
    fun skipped() {
        assertThat(AttemptOutcome.Skipped.retry).isEqualTo(RetryState.STOP)
    }

    @Test
    fun completed() {
        val outcome = AttemptOutcome.Completed(
            succeeded = false,
            retry = RetryState.RUN,
            failureReason = AttemptFailureReason.PAYLOAD_REJECTED,
        )
        assertThat(outcome.succeeded).isFalse
        assertThat(outcome.retry).isEqualTo(RetryState.RUN)
        assertThat(outcome.failureReason).isEqualTo(AttemptFailureReason.PAYLOAD_REJECTED)
    }
}
