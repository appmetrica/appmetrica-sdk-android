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
public class DataSendingRestrictionControllerGeneralTest extends CommonTest {

    private final DataSendingRestrictionControllerImpl.Storage mStorage = mock(DataSendingRestrictionControllerImpl.Storage.class);
    private DataSendingRestrictionControllerImpl mController;

    @Before
    public void setUp() {
        doReturn(null).when(mStorage).readRestrictionFromMainReporter();
        mController = new DataSendingRestrictionControllerImpl(mStorage);
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
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void testEnabledFromMainReporterWasFalseThenNull() {
        mController.setEnabledFromMainReporter(false);
        assertThat(mController.isRestrictedForSdk()).isTrue();
        mController.setEnabledFromMainReporter(null);
        assertThat(mController.isRestrictedForSdk()).isTrue();
    }

    @Test
    public void testEnabledFromMainReporterWasFalseThenTrue() {
        mController.setEnabledFromMainReporter(false);
        assertThat(mController.isRestrictedForSdk()).isTrue();
        mController.setEnabledFromMainReporter(true);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void testEnabledFromMainReporterWasTrueThenFalse() {
        mController.setEnabledFromMainReporter(true);
        assertThat(mController.isRestrictedForSdk()).isFalse();
        mController.setEnabledFromMainReporter(false);
        assertThat(mController.isRestrictedForSdk()).isTrue();
    }

    @RunWith(RobolectricTestRunner.class)
    public static class StorageImplTest {

        private final PreferencesServiceDbStorage mDbStorage = PreferencesServiceDbStorageTest.createMock();

        private DataSendingRestrictionControllerImpl.StorageImpl mStorage = new DataSendingRestrictionControllerImpl.StorageImpl(
                mDbStorage
        );

        @Test
        public void testStore() {
            mStorage.storeRestrictionFromMainReporter(true);
            verify(mDbStorage).putDataSendingRestrictedFromMainReporter(true);
            verify(mDbStorage).commit();
        }

        @Test
        public void testRead() {
            doReturn(false).when(mDbStorage).getDataSendingRestrictedFromMainReporter();
            assertThat(mStorage.readRestrictionFromMainReporter()).isFalse();
            verify(mDbStorage).getDataSendingRestrictedFromMainReporter();
        }
    }

}
