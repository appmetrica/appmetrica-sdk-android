package io.appmetrica.analytics.impl.preparer;

import android.content.ContentValues;
import android.util.Base64;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.protobuf.client.DbProto;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class BytesValueComposerTest extends CommonTest {

    @Mock
    private ReportRequestConfig mConfig;
    private final BytesValueComposer mBytesValueComposer = new BytesValueComposer();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetValue() {
        final byte[] expected = new byte[]{1, 2, 3, 4};
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = new String(Base64.encode(expected, Base64.DEFAULT));
        ContentValues cv = new ContentValues();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(mBytesValueComposer.getValue(event, mConfig)).isEqualTo(expected);
    }

    @Test
    public void testGetValueException() {
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = "bad base64";
        ContentValues cv = new ContentValues();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(mBytesValueComposer.getValue(event, mConfig)).isEmpty();
    }

    @Test
    public void testGetValueNoValue() {
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        ContentValues cv = new ContentValues();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(mBytesValueComposer.getValue(event, mConfig)).isEmpty();
    }
}
