package io.appmetrica.analytics.adrevenue.applovin.v12.internal

import io.appmetrica.analytics.adrevenue.applovin.v12.impl.AppLovinAdRevenueProcessor
import io.appmetrica.analytics.adrevenue.applovin.v12.impl.Constants
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils.detectClassExists
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueContext
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessorsHolder
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class AppLovinClientModuleEntryPointTest : CommonTest() {

    private val adRevenueProcessorsHolder: ModuleAdRevenueProcessorsHolder = mock()
    private val moduleAdRevenueContext: ModuleAdRevenueContext = mock {
        on { adRevenueProcessorsHolder } doReturn adRevenueProcessorsHolder
    }
    private val clientContext: ClientContext = mock {
        on { moduleAdRevenueContext } doReturn moduleAdRevenueContext
    }

    @get:Rule
    val reflectionUtilsRule: MockedStaticRule<ReflectionUtils> = MockedStaticRule(ReflectionUtils::class.java)
    @get:Rule
    val appLovinAdRevenueProcessorRule: MockedConstructionRule<AppLovinAdRevenueProcessor> =
        MockedConstructionRule(AppLovinAdRevenueProcessor::class.java)

    private val entryPoint: AppLovinClientModuleEntryPoint = AppLovinClientModuleEntryPoint()

    @Test
    fun getIdentifier() {
        assertThat(entryPoint.identifier).isEqualTo(Constants.MODULE_ID)
    }

    @Test
    fun initClientSide() {
        whenever(detectClassExists(Constants.LIBRARY_MAIN_CLASS)).thenReturn(true)

        entryPoint.initClientSide(clientContext)

        verify(adRevenueProcessorsHolder)
            .register(appLovinAdRevenueProcessorRule.constructionMock.constructed().first())
    }

    @Test
    fun onActivatedIfNoLibrary() {
        whenever(detectClassExists(Constants.LIBRARY_MAIN_CLASS)).thenReturn(false)

        entryPoint.initClientSide(clientContext)

        verifyNoInteractions(clientContext)
    }
}
