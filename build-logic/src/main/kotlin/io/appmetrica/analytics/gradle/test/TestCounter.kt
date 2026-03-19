package io.appmetrica.analytics.gradle.test

import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult

class TestCounter : TestListener {
    var totalTests: Long = 0
        private set
    var failedTests: Long = 0
        private set
    var skippedTests: Long = 0
        private set
    var hadFailures = false
        private set

    override fun beforeSuite(suite: TestDescriptor) {
    }

    override fun afterSuite(suite: TestDescriptor, result: TestResult) {
        if (suite.parent == null) {
            hadFailures = result.resultType == TestResult.ResultType.FAILURE
        }
    }

    override fun beforeTest(testDescriptor: TestDescriptor) {
    }

    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
        totalTests += result.testCount
        failedTests += result.failedTestCount
        skippedTests += result.skippedTestCount
    }

    fun reportString(): String = buildString {
        append(totalTests).append(" tests, ")
        append(failedTests).append(" failed, ")
        append(skippedTests).append(" skipped")
    }

    operator fun plus(other: TestCounter): TestCounter {
        val result = TestCounter()
        result.totalTests = totalTests + other.totalTests
        result.failedTests = failedTests + other.failedTests
        result.skippedTests = skippedTests + other.skippedTests
        result.hadFailures = hadFailures || other.hadFailures
        return result
    }
}
