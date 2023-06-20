package io.appmetrica.analytics.impl.events;

import android.content.ContentValues;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContainsUrgentEventsConditionTest extends CommonTest {

    private static Set<Integer> sUrgentEvents = new HashSet<Integer>(Arrays.asList(
            InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId(),
            InternalEvents.EVENT_TYPE_APP_UPDATE.getTypeId(),
            InternalEvents.EVENT_TYPE_INIT.getTypeId(),
            InternalEvents.EVENT_TYPE_SEND_REFERRER.getTypeId()
    ));

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class UrgentEventTypesTest {

        private ContainsUrgentEventsCondition mUrgentEventsCondition;
        private final boolean mIsUrgent;

        @ParameterizedRobolectricTestRunner.Parameters(name = "Type {0} urgent? {1}")
        public static Collection<Object[]> data() {
            List<Object[]> data = new ArrayList<Object[]>();
            for (Integer eventType : sUrgentEvents) {
                data.add(new Object[]{eventType, true});
            }
            for (InternalEvents eventType : InternalEvents.values()) {
                if (sUrgentEvents.contains(eventType.getTypeId()) == false) {
                    data.add(new Object[]{eventType.getTypeId(), false});
                }
            }
            return data;
        }

        public UrgentEventTypesTest(Integer eventType, boolean isUrgent) {
            mIsUrgent = isUrgent;
            mUrgentEventsCondition = new ContainsUrgentEventsCondition(mock(DatabaseHelper.class));
            ContentValues report = mock(ContentValues.class);
            when(report.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE)).thenReturn(eventType);
            mUrgentEventsCondition.onEventsAdded(Arrays.asList(eventType));
        }

        @Test
        public void test() {
            assertThat(mUrgentEventsCondition.isConditionMet()).isEqualTo(mIsUrgent);
        }
    }

    @Mock
    private DatabaseHelper mDatabaseHelper;
    private ContainsUrgentEventsCondition mUrgentEventsCondition;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mUrgentEventsCondition = new ContainsUrgentEventsCondition(mDatabaseHelper);
    }

    @Test
    public void testUrgentReportSavedThenRemoved() {
        ContentValues report = mock(ContentValues.class);
        final int type = InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId();
        when(report.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE)).thenReturn(type);
        mUrgentEventsCondition.onEventsAdded(Arrays.asList(type));
        mUrgentEventsCondition.onEventsRemoved(Arrays.asList(type));
        assertThat(mUrgentEventsCondition.isConditionMet()).isFalse();
    }

    @Test
    public void testTwoUrgentReportsSavedOneRemoved() {
        ContentValues report = mock(ContentValues.class);
        final int type = InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId();
        when(report.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE)).thenReturn(type);
        mUrgentEventsCondition.onEventsAdded(Arrays.asList(type, type));
        mUrgentEventsCondition.onEventsRemoved(Arrays.asList(type));
        assertThat(mUrgentEventsCondition.isConditionMet()).isTrue();
    }

    @Test
    public void testNoReportsSaved() {
        assertThat(mUrgentEventsCondition.isConditionMet()).isFalse();
    }

    @Test
    public void testUrgentEventSavedNotUrgentRemoved() {
        ContentValues firstReport = mock(ContentValues.class);
        final int firstType = InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId();
        when(firstReport.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE)).thenReturn(firstType);
        ContentValues customReport = mock(ContentValues.class);
        final int customType = InternalEvents.EVENT_TYPE_CUSTOM_EVENT.getTypeId();
        when(customReport.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE)).thenReturn(customType);
        mUrgentEventsCondition.onEventsAdded(Arrays.asList(firstType));
        mUrgentEventsCondition.onEventsRemoved(Arrays.asList(customType));
        assertThat(mUrgentEventsCondition.isConditionMet()).isTrue();
    }

    @Test
    public void testStateIsReadFromDb() {
        when(mDatabaseHelper.getEventsOfFollowingTypesCount(sUrgentEvents)).thenReturn(1L);
        ContainsUrgentEventsCondition condition = new ContainsUrgentEventsCondition(mDatabaseHelper);
        assertThat(condition.isConditionMet()).isTrue();
    }
}
