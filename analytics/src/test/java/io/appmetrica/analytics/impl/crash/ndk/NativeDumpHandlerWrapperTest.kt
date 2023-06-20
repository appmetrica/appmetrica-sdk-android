package io.appmetrica.analytics.impl.crash.ndk

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import java.io.File

class NativeDumpHandlerWrapperTest : CommonTest() {

    private val value = "someReadValue"

    @get:Rule
    public val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var nativeDumpHandler: NativeDumpHandler
    @Mock
    lateinit var file: File
    @Mock
    lateinit var description: NativeCrashHandlerDescription

    @Test
    fun apply() {
        doReturn(value).`when`(nativeDumpHandler).apply(any(), any())

        assertThat(NativeDumpHandlerWrapper(description, nativeDumpHandler).apply(file)).isEqualTo(value)
        verify(nativeDumpHandler).apply(file, description)
    }

}
