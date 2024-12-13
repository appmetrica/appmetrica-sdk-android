package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.SelfReportingArgumentsFactory
import io.appmetrica.analytics.impl.component.SelfSdkReportingComponentUnit
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.impl.startup.executor.StubbedExecutorFactory
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import org.junit.Rule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class SelfSdkReportingFactoryTest : CommonTest() {

    private val context: Context = mock()
    private val commonArguments: CommonArguments = mock()
    private val reporterArguments: CommonArguments.ReporterArguments = mock()
    private val startupState: StartupState = mock()
    private val componentsRepository: ComponentsRepository = mock()

    private val packageName = "package name"
    private val apiKey = "apiKey"

    private val clientDescription: ClientDescription = mock {
        on { packageName } doReturn packageName
        on { apiKey } doReturn apiKey
    }

    private val startupUnit: StartupUnit = mock {
        on { startupState } doReturn startupState
    }

    @get:Rule
    val componentIdMockedConstructionRule = constructionRule<ComponentId>()
    private val componentId by componentIdMockedConstructionRule

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val stubbedExecutorFactoryMockedConstructionRule = constructionRule<StubbedExecutorFactory>()
    private val stubbedExecutorFactory by stubbedExecutorFactoryMockedConstructionRule

    @get:Rule
    val selfReportArgumentsFactoryMockedConstructionRule = constructionRule<SelfReportingArgumentsFactory>()
    private val selfReportingArgumentsFactory by selfReportArgumentsFactoryMockedConstructionRule

    @get:Rule
    val selfSdkReportingComponentUnitMockedConstructionRule = constructionRule<SelfSdkReportingComponentUnit>()
    private val selfSdkReportingComponentUnit by selfSdkReportingComponentUnitMockedConstructionRule
}
