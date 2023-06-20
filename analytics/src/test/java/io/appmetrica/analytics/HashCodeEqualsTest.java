package io.appmetrica.analytics;

import android.location.Location;
import io.appmetrica.analytics.billinginterface.internal.Period;
import io.appmetrica.analytics.billinginterface.internal.ProductInfo;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.coreapi.internal.device.ScreenInfo;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;

public class HashCodeEqualsTest extends CommonTest {
    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class AllFieldsTest extends CommonTest {

        private final Class mClazz;

        public AllFieldsTest(final Class clazz) {
            mClazz = clazz;
        }

        @ParameterizedRobolectricTestRunner.Parameters(name = "class = {0}")
        public static Collection<Object[]> params() {
            return Arrays.asList(new Object[][]{
                    {io.appmetrica.analytics.impl.AppEnvironment.EnvironmentRevision.class},
                    {io.appmetrica.analytics.impl.features.FeatureDescription.class},
                    {PermissionState.class},
                    {io.appmetrica.analytics.impl.startup.StatSending.class},
                    {io.appmetrica.analytics.impl.startup.CollectingFlags.class},
                    {io.appmetrica.analytics.impl.features.FeatureDescription.class},
                    {ReferrerInfo.class},
                    {PermissionState.class},
                    {io.appmetrica.analytics.impl.BackgroundRestrictionsState.class},
                    {io.appmetrica.analytics.impl.component.clients.ClientDescription.class},
                    {io.appmetrica.analytics.impl.component.clients.ClientDescription.class},
                    {IdentifiersResult.class},
                    {io.appmetrica.analytics.impl.startup.CacheControl.class},
                    {BillingConfig.class},
                    {Period.class},
                    {ProductInfo.class},
                    {ScreenInfo.class}
            });
        }

        @Test
        public void testEqualsAndHashCode() {
            EqualsVerifier.forClass(mClazz)
                    .usingGetClass()
                    .verify();
        }
    }

    @RunWith(RobolectricTestRunner.class)
    public static class AndroidClassFieldsTest {

        @Test
        public void testReporterArgumentsEqualsAndHashCode() {
            EqualsVerifier.forClass(CommonArguments.ReporterArguments.class)
                    .usingGetClass()
                    .withPrefabValues(Location.class, new Location("GPS"), new Location("network"))
                    .verify();
        }

        @Test
        public void testComponentID() {
            EqualsVerifier.forClass(io.appmetrica.analytics.impl.component.ComponentId.class)
                    .usingGetClass()
                    .verify();
        }

        @Test
        public void testClientRepositoryTagEqualsAndHashCode() throws ClassNotFoundException {
            EqualsVerifier.forClass(Class.forName("io.appmetrica.analytics.impl.component.clients.ClientRepository$Tag"))
                    .usingGetClass()
                    .verify();
        }
    }

}
