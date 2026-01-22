package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.modulesapi.internal.network.NetworkClientWithCacheControl
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

internal class CacheControlConnectionHttpsClientImplTest : CommonTest() {

    private val eTag = "eTag"
    private val response = ByteArray(10) { it.toByte() }

    private val underlyingClient: NetworkClientWithCacheControl = mock {
        on { eTag } doReturn eTag
    }

    private val client by setUp { CacheControlConnectionHttpsClientImpl(underlyingClient) }

    @Test
    fun getOldETag() {
        assertThat(client.oldETag).isEqualTo(eTag)
    }

    @Test
    fun onResponse() {
        client.onResponse(eTag, response)
        verify(underlyingClient).onResponse(eTag, response)
        verifyNoMoreInteractions(underlyingClient)
    }

    @Test
    fun onNotModified() {
        client.onNotModified()
        verify(underlyingClient).onNotModified()
        verifyNoMoreInteractions(underlyingClient)
    }

    @Test
    fun onError() {
        client.onError()
        verify(underlyingClient).onError()
        verifyNoMoreInteractions(underlyingClient)
    }
}
