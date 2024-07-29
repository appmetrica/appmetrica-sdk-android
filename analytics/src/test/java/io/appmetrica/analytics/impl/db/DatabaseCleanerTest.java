package io.appmetrica.analytics.impl.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.ProtobufUtils;
import io.appmetrica.analytics.impl.SelfDiagnosticReporter;
import io.appmetrica.analytics.impl.SelfDiagnosticReporterStorage;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.LogRule;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DatabaseCleanerTest extends CommonTest {

    @Rule
    public final GlobalServiceLocatorRule rule = new GlobalServiceLocatorRule();
    @Rule
    public final LogRule logRule = new LogRule();

    private SQLiteDatabase db;
    private String tableName = "events";
    private String apiKey = "apiKey";
    private Context context;
    @Mock
    private IReporterExtended selfReporter;
    @Mock
    private SelfDiagnosticReporterStorage storage;
    @Mock
    private SelfDiagnosticReporter selfDiagnosticReporter;

    private DatabaseCleaner databaseCleaner;

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
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        when(storage.getOrCreateReporter(apiKey, CounterConfigurationReporterType.MAIN)).thenReturn(selfDiagnosticReporter);
        db = new SimpleDatabaseHelper(context).getWritableDatabase();
        databaseCleaner = new DatabaseCleaner(CounterConfigurationReporterType.MAIN, storage);
    }

    @Test
    public void testReturnValue() {
        addEvents(5);
        DatabaseCleaner.DeletionInfo deletionInfo = databaseCleaner.cleanEvents(
            db,
            tableName,
                Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER + " < 4",
                DatabaseCleaner.Reason.DB_OVERFLOW,
            apiKey,
                false
        );
        assertThat(deletionInfo.selectedEvents).extracting(new Function<ContentValues, Integer>() {
            @Override
            public Integer apply(ContentValues input) {
                return input.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER);
            }
        }).containsExactlyInAnyOrder(1, 2, 3);
        assertThat(deletionInfo.mDeletedRowsCount).isEqualTo(3);
    }

    @Test
    public void testEventsDeleted() {
        addEvents(5);
        databaseCleaner.cleanEvents(
            db,
            tableName,
                Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER + " < 4",
                DatabaseCleaner.Reason.DB_OVERFLOW,
            apiKey,
                false
        );
        Cursor cursor = db.rawQuery("SELECT * FROM \"events\"", null);
        cursor.moveToFirst();
        assertThat(cursor.getCount()).isEqualTo(2);
    }

    @Test
    public void testCleanupFormed() throws Exception {
        addEvents(5);
        databaseCleaner.cleanEvents(
            db,
            tableName,
                Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER + " < 4",
                DatabaseCleaner.Reason.DB_OVERFLOW,
            apiKey,
                true
        );
        ArgumentCaptor<CounterReport> reportCaptor = ArgumentCaptor.forClass(CounterReport.class);
        verify(selfDiagnosticReporter).reportEvent(reportCaptor.capture());
        CounterReport report = reportCaptor.getValue();
        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_CLEANUP.getTypeId());

        JSONObject details = new JSONObject();
        details.put("reason", "db_overflow");
        details.put("cleared", new JSONObject()
            .put("global_number", new JSONArray("[1,2,3]"))
            .put("event_type", new JSONArray("[4,4,4]"))
        );
        details.put("actual_deleted_number", 3);
        JSONObject expected = new JSONObject().put("details", details);
        JSONAssert.assertEquals(expected.toString(), report.getValue(), true);
    }

    @Test
    public void testNullStorageDoesNotThrow() {
        databaseCleaner = new DatabaseCleaner(CounterConfigurationReporterType.MAIN, null);
        addEvents(5);
        databaseCleaner.cleanEvents(
            db,
            tableName,
                Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER + " < 4",
                DatabaseCleaner.Reason.DB_OVERFLOW,
            apiKey,
                true
        );
    }

    @Test
    public void testNullApiKeyDoesNotThrow() {
        addEvents(5);
        databaseCleaner.cleanEvents(
            db,
            tableName,
                Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER + " < 4",
                DatabaseCleaner.Reason.DB_OVERFLOW,
                null,
                true
        );
    }

    @Test
    public void testCouldNotFormReport() {
        try (MockedStatic<EventsManager> sEventsManager = Mockito.mockStatic(EventsManager.class)) {
            when(EventsManager.cleanupEventReportEntry(anyString(), any(PublicLogger.class))).thenThrow(new RuntimeException());
            addEvents(5);
            databaseCleaner.cleanEvents(
                db,
                tableName,
                    Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER + " < 4",
                    DatabaseCleaner.Reason.DB_OVERFLOW,
                apiKey,
                    true
            );
            verifyNoMoreInteractions(selfDiagnosticReporter);
        }
    }

    @Test
    public void testException() {
        try (MockedStatic<AppMetricaSelfReportFacade> sAppMetricaSelfReportFacade = Mockito.mockStatic(AppMetricaSelfReportFacade.class)) {
            when(AppMetricaSelfReportFacade.getReporter()).thenReturn(selfReporter);
            DatabaseCleaner.DeletionInfo deletionInfo = databaseCleaner.cleanEvents(
                db,
                    "bad_table",
                    Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER + " < 4",
                    DatabaseCleaner.Reason.DB_OVERFLOW,
                apiKey,
                    false
            );
            assertThat(deletionInfo.selectedEvents).isNull();
            assertThat(deletionInfo.mDeletedRowsCount).isEqualTo(0);
        }
    }

    @Test
    public void testEventTypeIsConvertedToProto() throws Exception {
        try (MockedStatic<ProtobufUtils> sProtobufUtils = Mockito.mockStatic(ProtobufUtils.class)) {
            final int protoEvent = 21;
            when(ProtobufUtils.internalEventToProto(InternalEvents.EVENT_TYPE_REGULAR)).thenReturn(protoEvent);
            addEvents(1);
            databaseCleaner.cleanEvents(
                db,
                tableName,
                    Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER + " < 4",
                    DatabaseCleaner.Reason.DB_OVERFLOW,
                apiKey,
                    true
            );
            ArgumentCaptor<CounterReport> reportCaptor = ArgumentCaptor.forClass(CounterReport.class);
            verify(selfDiagnosticReporter).reportEvent(reportCaptor.capture());
            sProtobufUtils.verify(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    ProtobufUtils.internalEventToProto(InternalEvents.EVENT_TYPE_REGULAR);
                }
            });
            CounterReport report = reportCaptor.getValue();
            JSONAssert.assertEquals(
                    new JSONArray().put(protoEvent),
                    new JSONObject(report.getValue()).getJSONObject("details").getJSONObject("cleared").getJSONArray("event_type"),
                    true
            );
        }
    }

    private void addEvents(final int count) {
        db.beginTransaction();
        for (int i = 0; i < count; i++) {
            db.insert(Constants.EventsTable.TABLE_NAME, null, getReportContentValues(i));
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private ContentValues getReportContentValues(final int number) {
        ContentValues values = new ContentValues();
        values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION, number);
        values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER, number + 1);
        values.put(
                Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE,
                InternalEvents.EVENT_TYPE_REGULAR.getTypeId()
        );
        values.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION, 10);
        values.put(
                Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE,
                EventProto.ReportMessage.Session.SessionDesc.SESSION_FOREGROUND
        );
        values.put(
            Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION,
            "description".getBytes(StandardCharsets.UTF_8)
        );
        return values;
    }
}
