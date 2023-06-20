package io.appmetrica.analytics

import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.impl.permissions.CompositePermissionStrategy
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.startup.CollectingFlags.CollectingFlagsBuilder
import io.appmetrica.analytics.impl.startup.StartupStateModel
import io.appmetrica.analytics.impl.startup.StartupStateModel.StartupStateBuilder
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.lang.reflect.Modifier
import java.util.function.Predicate

@RunWith(RobolectricTestRunner::class)
class ToStringSpecialCasesTest : CommonTest() {

    @Test
    fun startupStateModelToString() {
        val actualValue = StartupStateBuilder(CollectingFlagsBuilder().build()).build()
        val extractedFieldAndValues = ToStringTestUtils.extractFieldsAndValues(
            StartupStateModel::class.java,
            actualValue,
            Modifier.PUBLIC or Modifier.FINAL,
            setOf("deviceID", "deviceIDHash")
        )
        ToStringTestUtils.testToString(actualValue, extractedFieldAndValues)
    }

    @Test
    fun startupRequestConfigToString() {
        val actualValue = ToStringTestUtils.mockValue(StartupRequestConfig::class.java)
        val extractedFieldAndValues = ToStringTestUtils.extractFieldsAndValues(
            StartupRequestConfig::class.java,
            actualValue,
            Modifier.PRIVATE or Modifier.FINAL,
            setOf("mReferrerHolder", "defaultStartupHostsProvider")
        )
        ToStringTestUtils.testToString(actualValue, extractedFieldAndValues)
    }

    @Test
    fun compositePermissionStrategyToString() {
        val first = mock<PermissionStrategy>()
        val second = mock<PermissionStrategy>()
        val actualValue = CompositePermissionStrategy(first, second)
        val pattern = "strategies=${arrayOf(first, second).contentToString()}"
        val predicate = object: Predicate<String> {

            override fun test(t: String): Boolean = t.contains(pattern)

            override fun toString(): String = "Contains `$pattern`"
        }
        ToStringTestUtils.testToString(actualValue, listOf(predicate))
    }
}
