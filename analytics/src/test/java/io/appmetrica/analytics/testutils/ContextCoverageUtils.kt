package io.appmetrica.analytics.testutils

import android.content.Context
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.lang.reflect.Modifier

object ContextCoverageUtils {

    fun checkCoverage(`class`: Class<*>, testClass: Class<*>) {
        val methodsWithContext = `class`.methods.filter {
            it.modifiers and Modifier.PUBLIC != 0 && !it.isBridge && it.parameterTypes.contains(Context::class.java)
        }
        val tests = testClass.declaredMethods.filter {
            it.isAnnotationPresent(Test::class.java) && it.name != "coverage"
        }
        assertThat(tests)
            .overridingErrorMessage("Actual:\n [${tests.joinToString("\n") { it.name }}],\n" +
                "expected: [${methodsWithContext.joinToString("\n") { it.name }}]")
            .hasSameSizeAs(methodsWithContext)

    }
}
