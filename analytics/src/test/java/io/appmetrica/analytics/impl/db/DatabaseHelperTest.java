package io.appmetrica.analytics.impl.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.EventSaver;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter;
import io.appmetrica.analytics.impl.events.EventListener;
import io.appmetrica.analytics.impl.events.EventTrigger;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.impl.utils.TimeUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.LogRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DatabaseHelperTest extends CommonTest {

    private static final Function<ContentValues, String> QUERY_PARAMETERS_EXTRACTOR =
            new Function<ContentValues, String>() {
                @Override
                public String apply(ContentValues contentValues) {
                    return contentValues.getAsString(
                            Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS
                    );
                }
            };

    private SQLiteDatabase db;
    private DatabaseHelper helper;
    @Mock
    private DatabaseStorage storage;
    @Mock
    private ComponentUnit componentUnit;
    @Mock
    private ReportRequestConfig config;
    @Mock
    private DatabaseCleaner databaseCleaner;
    @Mock
    private DbEventModelConverter dbEventModelConverter;
    @Mock
    private EventListener eventListener;
    @Rule
    public GlobalServiceLocatorRule rule = new GlobalServiceLocatorRule();
    @Rule
    public LogRule logRule = new LogRule();
    private final long maxEventsInDbCount = 200;
    private long session1;
    private long session2;
    private long session3;

    static class SimpleDatabaseHelper extends SQLiteOpenHelper {

        SimpleDatabaseHelper(final Context context) {
            super(context, "tests.db", null, 1);
        }

        public void onCreate(final SQLiteDatabase db) {
            db.execSQL(Constants.EventsTable.CREATE_TABLE);
            db.execSQL(Constants.SessionTable.CREATE_TABLE);
        }

        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        SimpleDatabaseHelper simpleDatabaseHelper = new SimpleDatabaseHelper(RuntimeEnvironment.getApplication());
        db = simpleDatabaseHelper.getWritableDatabase();

        doReturn(db).when(storage).getWritableDatabase();
        doReturn(db).when(storage).getReadableDatabase();

        ComponentId componentId = mock(ComponentId.class);
        when(componentUnit.getComponentId()).thenReturn(componentId);
        when(componentUnit.getEventSaver()).thenReturn(mock(EventSaver.class));
        when(componentId.getApiKey()).thenReturn("apiKey");
        when(componentUnit.getFreshReportRequestConfig()).thenReturn(config);
        when(componentUnit.getPublicLogger()).thenReturn(mock(PublicLogger.class));
        when(componentUnit.getEventTrigger()).thenReturn(mock(EventTrigger.class));
        when(config.getMaxEventsInDbCount()).thenReturn(maxEventsInDbCount);
        when(databaseCleaner.cleanEvents(same(db), anyString(), anyString(), any(DatabaseCleaner.Reason.class), nullable(String.class), anyBoolean()))
                .thenReturn(new DatabaseCleaner.DeletionInfo(Collections.<ContentValues>emptyList(), 0));
        helper = new DatabaseHelper(componentUnit, storage, databaseCleaner, dbEventModelConverter);
        helper.onComponentCreated();

        long now = TimeUtils.currentDeviceTimeSec();
        session1 = now - 2000;
        session2 = now - 1000;
        session3 = now - 200;
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void testClearIfTooManyEventsDoesNotExceed() {
        when(config.getMaxEventsInDbCount()).thenReturn(20L);
        addGeneralEvents(db, 19, TimeUtils.currentDeviceTimeSec());

        helper = new DatabaseHelper(componentUnit, storage, databaseCleaner, dbEventModelConverter);
        helper.addEventListener(eventListener);
        helper.clearIfTooManyEvents();

        verify(databaseCleaner, never())
            .cleanEvents(
                any(SQLiteDatabase.class),
                anyString(),
                anyString(),
                any(DatabaseCleaner.Reason.class),
                anyString(),
                anyBoolean()
            );
        verifyNoInteractions(eventListener);
    }

    @Test
    public void testClearIfTooManyEventsExceeds() throws Exception {
        when(config.getMaxEventsInDbCount()).thenReturn(20L);
        addGeneralEvents(db, 21, TimeUtils.currentDeviceTimeSec());

        helper = new DatabaseHelper(componentUnit, storage, databaseCleaner, dbEventModelConverter);
        helper.addEventListener(eventListener);

        when(databaseCleaner.cleanEvents(
            same(db),
            eq("events"),
            anyString(),
            eq(DatabaseCleaner.Reason.DB_OVERFLOW),
            anyString(),
            eq(true)
        )).thenReturn(new DatabaseCleaner.DeletionInfo(new ArrayList<ContentValues>(), 100));

        helper.clearIfTooManyEvents();

        verify(databaseCleaner)
            .cleanEvents(
                same(db),
                eq("events"),
                anyString(),
                eq(DatabaseCleaner.Reason.DB_OVERFLOW),
                anyString(),
                eq(true)
            );

        verify(eventListener).onEventsUpdated();
    }

    @Test
    public void testClearIfTooManyEventsDeletesTenPercentOfEvents() throws Exception {
        when(config.getMaxEventsInDbCount()).thenReturn(90L);
        addGeneralEvents(db, 100, TimeUtils.currentDeviceTimeSec());

        helper = new DatabaseHelper(componentUnit, storage);
        helper.clearIfTooManyEvents();

        Cursor cursor = db.rawQuery("select count() from events", null);
        cursor.moveToNext();
        assertThat(cursor.getInt(0)).isEqualTo(90);

        Utils.closeCursor(cursor);
    }

    @Test
    public void testClearIfTooManyEventsDeletesTenPercentOfEventsEvenIfImportantAreTheOldest() {
        when(config.getMaxEventsInDbCount()).thenReturn(90L);
        addGeneralEvents(db, 5, 1000, 0, EventsManager.EVENTS_WITH_FIRST_HIGHEST_PRIORITY.get(0));
        addGeneralEvents(db, 95, 1000);

        helper = new DatabaseHelper(componentUnit, storage);
        helper.clearIfTooManyEvents();

        Cursor cursor = db.rawQuery("select count() from events", null);
        cursor.moveToNext();
        assertThat(cursor.getInt(0)).isEqualTo(90);

        Utils.closeCursor(cursor);
    }

    @Test
    public void testClearIfTooManyEventsDeletesMostOldEvents() throws Exception {
        when(config.getMaxEventsInDbCount()).thenReturn(90L);
        addGeneralEvents(db, 5, session1);
        addGeneralEvents(db, 4, session2);
        addGeneralEvents(db, 91, session3);

        helper = new DatabaseHelper(componentUnit, storage);
        helper.clearIfTooManyEvents();

        Cursor cursor = db.rawQuery("select count() from events", null);
        cursor.moveToNext();
        assertThat(cursor.getInt(0)).isEqualTo(90);

        Utils.closeCursor(cursor);
        int result = execForSingleInt("select count() from events where session_id = " + session1);
        assertThat(result).isEqualTo(0);
        result = execForSingleInt("select count() from events where session_id = " + session2);
        assertThat(result).isEqualTo(0);
        result = execForSingleInt("select count() from events where session_id = " + session3);
        assertThat(result).isEqualTo(90);
    }

    @Test
    public void testCollectReportRequestParametersNoEvents() {
        addSession(db, 1001, 1, "b");
        addSession(db, 1000, 1, "a");
        addSession(db, 1002, 1, "b");

        assertThat(helper.collectAllQueryParameters()).extracting(QUERY_PARAMETERS_EXTRACTOR).isEmpty();
    }

    @Test
    public void testCollectReportRequestParametersOnlyOneSessionSelected() {
        addSession(db, 1001, 1, "b");
        addSession(db, 1000, 1, "a");
        addSession(db, 1002, 1, "b");
        addGeneralEvents(db, 1, 1001, 1);
        addGeneralEvents(db, 1, 1000, 1);
        addGeneralEvents(db, 1, 1002, 1);

        assertThat(helper.collectAllQueryParameters()).extracting(QUERY_PARAMETERS_EXTRACTOR).containsExactly("a");
    }

    @Test
    public void testCollectReportRequestParametersEmptySessionSkipped() {
        addSession(db, 1001, 1, "b");
        addSession(db, 1000, 1, "a");
        addSession(db, 1002, 1, "b");
        addGeneralEvents(db, 1, 1001, 1);

        assertThat(helper.collectAllQueryParameters()).extracting(QUERY_PARAMETERS_EXTRACTOR).containsExactly("b");
    }

    @Test
    public void testCollectReportRequestParametersForSingleBgSessionAndFgEvents() {
        addSession(db, 1000, 0, "a");
        addGeneralEvents(db, 1, 1000, 1);
        assertThat(helper.collectAllQueryParameters()).extracting(QUERY_PARAMETERS_EXTRACTOR).isEmpty();
    }

    @Test
    public void testCollectReportRequestParametersForSingleFgSessionAndBgEvents() {
        addSession(db, 1000, 1, "a");
        addGeneralEvents(db, 1, 1000, 0);
        assertThat(helper.collectAllQueryParameters()).extracting(QUERY_PARAMETERS_EXTRACTOR).isEmpty();
    }

    @Test
    public void testCollectReportRequestParametersForFgEventsAndDifferentSessions() {
        addSession(db, 1000, 0, "a");
        addSession(db, 1000, 1, "b");
        addSession(db, 1001, 1, "c");
        addGeneralEvents(db, 1, 1000, 1);
        assertThat(helper.collectAllQueryParameters()).extracting(QUERY_PARAMETERS_EXTRACTOR).containsExactly("b");
    }

    @Test
    public void testCollectReportRequestParametersForBgEventsAndDifferentSessions() {
        addSession(db, 1000, 1, "a");
        addSession(db, 1000, 0, "b");
        addSession(db, 1001, 0, "c");
        addGeneralEvents(db, 1, 1000, 0);
        assertThat(helper.collectAllQueryParameters()).extracting(QUERY_PARAMETERS_EXTRACTOR).containsExactly("b");
    }

    @Test
    public void testRemoveTopNotifies() {
        EventListener eventListener1 = mock(EventListener.class);
        EventListener eventListener2 = mock(EventListener.class);
        helper.addEventListener(eventListener1);
        helper.addEventListener(eventListener2);
        final ContentValues cv1 = new ContentValues();
        final ContentValues cv2 = new ContentValues();
        cv1.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE, 10);
        cv2.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE, 20);
        when(databaseCleaner.cleanEvents(same(db), anyString(), anyString(), eq(DatabaseCleaner.Reason.BAD_REQUEST), anyString(), eq(true)))
                .thenReturn(new DatabaseCleaner.DeletionInfo(Arrays.asList(cv1, cv2), 2));
        helper.removeTop(1000, 0, 2, true);
        ArgumentCaptor<List> reportTypesFirstListenerCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> reportTypesSecondListenerCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventListener1).onEventsRemoved(reportTypesFirstListenerCaptor.capture());
        verify(eventListener2).onEventsRemoved(reportTypesSecondListenerCaptor.capture());
        assertThat(reportTypesFirstListenerCaptor.getValue()).containsExactlyInAnyOrder(10, 20);
        assertThat(reportTypesSecondListenerCaptor.getValue()).containsExactlyInAnyOrder(10, 20);
    }

    @Test
    public void testRemoveTopShouldFormCleanup() {
        addGeneralEventsForFgSession(db, 1, 1000, InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId());
        addGeneralEventsForFgSession(db, 1, 1000, InternalEvents.EVENT_TYPE_CUSTOM_EVENT.getTypeId());
        helper.removeTop(1000, 0, 2, true);
        verify(databaseCleaner).cleanEvents(same(db), eq("events"), anyString(), eq(DatabaseCleaner.Reason.BAD_REQUEST), anyString(), eq(true));
    }

    @Test
    public void testRemoveTopShouldNotFormCleanup() {
        addGeneralEventsForFgSession(db, 1, 1000, InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId());
        addGeneralEventsForFgSession(db, 1, 1000, InternalEvents.EVENT_TYPE_CUSTOM_EVENT.getTypeId());
        helper.removeTop(1000, 0, 2, false);
        verify(databaseCleaner).cleanEvents(same(db), eq("events"), anyString(), eq(DatabaseCleaner.Reason.BAD_REQUEST), anyString(), eq(false));
    }

    @Test
    public void testEventsSavedNotifies() {
        EventListener eventListener1 = mock(EventListener.class);
        EventListener eventListener2 = mock(EventListener.class);
        helper.addEventListener(eventListener1);
        helper.addEventListener(eventListener2);
        ContentValues firstReport = mock(ContentValues.class);
        final int type = InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId();
        when(firstReport.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE)).thenReturn(type);
        helper.addReportValues(firstReport);
        verify(eventListener1, timeout(1000)).onEventsAdded(Arrays.asList(type));
    }

    @Test
    public void testGetEventsCountReadsState() {
        addGeneralEvents(db, 2, 1000, 0, 10);
        addGeneralEvents(db, 3, 1001, 1, 20);
        DatabaseHelper helper = new DatabaseHelper(componentUnit, storage, databaseCleaner, dbEventModelConverter);
        assertThat(helper.getEventsCount()).isEqualTo(5);
    }

    @Test
    public void testGetEventsCountUpdated() {
        helper.insertEvents(Arrays.asList(
                getReportContentValues(0, 10, 1000, 0),
                getReportContentValues(1, 20, 10001, 1)
        ));
        assertThat(helper.getEventsCount()).isEqualTo(2);
    }

    @Test
    public void testGetEventsOfFollowingTypesCount() {
        addGeneralEvents(db, 1, 1000, 0, 10);
        addGeneralEvents(db, 2, 1001, 1, 20);
        addGeneralEvents(db, 1, 1002, 1, 30);
        DatabaseHelper helper = new DatabaseHelper(componentUnit, storage, databaseCleaner, dbEventModelConverter);
        helper.insertEvents(Arrays.asList(
                getReportContentValues(0, 10, 1002, 0),
                getReportContentValues(1, 30, 1002, 1)
        ));
        assertThat(helper.getEventsOfFollowingTypesCount(new HashSet<Integer>(Arrays.asList(10, 20)))).isEqualTo(4);
    }

    @Test
    public void testRemoveEmptySessions() {
        addSession(db, 999, 1, "");
        addSession(db, 1000, 1, "");
        addSession(db, 1001, 1, "");
        helper.removeEmptySessions(1000);
        assertThat(execForSingleInt("select count() from sessions where id = 999")).isEqualTo(0);
        assertThat(execForSingleInt("select count() from sessions where id = 1000")).isEqualTo(1);
        assertThat(execForSingleInt("select count() from sessions where id = 1001")).isEqualTo(1);
    }

    private int execForSingleInt(final String query) {
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToNext();
        int result = cursor.getInt(0);
        Utils.closeCursor(cursor);
        return result;
    }

    private void addGeneralEvents(final SQLiteDatabase db, final int count, final long sessionId) {
        addGeneralEventsForFgSession(db, count, sessionId, InternalEvents.EVENT_TYPE_REGULAR.getTypeId());
    }

    private void addGeneralEvents(final SQLiteDatabase db, final int count, final long sessionId, int sessionType) {
        addGeneralEvents(db, count, sessionId, sessionType, InternalEvents.EVENT_TYPE_REGULAR.getTypeId());
    }

    private void addGeneralEventsForFgSession(final SQLiteDatabase db, final int count, final long sessionId, final int type) {
        addGeneralEvents(db, count, sessionId, 0, type);

    }

    private void addGeneralEvents(final SQLiteDatabase db, final int count, final long sessionId, final long sessionType, final int type) {
        db.beginTransaction();
        for (int i = 0; i < count; i++) {
            db.insert(Constants.EventsTable.TABLE_NAME, null, getReportContentValues(i, type, sessionId, sessionType));
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private ContentValues getReportContentValues(int numberInSession, int type, long sessionId, long sessionType) {
        ContentValues values = new ContentValues();
        values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION, numberInSession);
        values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE, type);
        values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION, sessionId);
        values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE, sessionType);
        return values;
    }

    private void addSession(SQLiteDatabase db, long sessionId, int sessionType, String requestParameter) {
        ContentValues values = new ContentValues();
        values.put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_ID, sessionId);
        values.put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_TYPE, sessionType);
        values.put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS, requestParameter);
        db.insert(Constants.SessionTable.TABLE_NAME, null, values);
    }
}
