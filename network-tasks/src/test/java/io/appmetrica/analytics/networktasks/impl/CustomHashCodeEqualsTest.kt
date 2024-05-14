package io.appmetrica.analytics.networktasks.impl

import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.Test

class CustomHashCodeEqualsTest {

    @Test
    fun testNetworkCoreQueueTaskEntryEqualsAndHashCode() {
        EqualsVerifier.forClass(Class.forName("io.appmetrica.analytics.networktasks.internal.NetworkCore\$QueueTaskEntry"))
            .usingGetClass()
            .withNonnullFields("taskDescription")
            .withIgnoredFields("networkTask")
            .verify()
    }
}
