package io.appmetrica.analytics.networktasks.internal

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.network.internal.NetworkClientServiceLocator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkServiceLocatorTest : CommonTest() {

    @get:Rule
    val networkCoreConstructionRule = constructionRule<NetworkCore>()

    private val context: Context = mock()

    private val executionPolicy: IExecutionPolicy = mock()

    @get:Rule
    val networkClientServiceLocatorRule = staticRule<NetworkClientServiceLocator>()

    private val networkServiceLocator by setUp { NetworkServiceLocator(executionPolicy) }

    @Test
    fun init() {
        NetworkServiceLocator.init(context, executionPolicy)
        networkClientServiceLocatorRule.staticMock.verify {
            NetworkClientServiceLocator.init(context)
        }
    }

    @Test
    fun initTwice() {
        NetworkServiceLocator.init(context, executionPolicy)
        val first = NetworkServiceLocator.getInstance()
        NetworkServiceLocator.init(context, executionPolicy)
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
