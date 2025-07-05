package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class NetworkServiceLocatorTest : CommonTest() {

    @get:Rule
    val networkCoreConstructionRule = constructionRule<NetworkCore>()

    private val executionPolicy: IExecutionPolicy = mock()

    private val networkServiceLocator by setUp { NetworkServiceLocator(executionPolicy) }

    @Test
    fun initTwice() {
        NetworkServiceLocator.init(executionPolicy)
        val first = NetworkServiceLocator.getInstance()
        NetworkServiceLocator.init(executionPolicy)
        val second = NetworkServiceLocator.getInstance()
        assertThat(first).isNotNull().isSameAs(second)
    }

    @Test
    fun constructor() {
        val networkCore = networkServiceLocator.networkCore
        assertThat(networkCoreConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(networkCoreConstructionRule.argumentInterceptor.flatArguments()).containsExactly(executionPolicy)
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
