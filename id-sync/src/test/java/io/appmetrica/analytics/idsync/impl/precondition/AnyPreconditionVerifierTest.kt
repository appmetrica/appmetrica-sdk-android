package io.appmetrica.analytics.idsync.impl.precondition

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class AnyPreconditionVerifierTest : CommonTest() {

    private val verifier by setUp { AnyPreconditionVerifier() }

    @Test
    fun `verify returns true`() {
        assertThat(verifier.matchPrecondition()).isTrue()
    }
}
