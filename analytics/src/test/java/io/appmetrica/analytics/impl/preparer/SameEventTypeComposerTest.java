package io.appmetrica.analytics.impl.preparer;

import android.content.ContentValues;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SameEventTypeComposerTest extends CommonTest {

    private SameEventTypeComposer mSameEventTypeComposer = new SameEventTypeComposer();

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Test
    public void testGetEventTypeNoType() {
        EventFromDbModel event = new EventFromDbModel(new ContentValues());
        assertThat(mSameEventTypeComposer.getEventType(event)).isNull();
    }

    @Test
    public void testGetEventType() {
        ContentValues cv = new ContentValues();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE, InternalEvents.EVENT_TYPE_REGULAR.getTypeId());
        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(mSameEventTypeComposer.getEventType(event)).isEqualTo(EventProto.ReportMessage.Session.Event.EVENT_CLIENT);
    }
}
