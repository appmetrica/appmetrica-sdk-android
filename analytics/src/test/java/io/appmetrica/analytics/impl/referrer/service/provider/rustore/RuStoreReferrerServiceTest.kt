package io.appmetrica.analytics.impl.referrer.service.provider.rustore

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.os.IBinder
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.GetInstallReferrerCallback
import io.appmetrica.analytics.impl.referrer.service.provider.rustore.aidl.InstallReferrerProvider
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class RuStoreReferrerServiceTest : CommonTest() {

    private val ruStorePackageName = "ru.vk.store"
    private val ruStoreServiceAction = "ru.vk.store.sdk.install.referrer.InstallReferrerProvider"
    private val ruStoreProviderDescriptor = "ru.vk.store.sdk.install.referrer.InstallReferrerProvider"
    private val appPackageName = "io.appmetrica.test.app"

    private val packageManager: PackageManager = mock()
    private val context: Context = mock {
        on { packageManager } doReturn packageManager
        on { packageName } doReturn appPackageName
    }
    private val listener: ReferrerListener = mock()
    private val aidlService: InstallReferrerProvider = mock()

    // Binder that returns our service mock via queryLocalInterface — bypasses Stub.asInterface proxy creation
    private val binder: IBinder = mock {
        on { queryLocalInterface(ruStoreProviderDescriptor) } doReturn aidlService
    }

    @get:Rule
    val intentRule = MockedConstructionRule(Intent::class.java)

    @get:Rule
    val componentNameRule = MockedConstructionRule(ComponentName::class.java)

    private val service by setUp { RuStoreReferrerService(context) }

    // region findRuStoreComponent

    @Test
    fun `requestReferrer fails when queryIntentServices returns empty list`() {
        whenever(packageManager.queryIntentServices(any(), any<Int>())) doReturn emptyList()

        service.requestReferrer(listener)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Failure::class.java)
        assertThat((resultCaptor.firstValue as ReferrerResult.Failure).message)
            .isEqualTo("RuStore service component not found")
        verify(context, never()).bindService(any(), any(), any<Int>())
    }

    @Test
    fun `requestReferrer binds with correct intent and component when rustore component found`() {
        val ruStoreServiceName = "ru.vk.store.sdk.ReferrerService"
        val ruStoreResolveInfo = makeResolveInfo(ruStorePackageName, ruStoreServiceName)
        whenever(packageManager.queryIntentServices(any(), any<Int>())) doReturn listOf(ruStoreResolveInfo)
        whenever(context.bindService(any(), any(), any<Int>())) doReturn true

        service.requestReferrer(listener)

        // Intent is constructed twice: once in findRuStoreComponent (for querying), once for binding
        val allConstructed = intentRule.constructionMock.constructed()
        assertThat(allConstructed).hasSize(2)
        // Both Intents are created with ruStoreServiceAction
        assertThat(intentRule.argumentInterceptor.flatArguments())
            .containsExactly(ruStoreServiceAction, ruStoreServiceAction)
        // ComponentName is constructed with correct package and class
        assertThat(componentNameRule.argumentInterceptor.flatArguments())
            .containsExactly(ruStorePackageName, ruStoreServiceName)
        // The binding Intent gets setComponent called
        verify(allConstructed[1]).component = any()
        verify(listener, never()).onResult(any())
    }

    @Test
    fun `requestReferrer fails when queryIntentServices throws exception`() {
        whenever(packageManager.queryIntentServices(any(), any<Int>())) doThrow RuntimeException("pm error")

        service.requestReferrer(listener)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        assertThat((resultCaptor.firstValue as ReferrerResult.Failure).message)
            .isEqualTo("RuStore service component not found")
        verify(context, never()).bindService(any(), any(), any<Int>())
    }

    // endregion

    // region bindService

    @Test
    fun `requestReferrer fails when bindService throws exception`() {
        val exception = RuntimeException("bind error")
        setupRuStoreComponent()
        whenever(context.bindService(any(), any(), any<Int>())) doThrow exception

        service.requestReferrer(listener)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to bind RuStore service")
        assertThat(failure.throwable).isEqualTo(exception)
    }

    @Test
    fun `requestReferrer fails when bindService returns false`() {
        setupRuStoreComponent()
        whenever(context.bindService(any(), any(), any<Int>())) doReturn false

        service.requestReferrer(listener)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).startsWith("bindService returned false for RuStore service")
    }

    // endregion

    // region onServiceConnected

    @Test
    fun `onServiceConnected fails and unbinds when asInterface returns null`() {
        // A binder that returns null from queryLocalInterface causes Stub.asInterface to return a proxy,
        // but we pass null binder directly so asInterface returns null
        val connection = bindAndGetConnection()

        connection.onServiceConnected(mock(), null)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        assertThat((resultCaptor.firstValue as ReferrerResult.Failure).message)
            .isEqualTo("RuStore service binder is null")
        verify(context).unbindService(connection)
    }

    @Test
    fun `onServiceConnected calls getInstallReferrer with correct package name`() {
        val connection = bindAndGetConnection()

        connection.onServiceConnected(mock(), binder)

        verify(aidlService).getInstallReferrer(eq(appPackageName), any())
        verify(listener, never()).onResult(any())
    }

    @Test
    fun `onServiceConnected fails and unbinds when getInstallReferrer throws exception`() {
        val exception = RuntimeException("remote error")
        whenever(aidlService.getInstallReferrer(any(), any())) doThrow exception
        val connection = bindAndGetConnection()

        connection.onServiceConnected(mock(), binder)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to referrer from RuStore service")
        assertThat(failure.throwable).isEqualTo(exception)
        verify(context).unbindService(connection)
    }

    // endregion

    // region GetInstallReferrerCallback

    @Test
    fun `onSuccess returns Failure for null or blank payload`() {
        for (payload in listOf(null, "", "   ")) {
            val listener: ReferrerListener = mock()
            val callback = connectAndGetCallback(listener)
            callback.onSuccess(payload)
            val resultCaptor = argumentCaptor<ReferrerResult>()
            verify(listener).onResult(resultCaptor.capture())
            assertThat((resultCaptor.firstValue as ReferrerResult.Failure).message)
                .isEqualTo("RuStore referrer payload is empty")
        }
    }

    @Test
    fun `onSuccess returns Failure when referrer id is empty`() {
        val callback = connectAndGetCallback(listener)

        callback.onSuccess(buildPayload(referrerId = "", clickTs = 1000L, installTs = 2000L))

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        assertThat((resultCaptor.firstValue as ReferrerResult.Failure).message)
            .isEqualTo("RuStore referrer id is empty")
    }

    @Test
    fun `onSuccess unbinds and returns Success with correct ReferrerInfo for valid payload`() {
        val referrerId = "utm_source=rustore"
        val clickTimestampMs = 1_700_000_000_000L
        val installTimestampMs = 1_700_000_001_000L
        val connection = bindAndGetConnection()
        connection.onServiceConnected(mock(), binder)
        val callback = captureCallback()

        callback.onSuccess(buildPayload(referrerId, clickTimestampMs, installTimestampMs))

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        val success = resultCaptor.firstValue as ReferrerResult.Success
        assertThat(success.referrerInfo).isEqualTo(
            ReferrerInfo(referrerId, clickTimestampMs / 1000, installTimestampMs / 1000, ReferrerInfo.Source.RS)
        )
        verify(context).unbindService(connection)
    }

    @Test
    fun `onSuccess unbinds and returns Success with ReferrerInfo without times`() {
        val referrerId = "utm_source=rustore"
        val connection = bindAndGetConnection()
        connection.onServiceConnected(mock(), binder)
        val callback = captureCallback()

        callback.onSuccess(buildPayload(referrerId, null, null))

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        val success = resultCaptor.firstValue as ReferrerResult.Success
        assertThat(success.referrerInfo).isEqualTo(
            ReferrerInfo(referrerId, 0, 0, ReferrerInfo.Source.RS)
        )
        verify(context).unbindService(connection)
    }

    @Test
    fun `onSuccess unbinds and returns Failure when payload is invalid JSON`() {
        val connection = bindAndGetConnection()
        connection.onServiceConnected(mock(), binder)
        val callback = captureCallback()

        callback.onSuccess("not-a-json")

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to parse RuStore referrer payload")
        assertThat(failure.throwable).isNotNull()
        verify(context).unbindService(connection)
    }

    @Test
    fun `onError unbinds and returns Failure with code and message`() {
        val connection = bindAndGetConnection()
        connection.onServiceConnected(mock(), binder)
        val callback = captureCallback()

        callback.onError(42, "something went wrong")

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("RuStore referrer error 42: something went wrong")
        verify(context).unbindService(connection)
    }

    // endregion

    // region ServiceConnection lifecycle

    @Test
    fun `onBindingDied unbinds and returns Failure`() {
        val connection = bindAndGetConnection()

        connection.onBindingDied(mock())

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        assertThat((resultCaptor.firstValue as ReferrerResult.Failure).message)
            .isEqualTo("RuStore service binding died")
        verify(context).unbindService(connection)
    }

    @Test
    fun `onNullBinding unbinds and returns Failure`() {
        val connection = bindAndGetConnection()

        connection.onNullBinding(mock())

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())
        assertThat((resultCaptor.firstValue as ReferrerResult.Failure).message)
            .isEqualTo("RuStore service returned null binding")
        verify(context).unbindService(connection)
    }

    @Test
    fun `onServiceDisconnected does not invoke listener and does not crash`() {
        val connection = bindAndGetConnection()

        connection.onServiceDisconnected(mock())

        verify(listener, never()).onResult(any())
    }

    // endregion

    // region helpers

    private fun makeResolveInfo(packageName: String, serviceName: String): ResolveInfo {
        val serviceInfo = ServiceInfo().apply {
            this.packageName = packageName
            this.name = serviceName
        }
        return ResolveInfo().apply { this.serviceInfo = serviceInfo }
    }

    private fun setupRuStoreComponent(serviceName: String = "ru.vk.store.sdk.ReferrerService") {
        val resolveInfo = makeResolveInfo(ruStorePackageName, serviceName)
        whenever(packageManager.queryIntentServices(any(), any<Int>())) doReturn listOf(resolveInfo)
    }

    private fun bindAndGetConnection(): ServiceConnection {
        setupRuStoreComponent()
        whenever(context.bindService(any(), any(), any<Int>())) doReturn true

        service.requestReferrer(listener)

        val captor = argumentCaptor<ServiceConnection>()
        verify(context).bindService(any(), captor.capture(), any<Int>())
        return captor.firstValue
    }

    private fun captureCallback(): GetInstallReferrerCallback {
        val captor = argumentCaptor<GetInstallReferrerCallback>()
        verify(aidlService).getInstallReferrer(any(), captor.capture())
        return captor.firstValue
    }

    /**
     * Creates a fresh [RuStoreReferrerService] + binds + connects and returns the [GetInstallReferrerCallback].
     * Uses a custom [listener] so the mock can be verified in isolation.
     */
    private fun connectAndGetCallback(listener: ReferrerListener): GetInstallReferrerCallback {
        val freshService = RuStoreReferrerService(context)
        setupRuStoreComponent()
        whenever(context.bindService(any(), any(), any<Int>())) doReturn true

        freshService.requestReferrer(listener)

        val connCaptor = argumentCaptor<ServiceConnection>()
        verify(context, atLeastOnce()).bindService(any(), connCaptor.capture(), any<Int>())
        connCaptor.lastValue.onServiceConnected(mock(), binder)

        val cbCaptor = argumentCaptor<GetInstallReferrerCallback>()
        verify(aidlService, atLeastOnce()).getInstallReferrer(any(), cbCaptor.capture())
        return cbCaptor.lastValue
    }

    private fun buildPayload(referrerId: String, clickTs: Long?, installTs: Long?): String {
        return JSONObject()
            .put("REFERRER_ID_KEY", referrerId)
            .put("RECEIVED_TIMESTAMP_KEY", clickTs)
            .put("INSTALL_APP_TIMESTAMP_KEY", installTs)
            .toString()
    }

    // endregion
}
