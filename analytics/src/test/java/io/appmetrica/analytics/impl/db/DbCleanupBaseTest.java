package io.appmetrica.analytics.impl.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.impl.utils.TimeUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public abstract class DbCleanupBaseTest extends CommonTest {

    @Rule
    public final GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();
    SQLiteDatabase mDb;
    DatabaseHelper mHelper;

    @Before
    public void setUp() {
        DatabaseHelperTest.SimpleDatabaseHelper simpleDatabaseHelper = new DatabaseHelperTest.SimpleDatabaseHelper(RuntimeEnvironment.getApplication());
        mDb = simpleDatabaseHelper.getWritableDatabase();

        DatabaseStorage storage = mock(DatabaseStorage.class);
        doReturn(mDb).when(storage).getWritableDatabase();
        doReturn(mDb).when(storage).getReadableDatabase();

        ComponentId componentId = mock(ComponentId.class);
        ComponentUnit componentUnit = mock(ComponentUnit.class);
        doReturn(TestUtils.createDefaultStartupState()).when(componentUnit).getStartupState();
        when(componentUnit.getComponentId()).thenReturn(componentId);
        when(componentId.getApiKey()).thenReturn("apiKey");
        ReportRequestConfig config = mock(ReportRequestConfig.class);
        when(componentUnit.getFreshReportRequestConfig()).thenReturn(config);
        when(config.getMaxEventsInDbCount()).thenReturn(getMaxEventsCount());
        when(componentUnit.getContext()).thenReturn(RuntimeEnvironment.getApplication());
        when(componentUnit.getPublicLogger()).thenReturn(mock(PublicLogger.class));
        mHelper = new DatabaseHelper(componentUnit, storage);
    }

    @After
    public void tearDown() {
        try {
            mDb.close();
        } catch (Throwable ignored) {}
    }

    int execForSingleInt(final String query) {
        Cursor cursor = mDb.rawQuery(query, null);
        cursor.moveToNext();
        int result = cursor.getInt(0);
        Utils.closeCursor(cursor);
        return result;
    }

    List<Integer> selectTypes() {
        List<Integer> types = new ArrayList<Integer>();
        Cursor cursor = mDb.rawQuery("select " + Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE + " from " + Constants.EventsTable.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            types.add(cursor.getInt(0));
        }
        Utils.closeCursor(cursor);
        return types;
    }

    void addExcessiveEvents(final int count, final int type) {
        List<ContentValues> reports = new ArrayList<ContentValues>();
        for (int i = 0; i < count; i++) {
            ContentValues values = new ContentValues();
            values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION, TimeUtils.currentDeviceTimeSec());
            values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE, 0);
            values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION, i);
            values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE, type);
            values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER, 42);
            values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TIME, 424242);
            reports.add(values);
        }
        mHelper.insertEvents(reports);
    }

    abstract long getMaxEventsCount();
}
