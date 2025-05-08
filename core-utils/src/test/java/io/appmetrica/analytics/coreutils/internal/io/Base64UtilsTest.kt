package io.appmetrica.analytics.coreutils.internal.io

import android.util.Base64
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class Base64UtilsTest : CommonTest() {

    @Test
    @Throws(IOException::class)
    fun stringCompressionToBase64() {
        val value = "anfbfidsaobfisadfnisdnfidsf"
        assertThat(Base64Utils.compressBase64String(value))
            .isEqualTo(Base64.encodeToString(GZIPUtils.gzipBytes(value.toByteArray(charset("UTF-8"))), Base64.DEFAULT))
    }

    @Test
    @Throws(IOException::class)
    fun byteCompressionToBase64() {
        val bytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        assertThat(Base64Utils.compressBase64(bytes))
            .isEqualTo(Base64.encodeToString(GZIPUtils.gzipBytes(bytes), Base64.DEFAULT))
    }

    @Test
    @Throws(IOException::class)
    fun byteDecompressionFromBase64() {
        val bytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val gzippedBytes = GZIPUtils.gzipBytes(bytes)
        val encodedBytes = Base64.encodeToString(gzippedBytes, Base64.DEFAULT)
        assertThat(Base64Utils.decompressBase64GzipAsBytes(encodedBytes)).isEqualTo(bytes)
    }
}
