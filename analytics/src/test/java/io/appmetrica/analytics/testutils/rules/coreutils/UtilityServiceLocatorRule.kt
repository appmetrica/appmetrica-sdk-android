package io.appmetrica.analytics.testutils.rules.coreutils

import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceLocator
import org.junit.rules.ExternalResource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class UtilityServiceLocatorRule : ExternalResource() {

    private lateinit var originalInstance: UtilityServiceLocator

    private val mockedInstance = mock<UtilityServiceLocator> {
        on { firstExecutionService } doReturn mock()
        on { activationBarrier } doReturn mock()
    }

    override fun before() {
        originalInstance = UtilityServiceLocator.instance
        UtilityServiceLocator.instance = mockedInstance
    }

    override fun after() {
        UtilityServiceLocator.instance = originalInstance
    }
}
