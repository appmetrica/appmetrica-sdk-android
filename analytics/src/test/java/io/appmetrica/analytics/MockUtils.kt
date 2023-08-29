package io.appmetrica.analytics

import io.appmetrica.analytics.testutils.RandomStringGenerator
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

object MockUtils {

    val stringGenerator = RandomStringGenerator(100)

    inline fun <reified T : Any> mockForToString(mockedString: String = stringGenerator.nextString()): T = mock<T> {
        on { toString() } doReturn mockedString
    }
}
