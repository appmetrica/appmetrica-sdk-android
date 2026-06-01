package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl

import android.os.Bundle
import com.applovin.communicator.AppLovinCommunicatorMessage
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class AppLovinIlrdSubscriberTest : CommonTest() {

    private val receivedIds = mutableListOf<String>()
    private val receivedBundles = mutableListOf<Bundle>()

    private val subscriber = AppLovinIlrdSubscriber { id, bundle ->
        receivedIds.add(id)
        receivedBundles.add(bundle)
    }

    @Test
    fun getCommunicatorId() {
        assertThat(subscriber.getCommunicatorId()).isEqualTo(Constants.COMMUNICATOR_ID)
    }

    @Test
    fun onMessageReceivedDeliversToHandler() {
        val bundle: Bundle = mock {
            on { getString("id") } doReturn "test-id-1"
        }
        val message: AppLovinCommunicatorMessage = mock {
            on { topic } doReturn Constants.TOPIC
            on { messageData } doReturn bundle
        }

        subscriber.onMessageReceived(message)

        assertThat(receivedIds).isEqualTo(listOf("test-id-1"))
        assertThat(receivedBundles).isEqualTo(listOf(bundle))
    }

    @Test
    fun onMessageReceivedIgnoresWrongTopic() {
        val bundle: Bundle = mock {
            on { getString("id") } doReturn "test-id-2"
        }
        val message: AppLovinCommunicatorMessage = mock {
            on { topic } doReturn "other_topic"
            on { messageData } doReturn bundle
        }

        subscriber.onMessageReceived(message)

        assertThat(receivedIds).isEmpty()
    }

    @Test
    fun onMessageReceivedIgnoresWhenIdMissing() {
        val bundle: Bundle = mock {
            on { getString("id") } doReturn null
        }
        val message: AppLovinCommunicatorMessage = mock {
            on { topic } doReturn Constants.TOPIC
            on { messageData } doReturn bundle
        }

        subscriber.onMessageReceived(message)

        assertThat(receivedIds).isEmpty()
    }

    @Test
    fun onMessageReceivedIgnoresWhenMessageDataIsNull() {
        val message: AppLovinCommunicatorMessage = mock {
            on { topic } doReturn Constants.TOPIC
            on { messageData } doReturn null
        }

        subscriber.onMessageReceived(message)

        assertThat(receivedIds).isEmpty()
    }
}
