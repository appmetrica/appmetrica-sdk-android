package io.appmetrica.analytics.billingv8.impl.library

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class BillingConnectionControllerTest : CommonTest() {

    private val firstConfig = BillingConfig(41, 42)
    private val secondConfig = BillingConfig(43, 44)
    private val thirdConfig = BillingConfig(45, 46)
    private val firstClient = readyClient()
    private val secondClient = readyClient()
    private val thirdClient = readyClient()
    private val availableClients = ArrayDeque(listOf(firstClient, secondClient, thirdClient))
    private val rounds = mutableListOf<Round>()
    private val workerExecutor: IHandlerExecutor = mock {
        on { post(any()) } doAnswer {
            it.getArgument<Runnable>(0).run()
            null
        }
    }
    private val controller = BillingConnectionController(
        workerExecutor = workerExecutor,
        createClient = availableClients::removeFirst,
        collect = { config, client, holder -> rounds.add(Round(config, client, holder)) },
    )

    @Test
    fun `setup starts collection with the latest configuration`() {
        controller.request(firstConfig)
        controller.request(secondConfig)
        controller.request(thirdConfig)
        assertThat(rounds).isEmpty()

        setupListener(firstClient).onBillingSetupFinished(ok)
        assertThat(rounds.map { it.config }).containsExactly(thirdConfig)
        verify(firstClient, never()).endConnection()

        rounds.single().finish()
        assertThat(rounds.map { it.config }).containsExactly(thirdConfig)
        verify(firstClient).endConnection()
        verify(secondClient, never()).startConnection(any())
    }

    @Test
    fun `failed setup preserves the latest pending request and ignores late setup`() {
        controller.request(firstConfig)
        val oldSetup = setupListener(firstClient)
        controller.request(secondConfig)
        controller.request(thirdConfig)

        oldSetup.onBillingSetupFinished(unavailable)
        val calls = inOrder(firstClient, secondClient)
        calls.verify(firstClient).endConnection()
        calls.verify(secondClient).startConnection(any())
        oldSetup.onBillingSetupFinished(ok)
        assertThat(rounds).isEmpty()

        setupListener(secondClient).onBillingSetupFinished(ok)
        assertThat(rounds.map { it.config }).containsExactly(thirdConfig)
        assertThat(rounds.single().client).isSameAs(secondClient)
    }

    @Test
    fun `successful setup on an unready client closes it before collecting pending work`() {
        whenever(firstClient.isReady).thenReturn(false)
        controller.request(firstConfig)
        controller.request(secondConfig)

        setupListener(firstClient).onBillingSetupFinished(ok)

        val calls = inOrder(firstClient, secondClient)
        calls.verify(firstClient).endConnection()
        calls.verify(secondClient).startConnection(any())
        assertThat(rounds).isEmpty()

        setupListener(secondClient).onBillingSetupFinished(ok)
        assertThat(rounds.single().config).isEqualTo(secondConfig)
        assertThat(rounds.single().client).isSameAs(secondClient)
        rounds.single().finish()
        verify(secondClient).endConnection()
    }

    @Test
    fun `active round keeps only the latest signal and accepts a new signal in the next round`() {
        controller.request(firstConfig)
        setupListener(firstClient).onBillingSetupFinished(ok)
        controller.request(secondConfig)
        controller.request(thirdConfig)
        assertThat(rounds.map { it.config }).containsExactly(firstConfig)

        rounds.last().finish()
        assertThat(rounds.map { it.config }).containsExactly(firstConfig, thirdConfig)
        controller.request(secondConfig)
        assertThat(rounds.map { it.config }).containsExactly(firstConfig, thirdConfig)

        rounds.last().finish()
        assertThat(rounds.map { it.config }).containsExactly(firstConfig, thirdConfig, secondConfig)
        assertThat(rounds.map { it.client }).containsExactly(firstClient, firstClient, firstClient)
        rounds.last().finish()
        verify(firstClient).endConnection()
        verify(secondClient, never()).startConnection(any())
    }

    @Test
    fun `refresh queued before round completion reuses the current client`() {
        val workerTasks = useQueuedWorker()
        controller.request(firstConfig)
        setupListener(firstClient).onBillingSetupFinished(ok)
        workerTasks.removeFirst().run()
        val firstRound = rounds.single()

        workerTasks.addLast { firstRound.finish() }
        workerTasks.addLast { controller.request(secondConfig) }
        workerTasks.removeFirst().run()
        workerTasks.removeFirst().run()

        assertThat(rounds.map { it.config }).containsExactly(firstConfig)
        verify(firstClient, never()).endConnection()

        workerTasks.removeFirst().run()

        assertThat(rounds.map { it.config }).containsExactly(firstConfig, secondConfig)
        assertThat(rounds.last().client).isSameAs(firstClient)
        verify(firstClient, never()).endConnection()
        verify(secondClient, never()).startConnection(any())

        rounds.last().finish()
        workerTasks.removeFirst().run()
        verify(firstClient).endConnection()
    }

    @Test
    fun `stale failed completion after disable leaves the replacement client open`() {
        val workerTasks = useQueuedWorker()
        controller.request(firstConfig)
        setupListener(firstClient).onBillingSetupFinished(ok)
        workerTasks.removeFirst().run()
        val firstRound = rounds.single()
        controller.request(secondConfig)

        workerTasks.addLast {
            firstRound.holder.markFailed()
            firstRound.finish()
        }
        workerTasks.addLast { controller.disable() }
        workerTasks.addLast { controller.request(thirdConfig) }
        workerTasks.removeFirst().run()
        workerTasks.removeFirst().run()

        verify(firstClient).endConnection()
        verify(secondClient, never()).startConnection(any())

        workerTasks.removeFirst().run()
        val replacementSetup = setupListener(secondClient)
        workerTasks.removeFirst().run()

        verify(firstClient).endConnection()
        verify(secondClient, never()).endConnection()
        assertThat(rounds.map { it.config }).containsExactly(firstConfig)

        replacementSetup.onBillingSetupFinished(ok)
        workerTasks.removeFirst().run()
        assertThat(rounds.map { it.config }).containsExactly(firstConfig, thirdConfig)
        assertThat(rounds.last().client).isSameAs(secondClient)

        rounds.last().finish()
        workerTasks.removeFirst().run()
        verify(secondClient).endConnection()
    }

    @Test
    fun `disconnect during collection discards late callbacks and preserves the latest request`() {
        controller.request(firstConfig)
        val firstSetup = setupListener(firstClient)
        firstSetup.onBillingSetupFinished(ok)
        val disconnectedRound = rounds.single()
        controller.request(secondConfig)
        controller.request(thirdConfig)

        firstSetup.onBillingServiceDisconnected()
        val calls = inOrder(firstClient, secondClient)
        calls.verify(firstClient).endConnection()
        calls.verify(secondClient).startConnection(any())
        setupListener(secondClient).onBillingSetupFinished(ok)
        assertThat(rounds.map { it.config }).containsExactly(firstConfig, thirdConfig)

        disconnectedRound.finish()
        assertThat(disconnectedRound.holder.isWaitingFor(disconnectedRound.listener)).isFalse()
        verify(secondClient, never()).endConnection()

        rounds.last().finish()
        verify(secondClient).endConnection()
    }

    @Test
    fun `disable during setup clears pending work and ignores late setup`() {
        controller.request(firstConfig)
        val oldSetup = setupListener(firstClient)
        controller.request(secondConfig)

        controller.disable()
        oldSetup.onBillingSetupFinished(ok)
        assertThat(rounds).isEmpty()
        verify(firstClient).endConnection()
        verify(secondClient, never()).startConnection(any())

        controller.request(thirdConfig)
        oldSetup.onBillingSetupFinished(ok)
        assertThat(rounds).isEmpty()
        setupListener(secondClient).onBillingSetupFinished(ok)
        assertThat(rounds.map { it.config }).containsExactly(thirdConfig)
        rounds.single().finish()
        assertThat(rounds.map { it.config }).containsExactly(thirdConfig)
        verify(secondClient).endConnection()
    }

    @Test
    fun `a new request after collection drains starts a fresh client`() {
        controller.request(firstConfig)
        setupListener(firstClient).onBillingSetupFinished(ok)
        rounds.single().finish()
        verify(firstClient).endConnection()
        verify(secondClient, never()).startConnection(any())

        controller.request(secondConfig)
        val calls = inOrder(firstClient, secondClient)
        calls.verify(firstClient).endConnection()
        calls.verify(secondClient).startConnection(any())
        assertThat(rounds.map { it.config }).containsExactly(firstConfig)

        setupListener(secondClient).onBillingSetupFinished(ok)
        assertThat(rounds.map { it.config }).containsExactly(firstConfig, secondConfig)
        assertThat(rounds.map { it.client }).containsExactly(firstClient, secondClient)
        rounds.last().finish()
        verify(secondClient).endConnection()
    }

    @Test
    fun `close failures allow pending and later requests to start new clients`() {
        doThrow(IllegalStateException("Cannot close first client")).whenever(firstClient).endConnection()
        doThrow(IllegalStateException("Cannot close replacement client")).whenever(secondClient).endConnection()
        controller.request(firstConfig)
        controller.request(secondConfig)
        setupListener(firstClient).onBillingSetupFinished(unavailable)

        val calls = inOrder(firstClient, secondClient, thirdClient)
        calls.verify(firstClient).endConnection()
        calls.verify(secondClient).startConnection(any())
        setupListener(secondClient).onBillingSetupFinished(ok)
        assertThat(rounds.map { it.config }).containsExactly(secondConfig)

        verify(thirdClient, never()).startConnection(any())
        rounds.single().finish()
        calls.verify(secondClient).endConnection()
        verify(thirdClient, never()).startConnection(any())

        controller.request(thirdConfig)
        calls.verify(thirdClient).startConnection(any())
        setupListener(thirdClient).onBillingSetupFinished(ok)
        assertThat(rounds.map { it.config }).containsExactly(secondConfig, thirdConfig)
        rounds.last().finish()
        calls.verify(thirdClient).endConnection()
        verify(firstClient).endConnection()
        verify(secondClient).endConnection()
    }

    private fun useQueuedWorker(): ArrayDeque<Runnable> {
        val tasks = ArrayDeque<Runnable>()
        doAnswer {
            tasks.addLast(it.getArgument(0))
            null
        }.whenever(workerExecutor).post(any())
        return tasks
    }

    private fun setupListener(client: BillingClient): BillingClientStateListener {
        val captor = argumentCaptor<BillingClientStateListener>()
        verify(client).startConnection(captor.capture())
        return captor.firstValue
    }

    private fun readyClient(): BillingClient = mock {
        on { isReady } doReturn true
    }

    private class Round(
        val config: BillingConfig,
        val client: BillingClient,
        val holder: BillingLibraryConnectionHolder,
    ) {
        val listener = Any()

        init {
            holder.addListener(listener)
        }

        fun finish() = holder.removeListener(listener)
    }

    private companion object {
        val ok: BillingResult = BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build()
        val unavailable: BillingResult = BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE)
            .build()
    }
}
