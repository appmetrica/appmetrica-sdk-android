package io.appmetrica.analytics.plugins

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test

class StackTraceItemTest : CommonTest() {

    @Test
    fun noData() {
        val item = StackTraceItem.Builder().build()
        ObjectPropertyAssertions(item)
            .withPrivateFields(true)
            .checkFieldIsNull("className", "getClassName")
            .checkFieldIsNull("fileName", "getFileName")
            .checkFieldIsNull("line", "getLine")
            .checkFieldIsNull("column", "getColumn")
            .checkFieldIsNull("methodName", "getMethodName")
            .checkAll()
    }

    @Test
    fun filledData() {
        val className = "some class"
        val fileName = "some file"
        val methodName = "some method"
        val line = 678
        val column = 6
        val item = StackTraceItem.Builder()
            .withClassName(className)
            .withFileName(fileName)
            .withLine(line)
            .withColumn(column)
            .withMethodName(methodName)
            .build()
        ObjectPropertyAssertions(item)
            .withPrivateFields(true)
            .checkField("className", "getClassName", className)
            .checkField("fileName", "getFileName", fileName)
            .checkField("line", "getLine", line)
            .checkField("column", "getColumn", column)
            .checkField("methodName", "getMethodName", methodName)
            .checkAll()
    }
}
