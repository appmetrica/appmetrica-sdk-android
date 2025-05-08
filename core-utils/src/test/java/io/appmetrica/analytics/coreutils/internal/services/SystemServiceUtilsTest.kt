package io.appmetrica.analytics.coreutils.internal.services

import android.content.Context
import android.net.wifi.WifiManager
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.nullable
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SystemServiceUtilsTest : CommonTest() {

    private val context = mock<Context>()
    private val wifiManager = mock<WifiManager>()
    private val functionWithThrowable = mock<FunctionWithThrowable<WifiManager, Int?>>()

    @Test
    fun accessSystemServiceSafelyCallableNull() {
        assertThat(
            SystemServiceUtils.accessSystemServiceSafely(
                null,
                "getting sth",
                "WifiManager",
                functionWithThrowable
            )
        ).isNull()
        verify(functionWithThrowable, never()).apply(any())
    }

    @Test
    fun accessSystemServiceSafelyCallableException() {
        doThrow(RuntimeException()).whenever(functionWithThrowable).apply(wifiManager)
        assertThat(
            SystemServiceUtils.accessSystemServiceSafely(
                wifiManager,
                "getting sth",
                "WifiManager",
                functionWithThrowable
            )
        ).isNull()
        verify(functionWithThrowable).apply(wifiManager)
    }

    @Test
    fun accessSystemServiceSafelyCallable() {
        val res = 42
        whenever(functionWithThrowable.apply(wifiManager)).thenReturn(res)
        assertThat(
            SystemServiceUtils.accessSystemServiceSafely(
                wifiManager,
                "getting sth",
                "WifiManager",
                functionWithThrowable
            )
        ).isEqualTo(res)
    }

    @Test
    fun accessSystemServiceSafelyOrDefaultCallable() {
        val res = 42
        whenever(functionWithThrowable.apply(wifiManager)).thenReturn(res)
        assertThat(
            SystemServiceUtils.accessSystemServiceSafelyOrDefault(
                wifiManager,
                "getting sth",
                "WifiManager",
                10,
                functionWithThrowable
            )
        ).isEqualTo(res)
    }

    @Test
    @Throws(Throwable::class)
    fun accessSystemServiceSafelyOrDefaultCallableNull() {
        val def = 10
        whenever(functionWithThrowable.apply(wifiManager)).thenReturn(null)
        assertThat(
            SystemServiceUtils.accessSystemServiceSafelyOrDefault(
                wifiManager,
                "getting sth",
                "WifiManager",
                def,
                functionWithThrowable
            )
        ).isEqualTo(def)
    }

    @Test
    fun accessSystemServiceSafelyByNameNull() {
        whenever<Any?>(context.getSystemService(Context.WIFI_SERVICE)).thenReturn(null)
        assertThat(
            SystemServiceUtils.accessSystemServiceByNameSafely(
                context,
                Context.WIFI_SERVICE,
                "getting sth",
                "WifiManager",
                functionWithThrowable
            )
        ).isNull()
        verify(functionWithThrowable, never()).apply(nullable(WifiManager::class.java))
    }

    @Test
    fun accessSystemServiceSafelyByNameException() {
        doThrow(java.lang.RuntimeException()).whenever(functionWithThrowable).apply(wifiManager)
        whenever(context.getSystemService(Context.WIFI_SERVICE)).thenReturn(wifiManager)
        assertThat(
            SystemServiceUtils.accessSystemServiceByNameSafely(
                context,
                Context.WIFI_SERVICE,
                "getting sth",
                "WifiManager",
                functionWithThrowable
            )
        ).isNull()
        verify(functionWithThrowable).apply(wifiManager)
    }

    @Test
    fun accessSystemServiceSafelyByNameSuccess() {
        val res = 42
        whenever(context.getSystemService(Context.WIFI_SERVICE)).thenReturn(wifiManager)
        whenever(functionWithThrowable.apply(wifiManager)).thenReturn(res)
        assertThat(
            SystemServiceUtils.accessSystemServiceByNameSafely(
                context,
                Context.WIFI_SERVICE,
                "getting sth",
                "WifiManager",
                functionWithThrowable
            )
        ).isEqualTo(res)
    }
}
