package io.appmetrica.analytics.impl.proxy

import android.content.Context
import android.location.Location
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Random

internal class AppMetricaFacadeProviderTest : CommonTest() {

    private val appMetricaFacade: AppMetricaFacade = mock()
    private var context: Context = mock()

    @get:Rule
    val appMetricaFacadeStaticRule = staticRule<AppMetricaFacade>()

    private val provider: AppMetricaFacadeProvider by setUp { AppMetricaFacadeProvider() }

    @Test
    fun peekInitializedImpl() {
        whenever(AppMetricaFacade.peekInstance()).thenReturn(appMetricaFacade)
        assertThat(provider.peekInitializedImpl()).isEqualTo(appMetricaFacade)
    }

    @Test
    fun peekInitializedImplNull() {
        whenever(AppMetricaFacade.peekInstance()).thenReturn(null)
        assertThat(provider.peekInitializedImpl()).isNull()
    }

    @Test
    fun getInitializedImpl() {
        whenever(AppMetricaFacade.getInstance(context)).thenReturn(appMetricaFacade)
        assertThat(provider.getInitializedImpl(context)).isEqualTo(appMetricaFacade)
    }

    @Test
    fun isInitializedForAppForTrue() {
        whenever(AppMetricaFacade.isInitializedForApp()).thenReturn(true)
        assertThat(provider.isInitializedForApp).isTrue()
    }

    @Test
    fun isInitializedForAppForFalse() {
        whenever(AppMetricaFacade.isInitializedForApp()).thenReturn(false)
        assertThat(provider.isInitializedForApp).isFalse()
    }

    @Test
    fun isActivatedTrue() {
        whenever(AppMetricaFacade.isActivated()).thenReturn(true)
        assertThat(provider.isActivated).isTrue()
    }

    @Test
    fun isActivatedFalse() {
        whenever(AppMetricaFacade.isActivated()).thenReturn(false)
        assertThat(provider.isActivated).isFalse()
    }

    @Test
    fun markActivated() {
        provider.markActivated()
        appMetricaFacadeStaticRule.staticMock.verify { AppMetricaFacade.markActivated() }
    }

    @Test
    fun setLocation() {
        val location = Mockito.mock(Location::class.java)
        provider.setLocation(location)
        appMetricaFacadeStaticRule.staticMock.verify { AppMetricaFacade.setLocation(location) }
    }

    @Test
    fun setLocationTracking() {
        val locationTracking = Random().nextBoolean()
        provider.setLocationTracking(locationTracking)
        appMetricaFacadeStaticRule.staticMock.verify { AppMetricaFacade.setLocationTracking(locationTracking) }
    }

    @Test
    fun setAdvIdentifiersTracking() {
        provider.setAdvIdentifiersTracking(true)
        appMetricaFacadeStaticRule.staticMock.verify { AppMetricaFacade.setAdvIdentifiersTracking(true) }
    }

    @Test
    fun setDataSendingEnabled() {
        val dataSendingEnabled = Random().nextBoolean()
        provider.setDataSendingEnabled(dataSendingEnabled)
        appMetricaFacadeStaticRule.staticMock.verify {
            AppMetricaFacade.setDataSendingEnabled(
                dataSendingEnabled
            )
        }
    }

    @Test
    fun putErrorEnvironmentValue() {
        val key = "key"
        val value = "value"
        provider.putErrorEnvironmentValue(key, value)
        appMetricaFacadeStaticRule.staticMock.verify {
            AppMetricaFacade.putErrorEnvironmentValue(
                key,
                value
            )
        }
    }

    @Test
    fun putAppEnvironmentValue() {
        val key = "key"
        val value = "value"
        provider.putAppEnvironmentValue(key, value)
        appMetricaFacadeStaticRule.staticMock.verify { AppMetricaFacade.putAppEnvironmentValue(key, value) }
    }

    @Test
    fun clearAppEnvironment() {
        provider.clearAppEnvironment()
        appMetricaFacadeStaticRule.staticMock.verify { AppMetricaFacade.clearAppEnvironment() }
    }

    @Test
    fun setUserProfileID() {
        val userProfileID = "user_profile_id"
        provider.setUserProfileID(userProfileID)
        appMetricaFacadeStaticRule.staticMock.verify { AppMetricaFacade.setUserProfileID(userProfileID) }
    }

    @Test
    fun addAutoCollectedDataSubscriber() {
        val subscriber = "Subscriber"
        provider.addAutoCollectedDataSubscriber(subscriber)
        appMetricaFacadeStaticRule.staticMock.verify { AppMetricaFacade.addAutoCollectedDataSubscriber(subscriber) }
    }
}
