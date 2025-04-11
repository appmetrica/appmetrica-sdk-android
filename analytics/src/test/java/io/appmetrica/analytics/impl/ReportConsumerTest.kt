package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.clients.ClientDescription
import io.appmetrica.analytics.impl.component.clients.ClientRepository
import io.appmetrica.analytics.impl.component.clients.ClientUnit
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ReportConsumerTest : CommonTest() {

    private val context: Context = mock()

    private val packageName = "some"
    private val processId = 123
    private val processSessionID = "Some session id"

    private val clientDescription: ClientDescription = mock {
        on { packageName } doReturn packageName
        on { processID } doReturn processId
        on { processSessionID } doReturn processSessionID
    }
    private val commonArguments: CommonArguments = mock()
    private val clientUnit: ClientUnit = mock()

    private val clientRepository: ClientRepository = mock {
        on { getOrCreateClient(clientDescription, commonArguments) } doReturn clientUnit
    }
    private val counterReport: CounterReport = mock()
    private val extras: Bundle = mock()
    private val reportExecutor: IHandlerExecutor = mock()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val reportRunnableMockedConstructionRule = constructionRule<ReportRunnable>()

    private lateinit var reportConsumer: ReportConsumer

    @Before
    fun setUp() {
        whenever(GlobalServiceLocator.getInstance().serviceExecutorProvider.reportRunnableExecutor)
            .thenReturn(reportExecutor)
        reportConsumer = ReportConsumer(context, clientRepository)
    }

    @Test
    fun `consumeReport if non undefined type`() {
        reportConsumer.consumeReport(counterReport, extras)
        verify(reportExecutor).execute(reportRunnableMockedConstructionRule.constructionMock.constructed().first())
        assertThat(reportRunnableMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(reportRunnableMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, counterReport, extras, clientRepository)
    }

    @Test
    fun `consumeReport if undefined type`() {
        whenever(counterReport.isUndefinedType).thenReturn(true)
        reportConsumer.consumeReport(counterReport, extras)
        verifyNoInteractions(reportExecutor)
        assertThat(reportRunnableMockedConstructionRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun consumeCrash() {
        reportConsumer.consumeCrash(clientDescription, counterReport, commonArguments)
        verify(clientUnit).handle(counterReport, commonArguments)
        verify(clientRepository).remove(packageName, processId, processSessionID)
    }
}
