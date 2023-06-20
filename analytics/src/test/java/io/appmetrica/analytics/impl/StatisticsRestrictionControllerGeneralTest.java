package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorageTest;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class StatisticsRestrictionControllerGeneralTest extends CommonTest {

    private final StatisticsRestrictionControllerImpl.Storage mStorage = mock(StatisticsRestrictionControllerImpl.Storage.class);
    private StatisticsRestrictionControllerImpl mController;

    @Before
    public void setUp() {
        doReturn(null).when(mStorage).readRestrictionFromMainReporter();
        mController = new StatisticsRestrictionControllerImpl(mStorage);
    }

    @Test
    public void testSaveRestrictedInMainReporter() {
        mController.setEnabledFromMainReporter(null);
        verify(mStorage).storeRestrictionFromMainReporter(anyBoolean());
    }

    @Test
    public void testReadRestrictedInMainReporter() {
        verify(mStorage).readRestrictionFromMainReporter();
    }

    @Test
    public void testEnabledFromMainReporterNull() {
        mController.setEnabledFromMainReporter(null);
        assertThat(mController.isRestrictedForReporter()).isFalse();
    }

    @Test
    public void testEnabledFromMainReporterWasFalseThenNull() {
        mController.setEnabledFromMainReporter(false);
        assertThat(mController.isRestrictedForReporter()).isTrue();
        mController.setEnabledFromMainReporter(null);
        assertThat(mController.isRestrictedForReporter()).isTrue();
    }

    @Test
    public void testEnabledFromMainReporterWasFalseThenTrue() {
        mController.setEnabledFromMainReporter(false);
        assertThat(mController.isRestrictedForReporter()).isTrue();
        mController.setEnabledFromMainReporter(true);
        assertThat(mController.isRestrictedForReporter()).isFalse();
    }

    @Test
    public void testEnabledFromMainReporterWasTrueThenFalse() {
        mController.setEnabledFromMainReporter(true);
        assertThat(mController.isRestrictedForReporter()).isFalse();
        mController.setEnabledFromMainReporter(false);
        assertThat(mController.isRestrictedForReporter()).isTrue();
    }

    @RunWith(RobolectricTestRunner.class)
    public static class StorageImplTest {

        private final PreferencesServiceDbStorage mDbStorage = PreferencesServiceDbStorageTest.createMock();

        private StatisticsRestrictionControllerImpl.StorageImpl mStorage = new StatisticsRestrictionControllerImpl.StorageImpl(
                mDbStorage
        );

        @Test
        public void testStore() {
            mStorage.storeRestrictionFromMainReporter(true);
            verify(mDbStorage).putStatisticsRestrictedFromMainReporter(true);
            verify(mDbStorage).commit();
        }

        @Test
        public void testRead() {
            doReturn(false).when(mDbStorage).getStatisticsRestrictedFromMainReporter();
            assertThat(mStorage.readRestrictionFromMainReporter()).isFalse();
            verify(mDbStorage).getStatisticsRestrictedFromMainReporter();
        }
    }

}
