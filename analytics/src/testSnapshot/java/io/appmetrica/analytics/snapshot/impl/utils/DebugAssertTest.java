package io.appmetrica.analytics.snapshot.impl.utils;

import android.content.Context;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.StorageType;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.utils.DebugAssert;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DebugAssertTest extends CommonTest {

    @Mock
    private IKeyValueTableDbHelper serviceKeyValueHelper;
    private Context context;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();
    @Rule
    public final ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        when(GlobalServiceLocator.getInstance().getStorageFactory().getServicePreferenceDbHelperForMigration(context))
            .thenReturn(serviceKeyValueHelper);
    }

    @Test
    public void autoInapp() {
        DebugAssert.assertMigrated(context, StorageType.AUTO_INAPP);
    }

    @Test
    public void clientVersionIsTheSame() {
        when(ClientServiceLocator.getInstance().getClientMigrationApiLevel(any()))
            .thenReturn((long) BuildConfig.API_LEVEL);

        DebugAssert.assertMigrated(context, StorageType.CLIENT);
    }

    @Test
    public void clientVersionIsNotTheSame() {
        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Client migration is not checked");

        when(ClientServiceLocator.getInstance().getClientMigrationApiLevel(any()))
            .thenReturn((long) BuildConfig.API_LEVEL - 1);

        DebugAssert.assertMigrated(context, StorageType.CLIENT);
    }

    @Test
    public void serviceVersionIsTheSame() {
        final VitalCommonDataProvider vitalCommonDataProvider = mock(VitalCommonDataProvider.class);
        when(GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getCommonDataProviderForMigration())
                .thenReturn(vitalCommonDataProvider);
        when(vitalCommonDataProvider.getLastMigrationApiLevel()).thenReturn(BuildConfig.API_LEVEL);
        DebugAssert.assertMigrated(context, StorageType.SERVICE);
    }

    @Test
    public void serviceVersionIsNotTheSame() {
        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Service migration is not checked");
        final VitalCommonDataProvider vitalCommonDataProvider = mock(VitalCommonDataProvider.class);
        when(GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getCommonDataProviderForMigration())
                .thenReturn(vitalCommonDataProvider);
        when(vitalCommonDataProvider.getLastMigrationApiLevel()).thenReturn(BuildConfig.API_LEVEL - 1);
        DebugAssert.assertMigrated(context, StorageType.SERVICE);
    }
}
