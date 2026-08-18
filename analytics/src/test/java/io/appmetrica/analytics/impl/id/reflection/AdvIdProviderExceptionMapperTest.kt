package io.appmetrica.analytics.impl.id.reflection

import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdProviderException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceAccessDeniedException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceBindingException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceCommunicationException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceConnectionTimeoutException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceNotFoundException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceResponseException
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.lang.reflect.InvocationTargetException

@RunWith(Parameterized::class)
internal class AdvIdProviderExceptionMapperTest(
    private val throwable: Throwable,
    private val expectedStatus: IdentifierStatus,
    private val expectedDetails: String,
    private val expectedRetryable: Boolean,
) : CommonTest() {

    @Test
    fun `maps exception to expected failure`() {
        val failure = AdvIdProviderExceptionMapper.map(throwable)

        ObjectPropertyAssertions(failure.result)
            .checkField("mAdTrackingInfo", null as AdTrackingInfo?)
            .checkField("mStatus", expectedStatus)
            .checkField("mErrorExplanation", expectedDetails)
            .checkAll()
        assertThat(failure.isRetryable).isEqualTo(expectedRetryable)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{2}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    ClassNotFoundException(),
                    IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                    "identifier provider module not found",
                    false
                ),
                arrayOf(
                    AdvIdServiceNotFoundException(""),
                    IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                    "service not found",
                    false
                ),
                arrayOf(
                    AdvIdServiceBindingException(""),
                    IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                    "failed to bind service",
                    true
                ),
                arrayOf(
                    AdvIdServiceConnectionTimeoutException(""),
                    IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                    "service connection timed out",
                    true
                ),
                arrayOf(
                    AdvIdServiceCommunicationException(""),
                    IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                    "service communication failed",
                    true
                ),
                arrayOf(
                    AdvIdServiceAccessDeniedException(""),
                    IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                    "service access denied",
                    false
                ),
                arrayOf(
                    AdvIdServiceResponseException(""),
                    IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                    "service returned invalid binder",
                    false
                ),
                arrayOf(
                    object : AdvIdProviderException("") {},
                    IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                    "identifier provider failed",
                    false
                ),
                arrayOf(
                    IllegalStateException(),
                    IdentifierStatus.UNKNOWN,
                    "identifier provider invocation failed (IllegalStateException)",
                    false
                ),
                arrayOf(
                    InvocationTargetException(AdvIdServiceCommunicationException("")),
                    IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                    "service communication failed",
                    true
                )
            )
        }
    }
}
