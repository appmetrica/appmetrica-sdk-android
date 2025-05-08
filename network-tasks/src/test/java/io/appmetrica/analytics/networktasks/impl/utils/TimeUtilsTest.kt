package io.appmetrica.analytics.networktasks.impl.utils

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
class TimeUtilsTest : CommonTest() {

    private val gregorianCalendar = mock<GregorianCalendar>()

    @Test
    fun getTimeZoneOffsetSec() {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = parser.parse("2022-06-01 01:00:00").time / 1000
        val staticCalendar = Mockito.mockStatic(Calendar::class.java)
        try {
            whenever(Calendar.getInstance()).thenReturn(gregorianCalendar)
            stubbing(gregorianCalendar) {
                on { timeZone } doReturn TimeZone.getTimeZone("PST")
            }
            val expected = -7 * 60 * 60 // diff is 7 hours
            Assertions.assertThat(TimeUtils.getTimeZoneOffsetSec(date)).isEqualTo(expected)
        } finally {
            staticCalendar.close()
        }
    }
}
