package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl

import android.content.Context
import com.applovin.communicator.AppLovinCommunicator
import com.applovin.communicator.AppLovinCommunicatorSubscriber
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AppLovinIlrdAdapterTest : CommonTest() {

    private val context: Context = mock()
    private val reporter: AppLovinIlrdReporter = mock()
    private val communicator: AppLovinCommunicator = mock()

    @get:Rule
    val communicatorRule = staticRule<AppLovinCommunicator> {
        on { AppLovinCommunicator.getInstance(any()) } doReturn communicator
    }

    @get:Rule
    val subscriberRule = constructionRule<AppLovinIlrdSubscriber>()

    private val adapter by setUp { AppLovinIlrdAdapter(context, reporter) }

    @Test
    fun registerSubscriberSubscribesAndReturnsTrue() {
        val result = adapter.registerSubscriber()

        assertThat(result).isTrue()
        assertThat(subscriberRule.constructionMock.constructed()).hasSize(1)
        verify(communicator).subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun registerSubscriberIsIdempotent() {
        adapter.registerSubscriber()
        val result = adapter.registerSubscriber()

        assertThat(result).isTrue()
        assertThat(subscriberRule.constructionMock.constructed()).hasSize(1)
        verify(communicator).subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun unregisterSubscriberWhenNotRegisteredReturnsTrueAndDoesNotUnsubscribe() {
        val result = adapter.unregisterSubscriber()

        assertThat(result).isTrue()
        verify(communicator, never()).unsubscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }

    @Test
    fun unregisterSubscriberAfterRegisterUnsubscribesAndReturnsTrue() {
        adapter.registerSubscriber()
        val subscriber = subscriberRule.constructionMock.constructed().first()

        val result = adapter.unregisterSubscriber()

        assertThat(result).isTrue()
        verify(communicator).unsubscribe(subscriber, Constants.TOPIC)
    }

    @Test
    fun registerSubscriberReturnsFalseIfSubscribeThrows() {
        whenever(communicator.subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>()))
            .doThrow(RuntimeException("subscribe failed"))

        val result = adapter.registerSubscriber()

        assertThat(result).isFalse()
        assertThat(subscriberRule.constructionMock.constructed()).hasSize(1)
    }

    @Test
    fun unregisterSubscriberReturnsFalseAndKeepsSubscriberIfUnsubscribeThrows() {
        adapter.registerSubscriber()
        whenever(communicator.unsubscribe(any<AppLovinCommunicatorSubscriber>(), any<String>()))
            .doThrow(RuntimeException("unsubscribe failed"))

        val result = adapter.unregisterSubscriber()

        assertThat(result).isFalse()
        // subscriber is retained — next registerSubscriber call is idempotent
        val result2 = adapter.registerSubscriber()
        assertThat(result2).isTrue()
        assertThat(subscriberRule.constructionMock.constructed()).hasSize(1)
    }

    @Test
    fun registerAfterUnregister() {
        adapter.registerSubscriber()
        adapter.unregisterSubscriber()
        adapter.registerSubscriber()

        assertThat(subscriberRule.constructionMock.constructed()).hasSize(2)
        verify(communicator, times(2))
            .subscribe(any<AppLovinCommunicatorSubscriber>(), any<String>())
    }
}
