package io.appmetrica.analytics.impl.proxy

import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.ModulesFacade
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils
import io.appmetrica.analytics.impl.adrevenue.ModuleAdRevenueConverter
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.common.InternalModuleEvent
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InternalClientModuleProxyTest : CommonTest() {

    private val type = 4242
    private val name = "some_name"
    private val value = "some_value"
    private val serviceDataReporterType = 42
    private val extras = mapOf("key1" to "value1".toByteArray())
    private val attributes = mapOf("key2" to "value2")
    private val environment = mapOf("key3" to "value3")

    private val autoAdRevenue: ModuleAdRevenue = mock {
        on { autoCollected } doReturn true
    }
    private val adRevenue: AdRevenue = mock()

    private val moduleEventCaptor = argumentCaptor<ModuleEvent>()

    @get:Rule
    val modulesFacadeRule = staticRule<ModulesFacade>()

    @get:Rule
    val converterRule = MockedConstructionRule(ModuleAdRevenueConverter::class.java) { mock, _ ->
        whenever(mock.convert(autoAdRevenue)).thenReturn(adRevenue)
    }

    private val proxy by setUp { InternalClientModuleProxy() }

    @Test
    fun reportEvent() {
        val internalModuleEvent = InternalModuleEvent.newBuilder(type)
            .withName(name)
            .withValue(value)
            .withServiceDataReporterType(serviceDataReporterType)
            .withExtras(extras)
            .withAttributes(attributes)
            .withEnvironment(environment)
            .build()
        proxy.reportEvent(internalModuleEvent)

        modulesFacadeRule.staticMock.verify {
            ModulesFacade.reportEvent(moduleEventCaptor.capture())
        }
        ObjectPropertyAssertions(moduleEventCaptor.firstValue)
            .withPrivateFields(true)
            .checkField("type", type)
            .checkField("name", name)
            .checkField("value", value)
            .checkField("serviceDataReporterType", serviceDataReporterType)
            .checkField("extras", CollectionUtils.getListFromMap(extras))
            .checkField("attributes", CollectionUtils.getListFromMap(attributes))
            .checkField("environment", CollectionUtils.getListFromMap(environment))
            .checkAll()
    }

    @Test
    fun reportEventWithoutServiceDataReporterType() {
        val internalModuleEvent = InternalModuleEvent.newBuilder(type)
            .withName(name)
            .withValue(value)
            .withExtras(extras)
            .withAttributes(attributes)
            .withEnvironment(environment)
            .build()
        proxy.reportEvent(internalModuleEvent)

        modulesFacadeRule.staticMock.verify {
            ModulesFacade.reportEvent(moduleEventCaptor.capture())
        }
        ObjectPropertyAssertions(moduleEventCaptor.firstValue)
            .withPrivateFields(true)
            .checkField("type", type)
            .checkField("name", name)
            .checkField("value", value)
            .checkField("serviceDataReporterType", 1)
            .checkField("extras", CollectionUtils.getListFromMap(extras))
            .checkField("attributes", CollectionUtils.getListFromMap(attributes))
            .checkField("environment", CollectionUtils.getListFromMap(environment))
            .checkAll()
    }

    @Test
    fun reportAutoAdRevenueIfAutoIsTrue() {
        proxy.reportAdRevenue(autoAdRevenue)

        modulesFacadeRule.staticMock.verify {
            ModulesFacade.reportAdRevenue(adRevenue, true)
        }
    }

    @Test
    fun reportAutoAdRevenueIfAutoIsFalse() {
        proxy.reportAdRevenue(autoAdRevenue)

        modulesFacadeRule.staticMock.verify {
            ModulesFacade.reportAdRevenue(adRevenue, true)
        }
    }

}
