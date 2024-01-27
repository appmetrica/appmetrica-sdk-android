package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class NetworkServiceLocatorTest {

    @get:Rule
    val newNetworkCore = MockedConstructionRule(NetworkCore::class.java)

    @Before
    fun setUp() {
        NetworkServiceLocator.destroy()
    }

    @After
    fun tearDown() {
        NetworkServiceLocator.getInstance()?.onDestroy()
        NetworkServiceLocator.destroy()
    }

    @Test
    fun getInstanceBeforeInit() {
        assertThat(NetworkServiceLocator.getInstance()).isNull()
    }

    @Test
    fun getNetworkCoreBeforeInitAsync() {
        NetworkServiceLocator.init()
        assertThat(NetworkServiceLocator.getInstance().networkCore).isNull()
    }

    @Test
    fun initAsyncTwice() {
        NetworkServiceLocator.init()
        NetworkServiceLocator.getInstance().initAsync()
        val networkCore = NetworkServiceLocator.getInstance().networkCore
        assertThat(networkCore).isNotNull
        verify(networkCore).start()
        verify(networkCore).name = "YMM-NC"
        NetworkServiceLocator.getInstance().initAsync()
        assertThat(NetworkServiceLocator.getInstance().networkCore).isSameAs(networkCore)
    }

    @Test
    fun onDestroyWithoutInitAsync() {
        NetworkServiceLocator.init()
        NetworkServiceLocator.getInstance().onDestroy()
        assertThat(NetworkServiceLocator.getInstance().networkCore).isNull()
    }

    @Test
    fun onDestroyWithInitAsync() {
        NetworkServiceLocator.init()
        NetworkServiceLocator.getInstance().initAsync()
        NetworkServiceLocator.getInstance().onDestroy()
        assertThat(NetworkServiceLocator.getInstance().networkCore).isNotNull
        verify(newNetworkCore.constructionMock.constructed()[0]).onDestroy()
    }
}
