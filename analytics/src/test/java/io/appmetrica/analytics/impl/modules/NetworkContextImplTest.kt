package io.appmetrica.analytics.impl.modules

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.network.ExecutionPolicyBasedOnConnection
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkContextImplTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val executionPolicyMockedRule = MockedConstructionRule(ExecutionPolicyBasedOnConnection::class.java)

    private val context = mock<Context>()

    private lateinit var networkContextImpl: NetworkContextImpl

    @Before
    fun setUp() {
        networkContextImpl = NetworkContextImpl(context)
    }

    @Test
    fun sslSocketFactoryProvider() {
        assertThat(networkContextImpl.sslSocketFactoryProvider)
            .isEqualTo(GlobalServiceLocator.getInstance().sslSocketFactoryProvider)
    }

    @Test
    fun executionPolicy() {
        assertThat(networkContextImpl.executionPolicy)
            .isEqualTo(executionPolicyMockedRule.constructionMock.constructed()[0])
        assertThat(executionPolicyMockedRule.constructionMock.constructed()).hasSize(1)
        assertThat(executionPolicyMockedRule.argumentInterceptor.flatArguments()).containsExactly(context)
    }
}
