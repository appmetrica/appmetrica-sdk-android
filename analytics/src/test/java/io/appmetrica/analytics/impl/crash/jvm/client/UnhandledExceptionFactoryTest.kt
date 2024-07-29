package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads
import io.appmetrica.analytics.impl.crash.jvm.client.StackTraceItemInternal
import io.appmetrica.analytics.impl.crash.jvm.client.ThrowableModel
import io.appmetrica.analytics.impl.crash.jvm.client.ThrowableModelFactory
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledExceptionFactory
import io.appmetrica.analytics.plugins.StackTraceItem
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock

class UnhandledExceptionFactoryTest : CommonTest() {

    @Mock
    private lateinit var throwableModel: ThrowableModel
    @Rule
    @JvmField
    internal val sThrowableModelFactory = MockedStaticRule(ThrowableModelFactory::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun getUnhandledExceptionFromJavaFilled() {
        val exception = IllegalStateException("some message")
        `when`(ThrowableModelFactory.createModel(exception)).thenReturn(throwableModel)
        val allThreads = mock<AllThreads>()
        val element1 = StackTraceElement("class name 1", "method name 1", "file name 1", 11)
        val element2 = StackTraceElement("class name 2", "method name 2", "file name 2", 22)
        val stacktrace = listOf(element1, element2)
        val buildId = "111222"
        val isOffline = true
        val result = UnhandledExceptionFactory.getUnhandledExceptionFromJava(
            exception,
            allThreads,
            stacktrace,
            buildId,
            isOffline
        )
        ObjectPropertyAssertions(result)
            .checkField("exception", throwableModel)
            .checkField("allThreads", allThreads)
            .checkFieldComparingFieldByFieldRecursively("methodCallStacktrace", listOf(
                StackTraceItemInternal(element1), StackTraceItemInternal(element2)
            ))
            .checkField("buildId", buildId)
            .checkField("isOffline", isOffline)
            .checkFieldsAreNull("platform", "virtualMachineVersion", "pluginEnvironment")
            .checkAll()
    }

    @Test
    fun getUnhandledExceptionFromJavaNullable() {
        val result = UnhandledExceptionFactory.getUnhandledExceptionFromJava(null, null, null, null, null)
        ObjectPropertyAssertions(result)
            .checkFieldsAreNull("exception", "allThreads", "methodCallStacktrace", "buildId", "isOffline",
                "platform", "virtualMachineVersion", "pluginEnvironment")
            .checkAll()
    }

    @Test
    fun getUnhandledExceptionFromPlugin() {
        val exceptionClass = "some class"
        val message = "message"
        val element1 = StackTraceItem.Builder().withClassName("class1").build()
        val element2 = StackTraceItem.Builder().withClassName("class2").build()
        val platform = "flutter"
        val virtualMachineVersion = "4.4.4"
        val environment = mapOf("key1" to "value1", "key2" to "22")
        val stacktrace = listOf(element1, element2)
        val buildId = "111222"
        val isOffline = true
        val result = UnhandledExceptionFactory.getUnhandledExceptionFromPlugin(
            exceptionClass,
            message,
            stacktrace,
            platform,
            virtualMachineVersion,
            environment,
            buildId,
            isOffline
        )
        ObjectPropertyAssertions(result)
            .checkFieldRecursively<ThrowableModel>("exception") {
                it
                    .withPrivateFields(true)
                    .checkField("message", message)
                    .checkField("exceptionClass", exceptionClass)
                    .checkFieldComparingFieldByFieldRecursively("stacktrace", listOf(
                        StackTraceItemInternal(element1), StackTraceItemInternal(element2)
                    ))
                    .checkFieldsAreNull("cause", "suppressed")
            }
            .checkField("platform", platform)
            .checkField("virtualMachineVersion", virtualMachineVersion)
            .checkField("pluginEnvironment", environment)
            .checkField("buildId", buildId)
            .checkField("isOffline", isOffline)
            .checkFieldsAreNull("methodCallStacktrace", "allThreads")
            .checkAll()
    }

    @Test
    fun getUnhandledExceptionFromPluginNullable() {
        val result = UnhandledExceptionFactory.getUnhandledExceptionFromPlugin(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        ObjectPropertyAssertions(result)
            .checkFieldRecursively<ThrowableModel>("exception") {
                it
                    .withPrivateFields(true)
                    .checkFieldsAreNull("exceptionClass", "message", "cause", "suppressed", "stacktrace")
            }
            .checkFieldsAreNull("allThreads", "methodCallStacktrace", "buildId", "isOffline",
                "platform", "virtualMachineVersion", "pluginEnvironment")
            .checkAll()
    }
}
