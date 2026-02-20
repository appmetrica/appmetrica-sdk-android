package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorageTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class DataSendingRestrictionControllerStorageImplTest {

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
