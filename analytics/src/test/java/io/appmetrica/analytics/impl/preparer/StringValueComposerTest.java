package io.appmetrica.analytics.impl.preparer;

import android.content.ContentValues;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.protobuf.client.DbProto;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class StringValueComposerTest extends CommonTest {

    @Mock
    private ReportRequestConfig mConfig;
    private final StringValueComposer mStringValueComposer = new StringValueComposer();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetValueNull() {
        assertThat(mStringValueComposer.getValue(new EventFromDbModel(new ContentValues()), mConfig)).isEmpty();
    }

    @Test
    public void testGetValueEmpty() {
        ContentValues cv = MockProvider.mockedContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = "";
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));

        assertThat(mStringValueComposer.getValue(new EventFromDbModel(cv), mConfig)).isEmpty();
    }

    @Test
    public void testGetValue() {
        final String value = "some value";
        ContentValues cv = MockProvider.mockedContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = value;
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));

        assertThat(mStringValueComposer.getValue(new EventFromDbModel(cv), mConfig)).isEqualTo(value.getBytes());
    }
}
