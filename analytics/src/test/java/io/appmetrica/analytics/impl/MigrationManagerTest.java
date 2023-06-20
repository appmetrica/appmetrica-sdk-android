package io.appmetrica.analytics.impl;

import android.content.Context;
import android.util.SparseArray;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class MigrationManagerTest extends CommonTest {

    @Mock
    private MigrationManager.MigrationScript mMigrationScript;

    private Context mContext = RuntimeEnvironment.getApplication().getApplicationContext();

    private MigrationManager mMigrationManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testShouldMigrate() {
        mMigrationManager = createMigrationManager(1);
        mMigrationManager.checkMigration(mContext);
        verify(mMigrationScript).run(mContext);
        assertThat(mMigrationManager.getLastApiLevel()).isEqualTo(SdkData.CURRENT);
    }

    @Test
    public void testShouldNotMigrateFromMinus1() {
        mMigrationManager = createMigrationManager(-1);
        mMigrationManager.checkMigration(mContext);
        verify(mMigrationScript, never()).run(mContext);
        assertThat(mMigrationManager.getLastApiLevel()).isEqualTo(SdkData.CURRENT);
    }

    @Test
    public void testShouldNotMigrateFromHigherLevel() {
        mMigrationManager = createMigrationManager(SdkData.CURRENT + 10);
        mMigrationManager.checkMigration(mContext);
        verify(mMigrationScript, never()).run(mContext);
        assertThat(mMigrationManager.getLastApiLevel()).isEqualTo(SdkData.CURRENT);
    }

    private MigrationManager createMigrationManager(final int apiLevel) {
        return new MigrationManager() {

            private int mApiLevel = apiLevel;

            @Override
            SparseArray<MigrationScript> getScripts() {
                SparseArray<MigrationScript> result = new SparseArray<MigrationScript>();
                result.put(SdkData.CURRENT, mMigrationScript);
                return result;
            }

            @Override
            protected int getLastApiLevel() {
                return mApiLevel;
            }

            @Override
            protected void putLastApiLevel(int apiLevel) {
                mApiLevel = apiLevel;
            }
        };
    }

}
