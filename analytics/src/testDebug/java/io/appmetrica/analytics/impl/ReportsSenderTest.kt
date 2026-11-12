package io.appmetrica.analytics.impl

import android.os.Bundle
import android.os.Looper
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.service.commands.ReportToServiceCallable
import io.appmetrica.analytics.impl.service.commands.ServiceCallableFactory
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.ArrayDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class ReportsSenderTest : CommonTest() {

    private val appMetricaConnector: AppMetricaConnector = mock()
    private val executor: ICommonExecutor = mock()
    private val timeProvider: TimeProvider = mock()
    private val crashCallable: ReportToServiceCallable = mock()
    private val reportCallable: ReportToServiceCallable = mock()
    private val typedReportCallable: ReportToServiceCallable = mock()
    private val resumeUserSessionCallable: ReportToServiceCallable = mock()
    private val pauseUserSessionCallable: ReportToServiceCallable = mock()

    private val serviceCallableFactory: ServiceCallableFactory = mock {
        on { createCrashCallable(any(), any()) } doReturn crashCallable
        on { createReportCallable(any()) } doReturn reportCallable
        on { createTypedReportCallable(any(), any()) } doReturn typedReportCallable
        on { createResumeUseSessionCallable(any()) } doReturn resumeUserSessionCallable
        on { createPauseUseSessionCallable(any()) } doReturn pauseUserSessionCallable
    }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private lateinit var budget: ClientReportsQueueBudget
    private lateinit var reportsSender: ReportsSender
    private val postedDrains = ArrayDeque<Runnable>()

    @Before
    fun setUp() {
        whenever(ClientServiceLocator.getInstance().clientExecutorProvider.reportSenderExecutor)
            .thenReturn(executor)
        whenever(timeProvider.uptimeMillis()).thenReturn(0L, 60_000L, 60_000L, 60_000L)

        postedDrains.clear()
        whenever(executor.post(any())).thenAnswer { invocation ->
            postedDrains.addLast(invocation.getArgument(0))
            null
        }

        budget = ClientReportsQueueBudget()
        reportsSender = ReportsSender(
            appMetricaConnector,
            serviceCallableFactory,
            timeProvider,
            budget,
        )
    }

    @Test
    fun queueReportWithEvent() {
        val reportToSend = lightReport(isCrash = false)

        reportsSender.queueReport(reportToSend)

        verify(serviceCallableFactory).createReportCallable(reportToSend)
        verifyNoMoreInteractions(serviceCallableFactory)
        verify(executor).post(any())
        assertThat(budget.currentBytes).isEqualTo(Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES)

        drainAll()
        verify(reportCallable).call()
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun queueReportWithCrashEvent() {
        val reportToSend = lightReport(isCrash = true)

        reportsSender.queueReport(reportToSend)

        verify(serviceCallableFactory).createCrashCallable(reportToSend, true)
        drainAll()
        verify(crashCallable).call()
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun sendCrashIfConnectedAndExecuted() {
        val reportToSend = lightReport(isCrash = true)
        whenever(appMetricaConnector.isConnected).thenReturn(true)
        whenever(crashCallable.isExecuted).thenReturn(true)
        whenever(crashCallable.call()).thenAnswer {
            // Drain path marks executed via real CrashCallable; mock stays true.
            null
        }

        // Auto-run drain so latch completes immediately with isExecuted already true path...
        // Actually latch is countDown in drain finally regardless of isExecuted.
        whenever(executor.post(any())).thenAnswer { invocation ->
            invocation.getArgument<Runnable>(0).run()
            null
        }

        reportsSender.sendCrash(reportToSend)

        verify(serviceCallableFactory).createCrashCallable(reportToSend, false)
        verify(crashCallable).call()
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun sendCrashIfConnectedAndNotExecuted() {
        whenever(appMetricaConnector.isConnected).thenReturn(true)
        whenever(crashCallable.isExecuted).thenReturn(false)
        whenever(executor.post(any())).thenAnswer { invocation ->
            invocation.getArgument<Runnable>(0).run()
            null
        }

        reportsSender.sendCrash(lightReport(isCrash = true))

        // Drain call + sync fallback when isExecuted stays false (matches real non-file CrashCallable).
        verify(crashCallable, times(2)).call()
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun sendCrashIfNotConnectedAndNotExecuted() {
        whenever(appMetricaConnector.isConnected).thenReturn(false)
        whenever(crashCallable.isExecuted).thenReturn(false)

        reportsSender.sendCrash(lightReport(isCrash = true))

        verify(serviceCallableFactory).createCrashCallable(any(), any())
        verify(executor, never()).post(any())
        verify(crashCallable).call()
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun sendCrashTimeoutBeforeStartRunsSyncFallback() {
        whenever(appMetricaConnector.isConnected).thenReturn(true)
        whenever(crashCallable.isExecuted).thenReturn(false)
        // Never run drain → item stays !started → timeout removes + sync call
        whenever(timeProvider.uptimeMillis()).thenReturn(0L, 0L, 60_000L)

        reportsSender.sendCrash(lightReport(isCrash = true))

        verify(crashCallable).call()
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(0)
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun sendCrashTimeoutAfterStartDoesNotSyncFallbackOrRemove() {
        whenever(appMetricaConnector.isConnected).thenReturn(true)
        whenever(crashCallable.isExecuted).thenReturn(false)
        // Remaining wait after timeout → 0.
        whenever(timeProvider.uptimeMillis()).thenReturn(0L, 60_000L, 60_000L)

        val allowFinish = CountDownLatch(1)
        val callStarted = CountDownLatch(1)
        whenever(crashCallable.call()).thenAnswer {
            // Mimic CrashCallable: mark executed while in-flight so sync fallback is skipped.
            whenever(crashCallable.isExecuted).thenReturn(true)
            callStarted.countDown()
            assertThat(allowFinish.await(10, TimeUnit.SECONDS)).isTrue
            null
        }

        val drainThread = Thread {
            val deadline = System.currentTimeMillis() + 2_000
            while (postedDrains.isEmpty() && System.currentTimeMillis() < deadline) {
                Thread.sleep(10)
            }
            postedDrains.removeFirst().run()
        }.also { it.start() }

        reportsSender.sendCrash(lightReport(isCrash = true))

        // Drain already marked started before latch timed out → no second sync call.
        assertThat(callStarted.await(0, TimeUnit.MILLISECONDS)).isTrue
        verify(crashCallable, times(1)).call()
        // Item was polled by drain (in-flight); timeout must not enqueue a duplicate.
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(0)

        allowFinish.countDown()
        drainThread.join(5_000)
        verify(crashCallable, times(1)).call()
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun sendCrashOnCrsThreadRunsInlineWithoutPendingOrLatch() {
        // On a plain JVM test thread Looper.myLooper() is null; matching CRS looper → sync-bypass.
        val crsExecutor: IHandlerExecutor = mock()
        whenever(crsExecutor.looper).thenReturn(Looper.myLooper())
        whenever(ClientServiceLocator.getInstance().clientExecutorProvider.reportSenderExecutor)
            .thenReturn(crsExecutor)

        reportsSender = ReportsSender(
            appMetricaConnector,
            serviceCallableFactory,
            timeProvider,
            budget,
        )
        whenever(appMetricaConnector.isConnected).thenReturn(true)
        whenever(crashCallable.call()).thenAnswer {
            whenever(crashCallable.isExecuted).thenReturn(true)
            null
        }

        reportsSender.sendCrash(lightReport(isCrash = true))

        verify(crashCallable, times(1)).call()
        verify(crsExecutor, never()).post(any())
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(0)
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun reportDataTypedAsDataUsesBudget() {
        val type = InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION.typeId
        val bundle = mock<Bundle>()

        reportsSender.reportData(type, bundle)

        verify(serviceCallableFactory).createTypedReportCallable(type, bundle)
        assertThat(budget.currentBytes).isEqualTo(Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES)
        drainAll()
        verify(typedReportCallable).call()
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun reportDataTypedAsControlDoesNotUseBudget() {
        val type = InternalEvents.EVENT_TYPE_ACTIVATION.typeId
        val bundle = mock<Bundle>()

        reportsSender.reportData(type, bundle)

        assertThat(budget.currentBytes).isEqualTo(0)
        drainAll()
        verify(typedReportCallable).call()
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun queueResumeUserSession() {
        val processConfiguration = mock<ProcessConfiguration>()

        reportsSender.queueResumeUserSession(processConfiguration)

        verify(serviceCallableFactory).createResumeUseSessionCallable(processConfiguration)
        assertThat(budget.currentBytes).isEqualTo(0)
        drainAll()
        verify(resumeUserSessionCallable).call()
    }

    @Test
    fun queuePauseUserSession() {
        val processConfiguration = mock<ProcessConfiguration>()

        reportsSender.queuePauseUserSession(processConfiguration)

        assertThat(budget.currentBytes).isEqualTo(0)
        drainAll()
        verify(pauseUserSessionCallable).call()
    }

    @Test
    fun floodEvictsOldestPendingDataPreservingControlAndOrder() {
        val overhead = Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES
        budget = ClientReportsQueueBudget(maxBytesLimit = 2 * overhead)
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )

        val drained = mutableListOf<String>()
        var dataIndex = 0
        whenever(serviceCallableFactory.createReportCallable(any())).thenAnswer {
            val id = "d${dataIndex++}"
            mock<ReportToServiceCallable> {
                on { call() } doAnswer {
                    drained.add(id)
                    null
                }
            }
        }
        whenever(serviceCallableFactory.createCrashCallable(any(), any())).thenAnswer {
            val id = "crash${dataIndex++}"
            mock<ReportToServiceCallable> {
                on { call() } doAnswer {
                    drained.add(id)
                    null
                }
            }
        }
        var controlIndex = 0
        whenever(serviceCallableFactory.createTypedReportCallable(any(), any())).thenAnswer {
            val id = "c${controlIndex++}"
            mock<ReportToServiceCallable> {
                on { call() } doAnswer {
                    drained.add(id)
                    null
                }
            }
        }

        reportsSender.queueReport(lightReport(isCrash = false)) // d0
        reportsSender.reportData(InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID.typeId, mock()) // c0
        reportsSender.queueReport(lightReport(isCrash = false)) // d1 — budget full
        reportsSender.queueReport(lightReport(isCrash = true)) // crash2 — evicts oldest DATA d0
        reportsSender.reportData(InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA.typeId, mock()) // c1
        reportsSender.queueReport(lightReport(isCrash = false)) // d3 — evicts oldest DATA d1

        assertThat(budget.droppedCount).isEqualTo(2)
        assertThat(budget.currentBytes).isEqualTo(2 * overhead)
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(4)

        drainAll()
        assertThat(drained).containsExactly("c0", "crash2", "c1", "d3")
    }

    @Test
    fun controlAcceptedWhenDataBudgetFull() {
        budget = ClientReportsQueueBudget(
            maxBytesLimit = Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES
        )
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )
        reportsSender.queueReport(lightReport(isCrash = false))
        assertThat(budget.currentBytes).isEqualTo(Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES)

        reportsSender.reportData(InternalEvents.EVENT_TYPE_STARTUP.typeId, mock())
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(2)
        assertThat(budget.currentBytes).isEqualTo(Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES)
    }

    @Test
    fun queueReportControlSurvivesDataFlood() {
        val overhead = Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES
        budget = ClientReportsQueueBudget(maxBytesLimit = overhead)
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )

        val drained = mutableListOf<String>()
        var dataSeq = 0
        whenever(serviceCallableFactory.createReportCallable(any())).thenAnswer { invocation ->
            val report = invocation.getArgument<ReportToSend>(0)
            val label = if (report.report.type == InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID.typeId) {
                "control"
            } else {
                "d${dataSeq++}"
            }
            mock<ReportToServiceCallable> {
                on { call() } doAnswer {
                    drained.add(label)
                    null
                }
            }
        }

        reportsSender.queueReport(
            lightReport(isCrash = false, type = InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID.typeId)
        )
        assertThat(budget.currentBytes).isEqualTo(0)

        repeat(5) {
            reportsSender.queueReport(lightReport(isCrash = false))
        }

        assertThat(budget.droppedCount).isEqualTo(4)
        assertThat(budget.currentBytes).isEqualTo(overhead)
        drainAll()
        assertThat(drained).containsExactly("control", "d4")
    }

    @Test
    fun apiCrashViaQueueReportIsDataAndMayBeEvicted() {
        budget = ClientReportsQueueBudget(
            maxBytesLimit = Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES
        )
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )
        reportsSender.queueReport(lightReport(isCrash = false))
        reportsSender.queueReport(lightReport(isCrash = true))

        // Second DATA evicts the first oldest pending DATA
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(1)
        assertThat(budget.currentBytes).isEqualTo(Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES)
        assertThat(budget.droppedCount).isEqualTo(1)
    }

    @Test
    fun sendCrashAcceptedWhenBudgetFullWithoutTouchingBudget() {
        budget = ClientReportsQueueBudget(
            maxBytesLimit = Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES
        )
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )
        reportsSender.queueReport(lightReport(isCrash = false))
        val dataBytes = budget.currentBytes

        whenever(appMetricaConnector.isConnected).thenReturn(false)
        reportsSender.sendCrash(lightReport(isCrash = true))

        verify(crashCallable).call()
        assertThat(budget.currentBytes).isEqualTo(dataBytes)
    }

    @Test
    fun fatalIsEnqueuedAtFifoTailUnderBacklog() {
        budget = ClientReportsQueueBudget(maxBytesLimit = 20_000)
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )

        val order = mutableListOf<String>()
        whenever(typedReportCallable.call()).doAnswer {
            order.add("control")
            null
        }
        whenever(reportCallable.call()).doAnswer {
            order.add("data")
            null
        }
        whenever(crashCallable.call()).doAnswer {
            order.add("fatal")
            whenever(crashCallable.isExecuted).thenReturn(true)
            null
        }

        reportsSender.reportData(InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID.typeId, mock())
        reportsSender.queueReport(lightReport(isCrash = false))
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(2)

        whenever(appMetricaConnector.isConnected).thenReturn(true)
        // start=0, after latch=5s → remaining wait is 0
        whenever(timeProvider.uptimeMillis()).thenReturn(0L, 5_000L, 5_000L)

        val sendThread = Thread { reportsSender.sendCrash(lightReport(isCrash = true)) }
        sendThread.start()
        val deadline = System.currentTimeMillis() + 2_000
        while (reportsSender.pendingSizeForTests() < 3 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(3)

        drainAll()
        sendThread.join(5_000)
        assertThat(order).containsExactly("control", "data", "fatal")
    }

    @Test
    fun fatalSurvivesDataOverflowAtFifoTail() {
        val overhead = Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES
        budget = ClientReportsQueueBudget(maxBytesLimit = overhead)
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )

        val drained = mutableListOf<String>()
        var dataSeq = 0
        whenever(serviceCallableFactory.createReportCallable(any())).thenAnswer {
            val id = "d${dataSeq++}"
            mock<ReportToServiceCallable> {
                on { call() } doAnswer {
                    drained.add(id)
                    null
                }
            }
        }
        whenever(serviceCallableFactory.createCrashCallable(any(), any())).thenAnswer {
            mock<ReportToServiceCallable> {
                var executed = false
                on { isExecuted } doAnswer { executed }
                on { call() } doAnswer {
                    executed = true
                    drained.add("fatal")
                    null
                }
            }
        }

        reportsSender.queueReport(lightReport(isCrash = false)) // d0 fills budget
        whenever(appMetricaConnector.isConnected).thenReturn(true)
        whenever(timeProvider.uptimeMillis()).thenReturn(0L, 5_000L, 5_000L)

        val sendThread = Thread { reportsSender.sendCrash(lightReport(isCrash = true)) }
        sendThread.start()
        val deadline = System.currentTimeMillis() + 2_000
        while (reportsSender.pendingSizeForTests() < 2 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(2)

        // New DATA evicts oldest DATA (d0); FATAL is not DATA and must stay.
        reportsSender.queueReport(lightReport(isCrash = false)) // d1
        assertThat(budget.droppedCount).isEqualTo(1)
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(2)

        drainAll()
        sendThread.join(5_000)
        assertThat(drained).containsExactly("fatal", "d1")
    }

    @Test
    fun fatalAndControlSurviveDataOverflowAtFifoTail() {
        val overhead = Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES
        budget = ClientReportsQueueBudget(maxBytesLimit = overhead)
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )

        val drained = mutableListOf<String>()
        var dataSeq = 0
        var crashCalls = 0
        whenever(serviceCallableFactory.createReportCallable(any())).thenAnswer {
            val id = "d${dataSeq++}"
            mock<ReportToServiceCallable> {
                on { call() } doAnswer {
                    drained.add(id)
                    null
                }
            }
        }
        whenever(typedReportCallable.call()).doAnswer {
            drained.add("control")
            null
        }
        whenever(serviceCallableFactory.createCrashCallable(any(), any())).thenAnswer {
            mock<ReportToServiceCallable> {
                var executed = false
                on { isExecuted } doAnswer { executed }
                on { call() } doAnswer {
                    crashCalls += 1
                    executed = true
                    drained.add("fatal")
                    null
                }
            }
        }

        // Hold drain: DATA fills budget, CONTROL is not budgeted.
        reportsSender.queueReport(lightReport(isCrash = false)) // d0
        reportsSender.reportData(InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID.typeId, mock())
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(2)
        assertThat(budget.currentBytes).isEqualTo(overhead)

        // Evict oldest DATA; CONTROL stays.
        reportsSender.queueReport(lightReport(isCrash = false)) // d1
        assertThat(budget.droppedCount).isEqualTo(1)
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(2)
        assertThat(budget.currentBytes).isEqualTo(overhead)

        whenever(appMetricaConnector.isConnected).thenReturn(true)
        whenever(timeProvider.uptimeMillis()).thenReturn(0L, 5_000L, 5_000L)

        val sendThread = Thread { reportsSender.sendCrash(lightReport(isCrash = true)) }
        sendThread.start()
        val deadline = System.currentTimeMillis() + 2_000
        while (reportsSender.pendingSizeForTests() < 3 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(3)
        // FATAL has no DATA reservation; budget still equals the surviving DATA item.
        assertThat(budget.currentBytes).isEqualTo(overhead)

        drainAll()
        sendThread.join(5_000)
        assertThat(drained).containsExactly("control", "d1", "fatal")
        assertThat(crashCalls).isEqualTo(1)
        assertThat(budget.currentBytes).isEqualTo(0)
        verify(serviceCallableFactory, times(1)).createCrashCallable(any(), any())
    }

    @Test
    fun rtmUpdateConfigSurvivesDataOverflowAndAllowsLaterEvent() {
        val overhead = Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES
        budget = ClientReportsQueueBudget(maxBytesLimit = overhead)
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )

        val drained = mutableListOf<String>()
        var dataSeq = 0
        whenever(serviceCallableFactory.createReportCallable(any())).thenAnswer {
            val id = "d${dataSeq++}"
            mock<ReportToServiceCallable> {
                on { call() } doAnswer {
                    drained.add(id)
                    null
                }
            }
        }
        whenever(serviceCallableFactory.createTypedReportCallable(any(), any())).thenAnswer { invocation ->
            val bundle = invocation.getArgument<Bundle>(1)
            val action = bundle.getString("action")
            val label = when (action) {
                "io.appmetrica.analytics.rtm.UPDATE_RTM_CONFIGURATION" -> "rtm-config"
                "io.appmetrica.analytics.rtm.REPORT_EVENT" -> "rtm-event"
                else -> "rtm-other"
            }
            mock<ReportToServiceCallable> {
                on { call() } doAnswer {
                    drained.add(label)
                    null
                }
            }
        }

        val configBundle = mock<Bundle> {
            on { getString("action") } doReturn "io.appmetrica.analytics.rtm.UPDATE_RTM_CONFIGURATION"
        }
        val eventBundle = mock<Bundle> {
            on { getString("action") } doReturn "io.appmetrica.analytics.rtm.REPORT_EVENT"
        }

        // UPDATE_CONFIG is CONTROL and must not consume DATA budget.
        reportsSender.reportData(/* TYPE_RTM */ 2, configBundle)
        assertThat(budget.currentBytes).isEqualTo(0)
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(1)

        reportsSender.queueReport(lightReport(isCrash = false)) // d0 fills budget
        assertThat(budget.currentBytes).isEqualTo(overhead)
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(2)

        // New DATA evicts oldest DATA only; RTM config stays.
        reportsSender.queueReport(lightReport(isCrash = false)) // d1
        assertThat(budget.droppedCount).isEqualTo(1)
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(2)
        assertThat(budget.currentBytes).isEqualTo(overhead)

        drainAll()
        assertThat(drained).containsExactly("rtm-config", "d1")
        assertThat(budget.currentBytes).isEqualTo(0)

        // After drain, RTM report events still go through as DATA.
        reportsSender.reportData(/* TYPE_RTM */ 2, eventBundle)
        assertThat(budget.currentBytes).isEqualTo(overhead)
        drainAll()
        assertThat(drained).containsExactly("rtm-config", "d1", "rtm-event")
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun drainContinuesAfterCallableThrows() {
        val drained = mutableListOf<String>()
        val throwingData = mock<ReportToServiceCallable>()
        whenever(throwingData.call()).thenAnswer {
            drained.add("data-throw")
            throw RuntimeException("boom")
        }
        val okData = mock<ReportToServiceCallable>()
        whenever(okData.call()).thenAnswer {
            drained.add("data-ok")
            null
        }
        whenever(typedReportCallable.call()).thenAnswer {
            drained.add("control")
            null
        }
        whenever(serviceCallableFactory.createReportCallable(any()))
            .thenReturn(throwingData, okData)

        reportsSender.queueReport(lightReport(isCrash = false))
        reportsSender.reportData(InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID.typeId, mock())
        reportsSender.queueReport(lightReport(isCrash = false))

        drainAll()
        assertThat(drained).containsExactly("data-throw", "control", "data-ok")
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun reportDataRtmUpdateConfigIsControl() {
        val bundle = mock<Bundle> {
            on {
                getString("action")
            } doReturn "io.appmetrica.analytics.rtm.UPDATE_RTM_CONFIGURATION"
        }

        reportsSender.reportData(/* TYPE_RTM */ 2, bundle)

        assertThat(budget.currentBytes).isEqualTo(0)
        drainAll()
        verify(typedReportCallable).call()
    }

    @Test
    fun reportDataRtmReportEventUsesBudget() {
        val bundle = mock<Bundle> {
            on { getString("action") } doReturn "io.appmetrica.analytics.rtm.REPORT_EVENT"
        }

        reportsSender.reportData(/* TYPE_RTM */ 2, bundle)

        assertThat(budget.currentBytes).isEqualTo(Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES)
        drainAll()
        verify(typedReportCallable).call()
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun doesNotEvictStartedData() {
        budget = ClientReportsQueueBudget(
            maxBytesLimit = Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES
        )
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )

        val headBlock = CountDownLatch(1)
        val headStarted = CountDownLatch(1)
        whenever(reportCallable.call()).thenAnswer {
            headStarted.countDown()
            headBlock.await(5, TimeUnit.SECONDS)
            null
        }

        reportsSender.queueReport(lightReport(isCrash = false))
        val drainThread = Thread { postedDrains.removeFirst().run() }.also { it.start() }
        assertThat(headStarted.await(5, TimeUnit.SECONDS)).isTrue

        // Budget full with started head — new DATA cannot evict it → drop new
        reportsSender.queueReport(lightReport(isCrash = false))
        assertThat(budget.droppedCount).isEqualTo(1)
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(0) // started item already polled

        headBlock.countDown()
        drainThread.join(5_000)
        // After head finishes, drain may post again with empty queue
        drainAll()
        assertThat(budget.currentBytes).isEqualTo(0)
    }

    @Test
    fun oversizedDataEventRejected() {
        budget = ClientReportsQueueBudget(maxBytesLimit = 100)
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )
        reportsSender.queueReport(reportWithValueBytes(ByteArray(200) { 1 }, isCrash = false))

        verify(executor, never()).post(any())
        assertThat(budget.currentBytes).isEqualTo(0)
        assertThat(budget.droppedCount).isEqualTo(1)
    }

    @Test
    fun afterDrainEnqueueIsPossibleAgain() {
        budget = ClientReportsQueueBudget(
            maxBytesLimit = Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES
        )
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )
        reportsSender.queueReport(lightReport(isCrash = false))
        assertThat(budget.currentBytes).isEqualTo(Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES)

        drainAll()
        assertThat(budget.currentBytes).isEqualTo(0)

        reportsSender.queueReport(lightReport(isCrash = false))
        assertThat(budget.currentBytes).isEqualTo(Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES)
    }

    @Test
    fun acceptedEventsPreserveFifoAmongSurvivors() {
        val order = mutableListOf<Int>()
        var next = 0
        val callables = (0 until 3).map { index ->
            mock<ReportToServiceCallable> {
                on { call() } doAnswer {
                    order.add(index)
                    null
                }
            }
        }
        whenever(serviceCallableFactory.createReportCallable(any())).thenAnswer {
            callables[next++]
        }

        repeat(3) { reportsSender.queueReport(lightReport(isCrash = false)) }
        drainAll()
        assertThat(order).containsExactly(0, 1, 2)
    }

    @Test
    fun parallelQueueReportRespectsBudget() {
        budget = ClientReportsQueueBudget(maxBytesLimit = 20_000)
        reportsSender = ReportsSender(
            appMetricaConnector, serviceCallableFactory, timeProvider, budget,
        )

        val threads = (0 until 20).map {
            Thread { reportsSender.queueReport(lightReport(isCrash = false)) }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join(5_000) }

        assertThat(budget.currentBytes).isLessThanOrEqualTo(20_000)
        assertThat(budget.currentBytes % Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES).isEqualTo(0)
        assertThat(reportsSender.pendingSizeForTests()).isEqualTo(
            (budget.currentBytes / Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES).toInt()
        )
    }

    private fun drainAll() {
        while (postedDrains.isNotEmpty()) {
            postedDrains.removeFirst().run()
        }
    }

    private fun lightReport(
        isCrash: Boolean,
        type: Int = InternalEvents.EVENT_TYPE_REGULAR.typeId,
    ): ReportToSend {
        val event = mock<TrimmedCoreClientEvent> {
            on { valueBytes } doReturn null
            on { this.type } doReturn type
        }
        return mock {
            on { isCrashReport } doReturn isCrash
            on { report } doReturn event
        }
    }

    private fun reportWithValueBytes(bytes: ByteArray, isCrash: Boolean): ReportToSend {
        val event = mock<TrimmedCoreClientEvent> {
            on { valueBytes } doReturn bytes
            on { type } doReturn InternalEvents.EVENT_TYPE_REGULAR.typeId
        }
        return mock {
            on { isCrashReport } doReturn isCrash
            on { report } doReturn event
        }
    }
}
