package io.appmetrica.analytics.impl.db.connectors;

import android.content.Context;
import io.appmetrica.analytics.impl.db.TablesManager;
import io.appmetrica.analytics.impl.utils.concurrency.FileLocker;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

@RunWith(RobolectricTestRunner.class)
public class LockedOnFileDBConnectorTest extends CommonTest {

    @Mock
    private FileLocker mDatabaseFileLock;
    @Mock
    private TablesManager tablesManager;

    private final String mDbName = "db_name";
    private Context mContext;
    private LockedOnFileDBConnector mConnector;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        mConnector = new LockedOnFileDBConnector(mContext, mDbName, mDatabaseFileLock, tablesManager);
    }

    @Test
    public void testOpenDbException() throws Throwable {
        doThrow(new RuntimeException()).when(mDatabaseFileLock).lock();
        assertThat(mConnector.openDb()).isNull();
    }

    @Test
    public void testOpenDbSuccess() {
        assertThat(mConnector.openDb()).isNotNull();
    }

    @Test
    public void testCloseBadDb() throws Throwable {
        doThrow(new RuntimeException()).when(mDatabaseFileLock).lock();
        mConnector.closeDb(mConnector.openDb());
    }

    @Test
    public void testCloseDbSuccess() {
        mConnector.closeDb(mConnector.openDb());
    }
}
