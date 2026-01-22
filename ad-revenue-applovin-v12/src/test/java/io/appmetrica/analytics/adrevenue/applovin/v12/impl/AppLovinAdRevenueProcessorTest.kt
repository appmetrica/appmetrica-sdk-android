package io.appmetrica.analytics.adrevenue.applovin.v12.impl

import com.applovin.mediation.MaxAd
import com.applovin.sdk.AppLovinSdk
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.common.InternalClientModuleFacade
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

internal class AppLovinAdRevenueProcessorTest : CommonTest() {

    private val maxAd: MaxAd = mock()
    private val appLovinSdk: AppLovinSdk = mock()
    private val moduleAdRevenue: ModuleAdRevenue = mock()
    private val internalClientModuleFacade: InternalClientModuleFacade = mock()
    private val converter: AdRevenueConverter = mock {
        on { convert(maxAd, appLovinSdk) } doReturn moduleAdRevenue
    }
    private val clientContext: ClientContext = mock {
        on { internalClientModuleFacade } doReturn internalClientModuleFacade
    }

    @get:Rule
    var reflectionUtilsRule = MockedStaticRule(
        ReflectionUtils::class.java
    ) {
        whenever(
            ReflectionUtils.isArgumentsOfClasses(
                arrayOf(maxAd, appLovinSdk),
                MaxAd::class.java,
                AppLovinSdk::class.java
            )
        ).thenReturn(true)
    }

    private val processor = AppLovinAdRevenueProcessor(
        converter,
        clientContext
    )

    @Test
    fun process() {
        assertThat(processor.process(maxAd, appLovinSdk)).isTrue()
        verify(internalClientModuleFacade).reportAdRevenue(moduleAdRevenue)
    }

    @Test
    fun processWithWrongNumberOfParameters() {
        assertThat(processor.process(maxAd)).isFalse()
        verifyNoInteractions(internalClientModuleFacade)
    }

    @Test
    fun processWithWrongParameters() {
        whenever(
            ReflectionUtils.isArgumentsOfClasses(
                arrayOf(maxAd, appLovinSdk),
                MaxAd::class.java,
                AppLovinSdk::class.java
            )
        ).thenReturn(false)

        assertThat(processor.process(maxAd, appLovinSdk)).isFalse()
        verifyNoInteractions(internalClientModuleFacade)
    }
}
