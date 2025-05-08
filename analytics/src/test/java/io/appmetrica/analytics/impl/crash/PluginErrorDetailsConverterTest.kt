package io.appmetrica.analytics.impl.crash

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.ExtraMetaInfoRetriever
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledExceptionFactory
import io.appmetrica.analytics.plugins.PluginErrorDetails
import io.appmetrica.analytics.plugins.StackTraceItem
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PluginErrorDetailsConverterTest : CommonTest() {

    private val extraMetaInfoRetriever: ExtraMetaInfoRetriever = mock()
    private val unhandledException: UnhandledException = mock()

    @get:Rule
    internal val sUnhandledExceptionFactory = MockedStaticRule(UnhandledExceptionFactory::class.java)
    private lateinit var converter: PluginErrorDetailsConverter

    private val buildId = "333444"
    private val isOffline = true
    private val exceptionClass = "some class"
    private val message = "some message"
    private val platform = "unity"
    private val stacktrace = listOf(StackTraceItem.Builder().build())
    private val environment = mapOf("key1" to "value1", "key2" to "22")
    private val virtualMachineVersion = "6.5.7"
    private val filledInput = PluginErrorDetails.Builder()
        .withExceptionClass(exceptionClass)
        .withMessage(message)
        .withPlatform(platform)
        .withStacktrace(stacktrace)
        .withPluginEnvironment(environment)
        .withVirtualMachineVersion(virtualMachineVersion)
        .build()

    @Before
    fun setUp() {
        ObjectPropertyAssertions(filledInput)
            .withPrivateFields(true)
            .checkField("exceptionClass", "getExceptionClass", exceptionClass)
            .checkField("message", "getMessage", message)
            .checkField("platform", "getPlatform", platform)
            .checkField("stacktrace", "getStacktrace", stacktrace)
            .checkField("pluginEnvironment", "getPluginEnvironment", environment)
            .checkField("virtualMachineVersion", "getVirtualMachineVersion", virtualMachineVersion)
            .checkAll()

        whenever(extraMetaInfoRetriever.buildId).thenReturn(buildId)
        whenever(extraMetaInfoRetriever.isOffline).thenReturn(isOffline)
        whenever(
            UnhandledExceptionFactory.getUnhandledExceptionFromPlugin(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(unhandledException)
        converter = PluginErrorDetailsConverter(extraMetaInfoRetriever)
    }

    @Test
    fun toRegularError() {
        val errorMessage = "error message"
        ObjectPropertyAssertions(converter.toRegularError(errorMessage, filledInput))
            .checkField("message", errorMessage)
            .checkField("exception", unhandledException)
            .checkAll()
        sUnhandledExceptionFactory.staticMock.verify {
            UnhandledExceptionFactory.getUnhandledExceptionFromPlugin(
                exceptionClass,
                message,
                stacktrace,
                platform,
                virtualMachineVersion,
                environment,
                buildId,
                isOffline
            )
        }
    }

    @Test
    fun toRegularErrorNullablr() {
        ObjectPropertyAssertions(converter.toRegularError(null, null))
            .checkFieldsAreNull("message", "exception")
            .checkAll()
    }

    @Test
    fun toUnhandledException() {
        assertThat(converter.toUnhandledException(filledInput)).isSameAs(unhandledException)
        sUnhandledExceptionFactory.staticMock.verify {
            UnhandledExceptionFactory.getUnhandledExceptionFromPlugin(
                exceptionClass,
                message,
                stacktrace,
                platform,
                virtualMachineVersion,
                environment,
                buildId,
                isOffline
            )
        }
    }
}
