package io.appmetrica.analytics.networktasks.internal

import android.net.Uri
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FullUrlFormerTest : CommonTest() {

    private val paramsAppender = mock<IParamsAppender<Any>>()
    private val configProvider = mock<ConfigProvider<Any>>()
    private val hosts = listOf("http://host1.ru", "http://host2.ru", "http://host3.ru", "http://host4.ru")
    private val fullUrlFormer =
        FullUrlFormer(
            paramsAppender,
            configProvider
        )

    @Test
    fun getAllHostsInitialNull() {
        assertThat(fullUrlFormer.allHosts).isEmpty()
    }

    @Test
    fun getAllHostsAfterSetHosts() {
        fullUrlFormer.setHosts(hosts)
        assertThat(fullUrlFormer.allHosts).isEqualTo(hosts)
    }

    @Test
    fun getAllHostsAfterSetNullHosts() {
        fullUrlFormer.setHosts(null)
        assertThat(fullUrlFormer.allHosts).isEmpty()
    }

    @Test
    fun hasMoreHosts() {
        // no hosts
        assertThat(fullUrlFormer.hasMoreHosts()).isFalse

        fullUrlFormer.setHosts(hosts)
        // 4 hosts, attempt #0
        assertThat(fullUrlFormer.hasMoreHosts()).isTrue
        fullUrlFormer.incrementAttemptNumber()
        // 4 hosts, attempt #1
        assertThat(fullUrlFormer.hasMoreHosts()).isTrue
        fullUrlFormer.incrementAttemptNumber()
        // 4 hosts, attempt #2
        assertThat(fullUrlFormer.hasMoreHosts()).isTrue
        fullUrlFormer.incrementAttemptNumber()
        // 4 hosts, attempt #3
        assertThat(fullUrlFormer.hasMoreHosts()).isTrue
        fullUrlFormer.incrementAttemptNumber()
        // 4 hosts, attempt #4
        assertThat(fullUrlFormer.hasMoreHosts()).isFalse
    }

    @Test
    fun formUrl() {
        val config = Any()
        stubbing(configProvider) {
            on { this.config } doReturn config
        }
        doAnswer {
            (it.arguments[0] as Uri.Builder).appendQueryParameter("key1", "value1")
        }.whenever(paramsAppender).appendParams(any(), same(config))

        fullUrlFormer.setHosts(hosts)
        fullUrlFormer.incrementAttemptNumber()
        fullUrlFormer.buildAndSetFullHostUrl()
        assertThat(fullUrlFormer.url).isEqualTo("https://host1.ru?key1=value1")

        fullUrlFormer.incrementAttemptNumber()
        fullUrlFormer.incrementAttemptNumber()
        fullUrlFormer.incrementAttemptNumber()
        fullUrlFormer.buildAndSetFullHostUrl()
        assertThat(fullUrlFormer.url).isEqualTo("https://host4.ru?key1=value1")
    }

    @Test
    fun invalidAttemptNumber() {
        fullUrlFormer.setHosts(hosts)
        // attempt #-1
        assertThrows(IndexOutOfBoundsException::class.java) {
            fullUrlFormer.buildAndSetFullHostUrl()
        }

        fullUrlFormer.incrementAttemptNumber()
        fullUrlFormer.incrementAttemptNumber()
        fullUrlFormer.incrementAttemptNumber()
        fullUrlFormer.incrementAttemptNumber()
        fullUrlFormer.incrementAttemptNumber()

        // attempt #4
        assertThrows(IndexOutOfBoundsException::class.java) {
            fullUrlFormer.buildAndSetFullHostUrl()
        }
    }
}
