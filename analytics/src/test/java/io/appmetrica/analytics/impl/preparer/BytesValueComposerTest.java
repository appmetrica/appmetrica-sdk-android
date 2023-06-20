package io.appmetrica.analytics.impl.preparer;

import android.content.ContentValues;
import android.util.Base64;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.protobuf.client.DbProto;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypter;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypterProvider;
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BytesValueComposerTest extends CommonTest {

    @Mock
    private EventEncrypterProvider mEventEncrypterProvider;
    @Mock
    private EventEncrypter mEventEncrypter;
    @Mock
    private ReportRequestConfig mConfig;
    private BytesValueComposer mBytesValueComposer;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mBytesValueComposer = new BytesValueComposer(mEventEncrypterProvider);
    }

    @Test
    public void testGetValue() {
        final byte[] expected = new byte[]{1, 2, 3, 4};
        final byte[] value = "value".getBytes();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = new String(Base64.encode(value, Base64.DEFAULT));
        eventDescription.encryptingMode = EventEncryptionMode.AES_VALUE_ENCRYPTION.getModeId();
        ContentValues cv = new ContentValues();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        when(mEventEncrypterProvider.getEventEncrypter(EventEncryptionMode.AES_VALUE_ENCRYPTION)).thenReturn(mEventEncrypter);
        when(mEventEncrypter.decrypt(value)).thenReturn(expected);
        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(mBytesValueComposer.getValue(event, mConfig)).isEqualTo(expected);
    }

    @Test
    public void testGetValueException() {
        final byte[] expected = new byte[]{1, 2, 3, 4};
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = "bad base64";
        eventDescription.encryptingMode = EventEncryptionMode.AES_VALUE_ENCRYPTION.getModeId();
        ContentValues cv = new ContentValues();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        when(mEventEncrypterProvider.getEventEncrypter(EventEncryptionMode.AES_VALUE_ENCRYPTION)).thenReturn(mEventEncrypter);
        when(mEventEncrypter.decrypt(new byte[0])).thenReturn(expected);
        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(mBytesValueComposer.getValue(event, mConfig)).isEqualTo(expected);
    }

    @Test
    public void testGetValueNoValue() {
        final byte[] expected = new byte[]{1, 2, 3, 4};
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.encryptingMode = EventEncryptionMode.AES_VALUE_ENCRYPTION.getModeId();
        ContentValues cv = new ContentValues();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        when(mEventEncrypterProvider.getEventEncrypter(EventEncryptionMode.AES_VALUE_ENCRYPTION)).thenReturn(mEventEncrypter);
        when(mEventEncrypter.decrypt(new byte[0])).thenReturn(expected);
        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(mBytesValueComposer.getValue(event, mConfig)).isEqualTo(expected);
    }
}
