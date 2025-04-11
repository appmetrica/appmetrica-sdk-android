package io.appmetrica.analytics.impl.crash.ndk.service

import android.content.Context
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrashMetadata
import io.appmetrica.analytics.impl.crash.service.AlwaysAllowSendCrashPredicate
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class NativeCrashHandlerFactoryTest : CommonTest() {

    private val markCrashCompleted: (String) -> Unit = mock()
    private val context: Context = mock()
    private val reportConsumer: ReportConsumer = mock()

    @get:Rule
    val nativeCrashHandlerImplMockedConstructionRule = constructionRule<NativeCrashHandlerImpl>()

    private val pid = 100500
    private val nativeCrashMetadata: AppMetricaNativeCrashMetadata = mock {
        on { processID } doReturn pid
    }
    private val nativeCrash: AppMetricaNativeCrash = mock {
        on { metadata } doReturn nativeCrashMetadata
    }

    @get:Rule
    val nativeCrashFromCurrentSessionPredicateMockedConstructionRule =
        constructionRule<NativeCrashFromCurrentSessionPredicate>()

    @get:Rule
    val alwaysAllowSendCrashPredicateMockedConstructionRule =
        constructionRule<AlwaysAllowSendCrashPredicate<AppMetricaNativeCrash>>()

    private val nativeCrashHandlerFactory: NativeCrashHandlerFactory by setUp {
        NativeCrashHandlerFactory(markCrashCompleted)
    }

    @Test
    fun createHandlerForActualSession() {
        val handler = nativeCrashHandlerFactory.createHandlerForActualSession(context, reportConsumer)
        assertThat(handler)
            .isEqualTo(nativeCrashHandlerImplMockedConstructionRule.constructionMock.constructed().first())
        assertThat(nativeCrashHandlerImplMockedConstructionRule.constructionMock.constructed()).hasSize(1)

        val arguments = nativeCrashHandlerImplMockedConstructionRule.argumentInterceptor.flatArguments()
        SoftAssertions().apply {
            assertThat(arguments[0]).isEqualTo(context)
            assertThat(arguments[1]).isEqualTo(reportConsumer)
            assertThat(arguments[2]).isEqualTo(markCrashCompleted)
            assertThat(arguments[3]).isNotNull() // Check later
            assertThat(arguments[4]).isEqualTo(InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF)
            assertThat(arguments[5]).isEqualTo("actual")
        }
    }

    @Test
    fun `createHandlerForActualSession - check predicate`() {
        nativeCrashHandlerFactory.createHandlerForActualSession(context, reportConsumer)
        val predicateProvider = nativeCrashHandlerImplMockedConstructionRule.argumentInterceptor.flatArguments()[3]
            as NativeShouldSendCrashPredicateProvider
        val predicate = predicateProvider.predicate(nativeCrash)
        assertThat(predicate).isEqualTo(
            nativeCrashFromCurrentSessionPredicateMockedConstructionRule.constructionMock.constructed().first()
        )
        assertThat(nativeCrashFromCurrentSessionPredicateMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(nativeCrashFromCurrentSessionPredicateMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(pid)
    }

    @Test
    fun createHandlerForPrevSession() {
        val handler = nativeCrashHandlerFactory.createHandlerForPrevSession(context, reportConsumer)
        assertThat(handler)
            .isEqualTo(nativeCrashHandlerImplMockedConstructionRule.constructionMock.constructed().first())
        assertThat(nativeCrashHandlerImplMockedConstructionRule.constructionMock.constructed()).hasSize(1)

        val arguments = nativeCrashHandlerImplMockedConstructionRule.argumentInterceptor.flatArguments()
        SoftAssertions().apply {
            assertThat(arguments[0]).isEqualTo(context)
            assertThat(arguments[1]).isEqualTo(reportConsumer)
            assertThat(arguments[2]).isEqualTo(markCrashCompleted)
            assertThat(arguments[3]).isNotNull() // Check later
            assertThat(arguments[4]).isEqualTo(InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF)
            assertThat(arguments[5]).isEqualTo("prev session")
        }
    }

    @Test
    fun `createHandlerForPrevSession - check predicate`() {
        nativeCrashHandlerFactory.createHandlerForPrevSession(context, reportConsumer)
        val predicateProvider = nativeCrashHandlerImplMockedConstructionRule.argumentInterceptor.flatArguments()[3]
            as NativeShouldSendCrashPredicateProvider
        val predicate = predicateProvider.predicate(nativeCrash)
        assertThat(predicate).isEqualTo(
            alwaysAllowSendCrashPredicateMockedConstructionRule.constructionMock.constructed().first()
        )
        assertThat(alwaysAllowSendCrashPredicateMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(alwaysAllowSendCrashPredicateMockedConstructionRule.argumentInterceptor.flatArguments())
            .isEmpty()
    }
}
