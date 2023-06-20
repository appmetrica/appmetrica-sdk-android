package io.appmetrica.analytics.plugins

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PluginErrorDetailsTest : CommonTest() {

    @Test
    fun filledData() {
        val exceptionClass = "some class"
        val message = "some message"
        val platform = "unity"
        val stacktrace = listOf(StackTraceItem.Builder().build())
        val environment = mapOf("key1" to "value1", "key2" to "22")
        val virtualMachineVersion = "6.5.7"
        val details = PluginErrorDetails.Builder()
            .withExceptionClass(exceptionClass)
            .withMessage(message)
            .withPlatform(platform)
            .withStacktrace(stacktrace)
            .withPluginEnvironment(environment)
            .withVirtualMachineVersion(virtualMachineVersion)
            .build()
        ObjectPropertyAssertions(details)
            .withPrivateFields(true)
            .checkField("exceptionClass", "getExceptionClass", exceptionClass)
            .checkField("message", "getMessage", message)
            .checkField("platform", "getPlatform", platform)
            .checkField("stacktrace", "getStacktrace", stacktrace)
            .checkField("pluginEnvironment", "getPluginEnvironment", environment)
            .checkField("virtualMachineVersion", "getVirtualMachineVersion", virtualMachineVersion)
            .checkAll()
        assertThat(details.stacktrace).isNotSameAs(stacktrace)
        assertThat(details.pluginEnvironment).isNotSameAs(environment)
    }

    @Test
    fun noData() {
        val details = PluginErrorDetails.Builder().build()
        ObjectPropertyAssertions(details)
            .withPrivateFields(true)
            .checkFieldIsNull("exceptionClass", "getExceptionClass")
            .checkFieldIsNull("message", "getMessage")
            .checkFieldIsNull("platform", "getPlatform")
            .checkField("stacktrace", "getStacktrace", emptyList<StackTraceItem>())
            .checkField("pluginEnvironment", "getPluginEnvironment", emptyMap<String, Any>())
            .checkFieldIsNull("virtualMachineVersion", "getVirtualMachineVersion")
            .checkAll()
    }
}
