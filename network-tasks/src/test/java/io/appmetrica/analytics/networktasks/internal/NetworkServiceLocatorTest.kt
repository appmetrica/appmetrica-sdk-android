package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.verify

class NetworkServiceLocatorTest : CommonTest() {

    @get:Rule
    val networkCoreConstructionRule = constructionRule<NetworkCore>()

    private val networkServiceLocator by setUp { NetworkServiceLocator() }

    @Test
    fun initTwice() {
        NetworkServiceLocator.init()
        val first = NetworkServiceLocator.getInstance()
        NetworkServiceLocator.init()
        val second = NetworkServiceLocator.getInstance()
        assertThat(first).isNotNull().isSameAs(second)
    }

    @Test
    fun constructor() {
        val networkCore = networkServiceLocator.networkCore
        assertThat(networkCoreConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(networkCoreConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        assertThat(networkCore).isSameAs(networkCoreConstructionRule.constructionMock.constructed().first())

        verify(networkCore).start()
        verify(networkCore).name = "IAA-NC"
    }

    @Test
    fun onDestroy() {
        networkServiceLocator.onDestroy()
        verify(networkCoreConstructionRule.constructionMock.constructed().first()).stopTasks()
    }
}
