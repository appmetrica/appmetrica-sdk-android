package io.appmetrica.analytics.impl.network

import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock

@RunWith(Parameterized::class)
class CompositeExecutionPolicyTest(
    private val inputPolicies: Array<IExecutionPolicy>,
    private val expectedResult: Boolean,
    private val expectedDescription: String
) : CommonTest() {

    companion object {

        private const val FIRST_ALLOW_POLICY_DESCRIPTION = "PolicyAllow#1"
        private val firstAllowExecutionPolicy = mock<IExecutionPolicy> {
            on { description() }.thenReturn(FIRST_ALLOW_POLICY_DESCRIPTION)
            on { canBeExecuted() }.thenReturn(true)
        }

        private const val SECOND_ALLOW_POLICY_DESCRIPTION = "PolicyAllow#2"
        private val secondAllowExecutionPolicy = mock<IExecutionPolicy> {
            on { description() }.thenReturn(SECOND_ALLOW_POLICY_DESCRIPTION)
            on { canBeExecuted() }.thenReturn(true)
        }

        private const val FIRST_FORBID_POLICY_DESCRIPTION = "PolicyForbid#1"
        private val firstForbidExecutionPolicy = mock<IExecutionPolicy> {
            on { description() }.thenReturn(FIRST_FORBID_POLICY_DESCRIPTION)
            on { canBeExecuted() }.thenReturn(false)
        }

        private const val SECOND_FORBID_POLICY_DESCRIPTION = "PolicyForbid#2"
        private val secondForbidExecutionPolicy = mock<IExecutionPolicy> {
            on { description() }.thenReturn(SECOND_FORBID_POLICY_DESCRIPTION)
            on { canBeExecuted() }.thenReturn(false)
        }

        @JvmStatic
        @Parameterized.Parameters(name = "[{index}] {2}")
        fun data() = listOf(
            prepareTestCase(emptyArray(), false),
            prepareTestCase(arrayOf(firstAllowExecutionPolicy, secondAllowExecutionPolicy), true),
            prepareTestCase(arrayOf(firstForbidExecutionPolicy, secondForbidExecutionPolicy), false),
            prepareTestCase(arrayOf(firstAllowExecutionPolicy, firstForbidExecutionPolicy), false),
            prepareTestCase(arrayOf(firstForbidExecutionPolicy, secondAllowExecutionPolicy), false),
            prepareTestCase(arrayOf(firstAllowExecutionPolicy), true),
            prepareTestCase(arrayOf(firstForbidExecutionPolicy), false)
        )

        private fun prepareTestCase(
            inputPolicies: Array<IExecutionPolicy>,
            expectedResult: Boolean
        ): Array<Any?> {
            val description = "Composite of {${inputPolicies.joinToString(", ") { it.description() }}}"
            return arrayOf(inputPolicies, expectedResult, description)
        }
    }

    @Test
    fun canBeExecuted() {
        val compositeExecutionPolicy = CompositeExecutionPolicy(*inputPolicies)
        assertThat(compositeExecutionPolicy.canBeExecuted()).isEqualTo(expectedResult)
    }

    @Test
    fun description() {
        val compositeExecutionPolicy = CompositeExecutionPolicy(*inputPolicies)
        assertThat(compositeExecutionPolicy.description()).isEqualTo(expectedDescription)
    }
}
