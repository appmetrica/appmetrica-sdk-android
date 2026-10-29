package io.appmetrica.analytics.networktasks.internal.backoff

import io.appmetrica.analytics.networktasks.internal.attempt.AttemptFailureReason
import io.appmetrica.analytics.networktasks.internal.attempt.AttemptOutcome
import io.appmetrica.analytics.networktasks.internal.attempt.RetryState
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PreserveBackoffOnPayloadRejectedTest : CommonTest() {

    @Test
    fun readyToRunDoesNothing() {
        assertThat(PreserveBackoffOnPayloadRejected.backoffAction(AttemptOutcome.ReadyToRun))
            .isEqualTo(BackoffAction.DO_NOTHING)
    }

    @Test
    fun skippedDoesNothing() {
        assertThat(PreserveBackoffOnPayloadRejected.backoffAction(AttemptOutcome.Skipped))
            .isEqualTo(BackoffAction.DO_NOTHING)
    }

    @Test
    fun succeededResetsBackoff() {
        val outcome = AttemptOutcome.Completed(
            succeeded = true,
            retry = RetryState.STOP,
            failureReason = AttemptFailureReason.UNCLASSIFIED,
        )
        assertThat(PreserveBackoffOnPayloadRejected.backoffAction(outcome)).isEqualTo(BackoffAction.RESET)
    }

    @Test
    fun payloadRejectedDoesNothing() {
        val outcome = AttemptOutcome.Completed(
            succeeded = false,
            retry = RetryState.STOP,
            failureReason = AttemptFailureReason.PAYLOAD_REJECTED,
        )
        assertThat(PreserveBackoffOnPayloadRejected.backoffAction(outcome)).isEqualTo(BackoffAction.DO_NOTHING)
    }

    @Test
    fun unclassifiedFailureUpdatesBackoff() {
        val outcome = AttemptOutcome.Completed(
            succeeded = false,
            retry = RetryState.STOP,
            failureReason = AttemptFailureReason.UNCLASSIFIED,
        )
        assertThat(PreserveBackoffOnPayloadRejected.backoffAction(outcome)).isEqualTo(BackoffAction.UPDATE)
    }
}
