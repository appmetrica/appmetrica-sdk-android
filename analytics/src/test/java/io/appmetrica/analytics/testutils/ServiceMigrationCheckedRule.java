package io.appmetrica.analytics.testutils;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import org.junit.rules.ExternalResource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceMigrationCheckedRule extends ExternalResource {

    @Nullable
    private final GlobalServiceLocatorRule globalServiceLocatorRule;

    public ServiceMigrationCheckedRule() {
        this(false);
    }

    public ServiceMigrationCheckedRule(boolean useGlobalServiceLocatorRule) {
        if (useGlobalServiceLocatorRule) {
            globalServiceLocatorRule = new GlobalServiceLocatorRule();
        } else {
            globalServiceLocatorRule = null;
        }
    }

    @Override
    protected void before() throws Throwable {
        if (globalServiceLocatorRule != null) {
            globalServiceLocatorRule.before();
        }
        VitalCommonDataProvider vitalCommonDataProvider = mock(VitalCommonDataProvider.class);
        when(vitalCommonDataProvider.getLastMigrationApiLevel()).thenReturn(BuildConfig.API_LEVEL);
        when(GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getCommonDataProviderForMigration())
                .thenReturn(vitalCommonDataProvider);
    }

    @Override
    protected void after() {
        if (globalServiceLocatorRule != null) {
            globalServiceLocatorRule.after();
        }
    }
}
