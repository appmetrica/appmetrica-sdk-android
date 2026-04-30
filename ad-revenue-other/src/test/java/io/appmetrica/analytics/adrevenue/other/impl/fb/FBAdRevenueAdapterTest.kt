package io.appmetrica.analytics.adrevenue.other.impl.fb

import com.facebook.ads.AdSDKNotificationManager
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.mock

internal class FBAdRevenueAdapterTest : CommonTest() {

    private val clientContext: ClientContext = mock()

    @get:Rule
    val adSDKNotificationManagerRule = staticRule<AdSDKNotificationManager>()

    @get:Rule
    val listenerRule = constructionRule<FBAdRevenueDataListener>()

    private val adapter = FBAdRevenueAdapter()

    @Test
    fun registerListener() {
        adapter.registerListener(clientContext)

        assertThat(listenerRule.constructionMock.constructed()).hasSize(1)
        assertThat(listenerRule.argumentInterceptor.flatArguments()).startsWith(clientContext)

        val listener = listenerRule.constructionMock.constructed().first()

        adSDKNotificationManagerRule.staticMock.verify {
            AdSDKNotificationManager.addSDKNotificationListener(listener)
        }
    }

    @Test
    fun registerListenerIdempotent() {
        adapter.registerListener(clientContext)
        adapter.registerListener(clientContext)

        assertThat(listenerRule.constructionMock.constructed()).hasSize(1)
    }

    @Test
    fun unregisterListener() {
        adapter.registerListener(clientContext)
        val listener = listenerRule.constructionMock.constructed().first()

        adapter.unregisterListener()

        adSDKNotificationManagerRule.staticMock.verify {
            AdSDKNotificationManager.removeSDKNotificationListener(listener)
        }
    }

    @Test
    fun unregisterListenerWhenNotRegistered() {
        adapter.unregisterListener()

        adSDKNotificationManagerRule.staticMock.verify(
            {
                AdSDKNotificationManager.removeSDKNotificationListener(ArgumentMatchers.any())
            },
            Mockito.never()
        )
    }

    @Test
    fun registerAfterUnregister() {
        adapter.registerListener(clientContext)
        adapter.unregisterListener()
        adapter.registerListener(clientContext)

        assertThat(listenerRule.constructionMock.constructed()).hasSize(2)
    }
}
