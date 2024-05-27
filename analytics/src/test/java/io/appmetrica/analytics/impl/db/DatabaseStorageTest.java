package io.appmetrica.analytics.impl.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DatabaseStorageTest extends CommonTest {

    @Mock
    private PublicLogger mPublicLogger;
    @Mock
    private TablesManager mTablesManager;
    @Mock
    private IReporterExtended mAppmetricaReporter;
    @Mock
    private SQLiteDatabase database;
    private Context mContext;
    private final String mDbName = "database_name";

    private DatabaseStorage mDatabaseStorage;

    @Rule
    public final MockedStaticRule<AppMetricaSelfReportFacade> sAppMetricaSelfReportFacade = new MockedStaticRule<>(AppMetricaSelfReportFacade.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        mDatabaseStorage = new DatabaseStorage(mContext, mDbName, mTablesManager, mPublicLogger);

        when(AppMetricaSelfReportFacade.getReporter()).thenReturn(mAppmetricaReporter);
    }

    @Test
    public void testGetReadableDatabase() {
        assertThat(mDatabaseStorage.getReadableDatabase()).isNotNull();
        verifyNoMoreInteractions(mPublicLogger);
    }

    @Test
    public void testGetWritableDatabase() {
        assertThat(mDatabaseStorage.getWritableDatabase()).isNotNull();
        verifyNoMoreInteractions(mPublicLogger);
    }

    @Test
    public void testGetReadableDatabaseThrows() {
        assertThat(new DatabaseStorage(null, mDbName, mTablesManager, mPublicLogger).getReadableDatabase()).isNull();
        verify(mPublicLogger).error(any(Throwable.class), anyString(), eq(mDbName));
        verify(mAppmetricaReporter).reportError(eq("db_read_error"), any(Throwable.class));
    }

    @Test
    public void testGetWritableDatabaseThrows() {
        assertThat(new DatabaseStorage(null, mDbName, mTablesManager, mPublicLogger).getWritableDatabase()).isNull();
        verify(mPublicLogger).error(any(Throwable.class), anyString(), eq(mDbName));
        verify(mAppmetricaReporter).reportError(eq("db_write_error"), any(Throwable.class));
    }

    @Test
    public void onUpgrade() {
        final int oldVersion = 50;
        final int newVersion = 51;
        mDatabaseStorage.onUpgrade(database, oldVersion, newVersion);
        verify(mTablesManager).onUpgrade(database, oldVersion, newVersion);
    }

    @Test
    public void onDowngrade() {
        final int oldVersion = 52;
        final int newVersion = 51;
        mDatabaseStorage.onDowngrade(database, oldVersion, newVersion);
        verify(mTablesManager).onDowngrade(database, oldVersion, newVersion);
    }
}
