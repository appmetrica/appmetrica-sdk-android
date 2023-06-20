package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.networktasks.impl.utils.TimeUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.doReturn

class RequestDataHolderTest {

    private val requestDataHolder =
        RequestDataHolder()

    @Test
    fun getPostDataDefault() {
        assertThat(requestDataHolder.postData).isNull()
    }

    @Test
    fun getMethodDefault() {
        assertThat(requestDataHolder.method).isEqualTo(NetworkTask.Method.GET)
    }

    @Test
    fun getHeadersDefault() {
        assertThat(requestDataHolder.headers).isEmpty()
    }

    @Test
    fun getSendTimestampDefault() {
        assertThat(requestDataHolder.sendTimestamp).isNull()
    }

    @Test
    fun getSendTimezoneSec() {
        assertThat(requestDataHolder.sendTimezoneSec).isNull()
    }

    @Test
    fun setPostData() {
        val postData = "post data".toByteArray()
        requestDataHolder.postData = postData
        assertThat(requestDataHolder.postData).isEqualTo(postData)
        assertThat(requestDataHolder.method).isEqualTo(NetworkTask.Method.POST)
    }

    @Test
    fun setHeaders() {
        val headers = mapOf("key1" to listOf("header1", "header2"), "key2" to listOf("header3"))
        requestDataHolder.setHeader("key1", "header1", "header2")
        requestDataHolder.setHeader("key2", "header3")
        assertThat(requestDataHolder.headers).containsExactlyInAnyOrderEntriesOf(headers)
    }

    @Test
    fun applySendTime() {
        val timestamp = 82374687L
        val offset = 6677
        val staticTimeUtils = Mockito.mockStatic(TimeUtils::class.java)
        try {
            `when`(TimeUtils.getTimeZoneOffsetSec(timestamp / 1000)).doReturn(offset)
            requestDataHolder.applySendTime(timestamp)
            assertThat(requestDataHolder.sendTimestamp).isEqualTo(timestamp)
            assertThat(requestDataHolder.sendTimezoneSec).isEqualTo(offset)
        } finally {
            staticTimeUtils.close()
        }
    }
}
