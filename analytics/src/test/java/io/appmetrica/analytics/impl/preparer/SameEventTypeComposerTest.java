package io.appmetrica.analytics.impl.preparer;

import android.content.ContentValues;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockProvider;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SameEventTypeComposerTest extends CommonTest {

    private final SameEventTypeComposer mSameEventTypeComposer = new SameEventTypeComposer();

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Test
    public void testGetEventTypeNoType() {
        EventFromDbModel event = new EventFromDbModel(MockProvider.mockedContentValues());
        assertThat(mSameEventTypeComposer.getEventType(event)).isNull();
    }

    @Test
    public void testGetEventType() {
        ContentValues cv = MockProvider.mockedContentValues();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE, InternalEvents.EVENT_TYPE_REGULAR.getTypeId());
        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(mSameEventTypeComposer.getEventType(event)).isEqualTo(EventProto.ReportMessage.Session.Event.EVENT_CLIENT);
    }
}
