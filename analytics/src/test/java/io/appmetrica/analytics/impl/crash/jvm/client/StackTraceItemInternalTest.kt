package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.plugins.StackTraceItem
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

internal class StackTraceItemInternalTest : CommonTest() {

    @Test
    fun fromStackTraceItem() {
        val className = "class name"
        val fileName = "file name"
        val line = 66
        val column = 77
        val methodName = "some method"
        val input = StackTraceItem.Builder()
            .withClassName(className)
            .withFileName(fileName)
            .withLine(line)
            .withColumn(column)
            .withMethodName(methodName)
            .build()

        ObjectPropertyAssertions(input)
            .withPrivateFields(true)
            .checkField("className", "getClassName", className)
            .checkField("fileName", "getFileName", fileName)
            .checkField("line", "getLine", line)
            .checkField("column", "getColumn", column)
            .checkField("methodName", "getMethodName", methodName)
            .checkAll()

        val result = StackTraceItemInternal(input)
        ObjectPropertyAssertions(result)
            .checkField("className", "getClassName", className)
            .checkField("fileName", "getFileName", fileName)
            .checkField("line", "getLine", line)
            .checkField("column", "getColumn", column)
            .checkField("methodName", "getMethodName", methodName)
            .checkFieldIsNull("isAndroidNative", "isAndroidNative")
            .checkAll()
    }

    @Test
    fun fromStackTraceElement() {
        val className = "class name"
        val fileName = "file name"
        val line = 66
        val methodName = "some method"
        val stackTraceElement = mock(StackTraceElement::class.java)
        whenever(stackTraceElement.className).thenReturn(className)
        whenever(stackTraceElement.fileName).thenReturn(fileName)
        whenever(stackTraceElement.methodName).thenReturn(methodName)
        whenever(stackTraceElement.lineNumber).thenReturn(line)
        whenever(stackTraceElement.isNativeMethod).thenReturn(true)

        val result = StackTraceItemInternal(stackTraceElement)
        ObjectPropertyAssertions(result)
            .checkField("className", "getClassName", className)
            .checkField("fileName", "getFileName", fileName)
            .checkField("line", "getLine", line)
            .checkFieldIsNull("column", "getColumn")
            .checkField("methodName", "getMethodName", methodName)
            .checkField("isAndroidNative", "isAndroidNative", true)
            .checkAll()
    }
}
