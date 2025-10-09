package io.appmetrica.analytics.snapshot.impl.utils;

import android.content.Context;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.StorageType;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.impl.utils.DebugAssert;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DebugAssertTest extends CommonTest {

    @Mock
    private IKeyValueTableDbHelper serviceKeyValueHelper;
    @Mock
    private IKeyValueTableDbHelper clientKeyValueHelper;
    private Context context;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        DatabaseStorageFactory.getInstance(context).setServicePreferencesHelperForMigration(serviceKeyValueHelper);
        DatabaseStorageFactory.getInstance(context).setClientDbHelperForMigration(clientKeyValueHelper);
    }

    @Test
    public void autoInapp() {
        DebugAssert.assertMigrated(context, StorageType.AUTO_INAPP);
    }

    @Test
    public void clientVersionIsTheSame() {
        try (MockedConstruction<PreferencesClientDbStorage> ignored = Mockito.mockConstruction(PreferencesClientDbStorage.class, new MockedConstruction.MockInitializer<PreferencesClientDbStorage>() {
            @Override
            public void prepare(PreferencesClientDbStorage mock, MockedConstruction.Context context) {
                if (context.arguments().get(0) == clientKeyValueHelper) {
                    when(mock.getClientApiLevel(anyLong())).thenReturn((long) BuildConfig.API_LEVEL);
                }
            }
        })) {
            DebugAssert.assertMigrated(context, StorageType.CLIENT);
        }
    }

    @Test
    public void clientVersionIsNotTheSame() {
        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Client migration is not checked");

        try (MockedConstruction<PreferencesClientDbStorage> ignored = Mockito.mockConstruction(PreferencesClientDbStorage.class, new MockedConstruction.MockInitializer<PreferencesClientDbStorage>() {
            @Override
            public void prepare(PreferencesClientDbStorage mock, MockedConstruction.Context context) {
                if (context.arguments().get(0) == clientKeyValueHelper) {
                    when(mock.getClientApiLevel(anyLong())).thenReturn((long) BuildConfig.API_LEVEL - 1);
                }
            }
        })) {
            DebugAssert.assertMigrated(context, StorageType.CLIENT);
        }
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
