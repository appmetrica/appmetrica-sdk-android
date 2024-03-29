package io.appmetrica.analytics.impl.crash

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.IReporterFactoryProvider
import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.analytics.impl.TestsData
import io.appmetrica.analytics.impl.crash.client.ICrashProcessor
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class CrashProcessorFactoryTest : CommonTest() {

    private val reporterFactoryProvider = mock<IReporterFactoryProvider>()
    private val context = mock<Context>()
    val config = AppMetricaConfig.newConfigBuilder(TestsData.UUID_API_KEY).build()

    @get:Rule
    val sdkUtils = MockedStaticRule(SdkUtils::class.java)

    private val sdkCrashProcessor = mock<ICrashProcessor>()
    @get:Rule
    val sdkCrashProcessorCreatorMockedRule = MockedConstructionRule(
        SdkCrashProcessorCreator::class.java
    ) { mock, _ ->
        whenever(mock.createCrashProcessor(context, config, reporterFactoryProvider)).thenReturn(sdkCrashProcessor)
    }

    private val pushCrashProcessor = mock<ICrashProcessor>()
    @get:Rule
    val pushCrashProcessorCreatorMockedRule = MockedConstructionRule(
        PushCrashProcessorCreator::class.java
    ) { mock, _ ->
        whenever(mock.createCrashProcessor(context, config, reporterFactoryProvider)).thenReturn(pushCrashProcessor)
    }

    private val appCrashProcessor = mock<ICrashProcessor>()
    @get:Rule
    val appCrashProcessorCreatorMockedRule = MockedConstructionRule(
        ApplicationCrashProcessorCreator::class.java
    ) { mock, _ ->
        whenever(mock.createCrashProcessor(context, config, reporterFactoryProvider)).thenReturn(appCrashProcessor)
    }

    private val crashProcessor = mock<ICrashProcessor>()
    private val crashProcessorCreator = mock<CrashProcessorCreator> {
        on { createCrashProcessor(context, config, reporterFactoryProvider) } doReturn crashProcessor
    }

    @Test
    fun createCrashProcessors() {
        val factory = CrashProcessorFactory()
        factory.registerCrashProcessorCreator(crashProcessorCreator)

        assertThat(factory.createCrashProcessors(context, config, reporterFactoryProvider))
            .containsExactlyInAnyOrder(
                sdkCrashProcessor,
                pushCrashProcessor,
                appCrashProcessor,
                crashProcessor,
            )
    }

    @Test
    fun createCrashProcessorsWithCustomCreator() {
        val factory = CrashProcessorFactory()

        assertThat(factory.createCrashProcessors(context, config, reporterFactoryProvider))
            .containsExactlyInAnyOrder(
                sdkCrashProcessor,
                pushCrashProcessor,
                appCrashProcessor,
            )
    }
}
