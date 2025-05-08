package io.appmetrica.analytics.networktasks.impl

import io.appmetrica.analytics.testutils.CommonTest
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.Test

class CustomHashCodeEqualsTest : CommonTest() {

    @Test
    fun networkCoreQueueTaskEntryEqualsAndHashCode() {
        EqualsVerifier
            .forClass(Class.forName("io.appmetrica.analytics.networktasks.internal.NetworkCore\$QueueTaskEntry"))
            .usingGetClass()
            .withNonnullFields("taskDescription")
            .withIgnoredFields("networkTask")
            .verify()
    }
}
