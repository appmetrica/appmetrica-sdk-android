package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import java.util.concurrent.Executor
import javax.net.ssl.SSLSocketFactory

class NetworkTaskTest {

    private val executor = mock<Executor>()
    private val requestDataHolder = mock<RequestDataHolder>()
    private val responseDataHolder = mock<ResponseDataHolder>()
    private val fullUrlFormer = mock<FullUrlFormer<*>>()
    private val retryPolicyConfig = mock<RetryPolicyConfig>()
    private val sslSocketFactory = mock<SSLSocketFactory>()
    private val description = "some description"
    private val connectionBasedExecutionPolicy = mock<IExecutionPolicy>()
    private val exponentialBackoffPolicy = mock<ExponentialBackoffPolicy>()
    private val underlyingTask = mock<UnderlyingNetworkTask> {
        on { this.fullUrlFormer } doReturn fullUrlFormer
        on { this.requestDataHolder } doReturn requestDataHolder
        on { this.responseDataHolder } doReturn responseDataHolder
        on { this.retryPolicyConfig } doReturn retryPolicyConfig
        on { this.sslSocketFactory } doReturn sslSocketFactory
        on { this.description() } doReturn description
    }
    private val firstShouldTryNextHostCondition = mock<NetworkTask.ShouldTryNextHostCondition>()
    private val secondShouldTryNextHostCondition = mock<NetworkTask.ShouldTryNextHostCondition>()
    private val networkTask = NetworkTask(
        executor,
        connectionBasedExecutionPolicy,
        exponentialBackoffPolicy,
        underlyingTask,
        listOf(firstShouldTryNextHostCondition, secondShouldTryNextHostCondition),
        "userAgent"
    )

    @Test
    fun url() {
        val url = "some url"
        stubbing(fullUrlFormer) {
            on { this.url } doReturn url
        }
        assertThat(networkTask.url).isEqualTo(url)
    }

    @Test
    fun requestDataHolder() {
        assertThat(networkTask.requestDataHolder).isEqualTo(requestDataHolder)
    }

    @Test
    fun responseDataHolder() {
        assertThat(networkTask.responseDataHolder).isEqualTo(responseDataHolder)
    }

    @Test
    fun description() {
        assertThat(networkTask.description()).isEqualTo(description)
    }

    @Test
    fun executor() {
        assertThat(networkTask.executor).isEqualTo(executor)
    }

    @Test
    fun retryPolicyConfig() {
        assertThat(networkTask.retryPolicyConfig).isEqualTo(retryPolicyConfig)
    }

    @Test
    fun connectionExecutionPolicy() {
        assertThat(networkTask.connectionExecutionPolicy).isEqualTo(connectionBasedExecutionPolicy)
    }

    @Test
    fun exponentialBackoffPolicy() {
        assertThat(networkTask.exponentialBackoffPolicy).isEqualTo(exponentialBackoffPolicy)
    }

    @Test
    fun sslSocketFactory() {
        assertThat(networkTask.sslSocketFactory).isEqualTo(sslSocketFactory)
    }

    @Test
    fun shouldTryNextHostNoMoreHosts() {
        val code = 87687
        stubbing(responseDataHolder) {
            on { this.responseCode } doReturn code
        }
        stubbing(fullUrlFormer) {
            on { this.hasMoreHosts() } doReturn false
        }
        stubbing(firstShouldTryNextHostCondition) {
            on { this.shouldTryNextHost(code) } doReturn true
        }
        stubbing(secondShouldTryNextHostCondition) {
            on { this.shouldTryNextHost(code) } doReturn true
        }
        assertThat(networkTask.shouldTryNextHost()).isFalse
    }

    @Test
    fun shouldTryNextHostFirstConditionIsFalse() {
        val code = 87687
        stubbing(responseDataHolder) {
            on { this.responseCode } doReturn code
        }
        stubbing(fullUrlFormer) {
            on { this.hasMoreHosts() } doReturn true
        }
        stubbing(firstShouldTryNextHostCondition) {
            on { this.shouldTryNextHost(code) } doReturn false
        }
        stubbing(secondShouldTryNextHostCondition) {
            on { this.shouldTryNextHost(code) } doReturn true
        }
        assertThat(networkTask.shouldTryNextHost()).isFalse
    }

    @Test
    fun shouldTryNextHostSecondConditionIsFalse() {
        val code = 87687
        stubbing(responseDataHolder) {
            on { this.responseCode } doReturn code
        }
        stubbing(fullUrlFormer) {
            on { this.hasMoreHosts() } doReturn true
        }
        stubbing(firstShouldTryNextHostCondition) {
            on { this.shouldTryNextHost(code) } doReturn true
        }
        stubbing(secondShouldTryNextHostCondition) {
            on { this.shouldTryNextHost(code) } doReturn false
        }
        assertThat(networkTask.shouldTryNextHost()).isFalse
    }

    @Test
    fun shouldTryNextHostShould() {
        val code = 87687
        stubbing(responseDataHolder) {
            on { this.responseCode } doReturn code
        }
        stubbing(fullUrlFormer) {
            on { this.hasMoreHosts() } doReturn true
        }
        stubbing(firstShouldTryNextHostCondition) {
            on { this.shouldTryNextHost(code) } doReturn true
        }
        stubbing(secondShouldTryNextHostCondition) {
            on { this.shouldTryNextHost(code) } doReturn true
        }
        assertThat(networkTask.shouldTryNextHost()).isTrue
    }

    @Test
    fun shouldTryNextHostStateIsRemoved() {
        val code = 87687
        stubbing(responseDataHolder) {
            on { this.responseCode } doReturn code
        }
        stubbing(fullUrlFormer) {
            on { this.hasMoreHosts() } doReturn true
        }
        stubbing(firstShouldTryNextHostCondition) {
            on { this.shouldTryNextHost(code) } doReturn false
        }
        stubbing(secondShouldTryNextHostCondition) {
            on { this.shouldTryNextHost(code) } doReturn true
        }
        networkTask.onTaskAdded()
        networkTask.onTaskRemoved()
        assertThat(networkTask.shouldTryNextHost()).isFalse
    }

    @Test
    fun shouldTryNextHostStateIsFinished() {
        val code = 87687
        stubbing(responseDataHolder) {
            on { this.responseCode } doReturn code
        }
        stubbing(fullUrlFormer) {
            on { this.hasMoreHosts() } doReturn true
        }
        stubbing(firstShouldTryNextHostCondition) {
            on { this.shouldTryNextHost(code) } doReturn false
        }
        stubbing(secondShouldTryNextHostCondition) {
            on { this.shouldTryNextHost(code) } doReturn true
        }
        networkTask.onTaskAdded()
        networkTask.onTaskFinished()
        assertThat(networkTask.shouldTryNextHost()).isFalse
    }
}
