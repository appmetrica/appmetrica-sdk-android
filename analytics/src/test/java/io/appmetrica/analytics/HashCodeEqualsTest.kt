package io.appmetrica.analytics

import android.location.Location
import io.appmetrica.analytics.billinginterface.internal.Period
import io.appmetrica.analytics.billinginterface.internal.ProductInfo
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.coreapi.internal.device.ScreenInfo
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState
import io.appmetrica.analytics.impl.AppEnvironment.EnvironmentRevision
import io.appmetrica.analytics.impl.BackgroundRestrictionsState
import io.appmetrica.analytics.impl.component.CommonArguments.ReporterArguments
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.clients.ClientDescription
import io.appmetrica.analytics.impl.features.FeatureDescription
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.startup.CacheControl
import io.appmetrica.analytics.impl.startup.CollectingFlags
import io.appmetrica.analytics.impl.startup.StatSending
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.testutils.CommonTest
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class AllFieldsTest(private val clazz: Class<*>) : CommonTest() {

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "class = {0}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
                arrayOf(EnvironmentRevision::class.java),
                arrayOf(FeatureDescription::class.java),
                arrayOf(PermissionState::class.java),
                arrayOf(StatSending::class.java),
                arrayOf(CollectingFlags::class.java),
                arrayOf(FeatureDescription::class.java),
                arrayOf(ReferrerInfo::class.java),
                arrayOf(PermissionState::class.java),
                arrayOf(BackgroundRestrictionsState::class.java),
                arrayOf(ClientDescription::class.java),
                arrayOf(ClientDescription::class.java),
                arrayOf(IdentifiersResult::class.java),
                arrayOf(CacheControl::class.java),
                arrayOf(BillingConfig::class.java),
                arrayOf(Period::class.java),
                arrayOf(ProductInfo::class.java),
                arrayOf(ScreenInfo::class.java),
                arrayOf(StartupParamsItem::class.java)
            )
    }

    @Test
    fun equalsAndHashCode() {
        EqualsVerifier.forClass(clazz)
            .usingGetClass()
            .verify()
    }
}

@RunWith(RobolectricTestRunner::class)
class AndroidClassFieldsTest {

    @Test
    fun reporterArgumentsEqualsAndHashCode() {
        EqualsVerifier.forClass(ReporterArguments::class.java)
            .usingGetClass()
            .withPrefabValues(Location::class.java, Location("GPS"), Location("network"))
            .verify()
    }

    @Test
    fun componentIdEqualsAndHashCode() {
        EqualsVerifier.forClass(ComponentId::class.java)
            .usingGetClass()
            .verify()
    }

    @Test
    @Throws(ClassNotFoundException::class)
    fun clientRepositoryTagEqualsAndHashCode() {
        EqualsVerifier.forClass(
            Class.forName("io.appmetrica.analytics.impl.component.clients.ClientRepository\$Tag")
        ).usingGetClass().verify()
    }
}
