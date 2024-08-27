package io.appmetrica.analytics.impl.service.migration;

import android.content.Context;
import android.util.SparseArray;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.coreutils.internal.io.FileUtils;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.MigrationManager;
import io.appmetrica.analytics.impl.SdkData;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ServiceMigrationManagerTest extends CommonTest {

    @Mock
    private PreferencesServiceDbStorage mPreferencesServiceDbStorage;
    @Mock
    private VitalCommonDataProvider vitalCommonDataProvider;
    private Context mContext = RuntimeEnvironment.getApplication().getApplicationContext();

    @Rule
    public MockedStaticRule<FileUtils> sFileUtilsRule = new MockedStaticRule<>(FileUtils.class);

    @Rule
    public MockedConstructionRule<VitalCommonDataProvider> vitalCommonDataProviderMockedRule =
        new MockedConstructionRule<>(VitalCommonDataProvider.class);

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    private ServiceMigrationManager mMigrationManager;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        GlobalServiceLocator.init(RuntimeEnvironment.getApplication());
        when(mPreferencesServiceDbStorage.putUncheckedTime(anyBoolean())).thenReturn(mPreferencesServiceDbStorage);
        when(mPreferencesServiceDbStorage.remove(anyString())).thenReturn(mPreferencesServiceDbStorage);

        mMigrationManager = spy(new ServiceMigrationManager(vitalCommonDataProvider));

        doReturn(1).when(mMigrationManager).getLastApiLevel();
        Mockito.doReturn(SdkData.CURRENT).when(mMigrationManager).getCurrentApiLevel();
    }

    @Test
    public void defaultConstructor() {
        mMigrationManager = new ServiceMigrationManager();
        mMigrationManager.checkMigration(mContext);
        verifyNoMoreInteractions(mPreferencesServiceDbStorage);
    }

    @Test
    public void getScripts() {
        Map<Integer, Class<?>> expectedScriptClassesByVersion = new HashMap<>();
        expectedScriptClassesByVersion.put(112, ServiceMigrationScriptToV112.class);
        expectedScriptClassesByVersion.put(115, ServiceMigrationScriptToV115.class);
        SparseArray<MigrationManager.MigrationScript> scripts = mMigrationManager.getScripts();
        assertThat(extractVersionsAndClasses(scripts)).containsExactlyEntriesOf(expectedScriptClassesByVersion);
    }

    private Map<Integer, Class<?>> extractVersionsAndClasses(SparseArray<MigrationManager.MigrationScript> input) {
        Map<Integer, Class<?>> versionToClassMapping = new HashMap<>(input.size());
        for (int i = 0; i < input.size(); i++) {
            versionToClassMapping.put(input.keyAt(i), input.valueAt(i).getClass());
        }
        return versionToClassMapping;
    }

    @Test
    public void getLastApiLevelHasFromOldServices() {
        mMigrationManager.checkMigration(mContext);
        verify(vitalCommonDataProvider, never()).getLastMigrationApiLevel();
        verify(mMigrationManager).getScripts();
        verify(vitalCommonDataProvider).setLastMigrationApiLevel(BuildConfig.API_LEVEL);
    }

    @Test
    public void getLastApiLevelHasFromServicePreferences() {
        mMigrationManager.checkMigration(mContext);
        verify(vitalCommonDataProvider, never()).getLastMigrationApiLevel();
        verify(mMigrationManager).getScripts();
        verify(vitalCommonDataProvider).setLastMigrationApiLevel(BuildConfig.API_LEVEL);
    }

    @Test
    public void getLastApiLevelHasFromVitalCommonDataProvider() {
        int apiLevel = BuildConfig.API_LEVEL - 1;
        doReturn(apiLevel).when(vitalCommonDataProvider).getLastMigrationApiLevel();
        mMigrationManager.checkMigration(mContext);
        verify(mMigrationManager).getScripts();
        verify(vitalCommonDataProvider).setLastMigrationApiLevel(BuildConfig.API_LEVEL);
    }

    @Test
    public void putLastApiLevel() {
        int apiLevel = BuildConfig.API_LEVEL - 1;
        mMigrationManager.putLastApiLevel(apiLevel);
        verify(vitalCommonDataProvider).setLastMigrationApiLevel(apiLevel);
        verifyNoInteractions(mPreferencesServiceDbStorage);
    }

    @Test
    public void testMigrateFromNegativeApiLevel() {
        doReturn(-1).when(mMigrationManager).getLastApiLevel();
        doReturn(-1).when(vitalCommonDataProvider).getLastMigrationApiLevel();

        mMigrationManager.checkMigration(mContext);
        verify(vitalCommonDataProvider).setLastMigrationApiLevel(SdkData.CURRENT);
    }

    @Test
    public void testMigrationSavesApiLevel() {
        doCallRealMethod().when(mMigrationManager).getLastApiLevel();
        mMigrationManager.checkMigration(mContext);
        verify(vitalCommonDataProvider).setLastMigrationApiLevel(AppMetrica.getLibraryApiLevel());
    }

    @Test
    public void testShouldNotMigrateProbablyTimeFromPastIfStartupOffsetDoesNotExist() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new Long((Integer) invocation.getArgument(0));
            }
        }).when(mPreferencesServiceDbStorage).getServerTimeOffset(anyInt());

        mMigrationManager.checkMigration(mContext);
        verify(mPreferencesServiceDbStorage, never()).putUncheckedTime(anyBoolean());
    }
}
