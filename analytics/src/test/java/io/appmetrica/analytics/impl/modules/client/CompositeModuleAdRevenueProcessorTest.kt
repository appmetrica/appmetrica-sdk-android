package io.appmetrica.analytics.impl.modules.client

import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessor
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class CompositeModuleAdRevenueProcessorTest : CommonTest() {

    private val firstProcessor: ModuleAdRevenueProcessor = mock {
        on { process("string") } doReturn true
    }
    private val secondProcessor: ModuleAdRevenueProcessor = mock {
        on { process("string") } doReturn true
    }

    private val logger: PublicLogger = mock()

    @get:Rule
    val loggerStorageRule: MockedStaticRule<LoggerStorage> = MockedStaticRule(LoggerStorage::class.java)

    private val processor = CompositeModuleAdRevenueProcessor()

    @Before
    fun setUp() {
        whenever(LoggerStorage.getMainPublicOrAnonymousLogger()).thenReturn(logger)
    }

    @Test
    fun process() {
        processor.register(firstProcessor)
        processor.register(secondProcessor)

        assertThat(processor.process("string")).isTrue()

        verify(firstProcessor).process("string")
        verifyNoInteractions(secondProcessor, logger)
    }

    @Test
    fun processIfSecondProcessorIsOk() {
        whenever(secondProcessor.process("string", "string")).doReturn(true)

        processor.register(firstProcessor)
        processor.register(secondProcessor)

        assertThat(processor.process("string", "string")).isTrue()

        verify(firstProcessor).process("string", "string")
        verify(secondProcessor).process("string", "string")
        verifyNoInteractions(logger)
    }

    @Test
    fun processIfNoProcessorIsOk() {
        processor.register(firstProcessor)
        processor.register(secondProcessor)

        assertThat(processor.process("string", "string")).isFalse()

        verify(firstProcessor).process("string", "string")
        verify(secondProcessor).process("string", "string")
        verify(logger).info(any())
    }
}
