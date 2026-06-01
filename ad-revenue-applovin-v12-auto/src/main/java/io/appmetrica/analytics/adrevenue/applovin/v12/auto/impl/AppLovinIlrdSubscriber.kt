package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl

import android.os.Bundle
import com.applovin.communicator.AppLovinCommunicatorMessage
import com.applovin.communicator.AppLovinCommunicatorSubscriber
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class AppLovinIlrdSubscriber(
    private val handler: (id: String, data: Bundle) -> Unit
) : AppLovinCommunicatorSubscriber {

    private val tag = "[AppLovinIlrdSubscriber]"

    override fun getCommunicatorId(): String = Constants.COMMUNICATOR_ID

    override fun onMessageReceived(message: AppLovinCommunicatorMessage) {
        if (message.topic != Constants.TOPIC) {
            DebugLogger.info(tag, "Skipping message with unexpected topic: ${message.topic}")
            return
        }
        val data = message.messageData ?: run {
            DebugLogger.info(tag, "Skipping message: messageData is null")
            return
        }
        val id = data.getString("id") ?: run {
            DebugLogger.info(tag, "Skipping message: id is missing")
            return
        }
        DebugLogger.info(tag, "Received ILRD message: id=$id")
        handler(id, data)
    }
}
