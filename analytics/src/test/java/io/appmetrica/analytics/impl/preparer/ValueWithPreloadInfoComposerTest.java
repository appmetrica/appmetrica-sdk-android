package io.appmetrica.analytics.impl.preparer;

import android.content.ContentValues;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.protobuf.client.DbProto;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ValueWithPreloadInfoComposerTest extends CommonTest {

    @Mock
    private ReportRequestConfig mConfig;
    @Mock
    private EncryptedStringValueComposer mEncryptedStringValueComposer;
    private ValueWithPreloadInfoComposer mComposer;
    private final byte[] mExpectedBytes = new byte[]{12, 34, 56, 78, 90, 21, 43, 65};
    @Captor
    private ArgumentCaptor<EventFromDbModel> mEventCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mEncryptedStringValueComposer.getValue(any(EventFromDbModel.class), same(mConfig))).thenReturn(mExpectedBytes);
        mComposer = new ValueWithPreloadInfoComposer(mEncryptedStringValueComposer);
    }

    @Test
    public void shouldSendPreloadInfo() {
        when(mConfig.needToSendPreloadInfo()).thenReturn(true);
        EventFromDbModel event = mock(EventFromDbModel.class);
        assertThat(mComposer.getValue(event, mConfig)).isEqualTo(mExpectedBytes);
    }

    @Test
    public void shouldNotSendPreloadInfoHasPreloadInfo() throws Exception {
        String value = new JSONObject().put("key", "value").put("preloadInfo", new JSONObject()).toString();
        ContentValues cv = new ContentValues();
        EventEncryptionMode mode = EventEncryptionMode.AES_VALUE_ENCRYPTION;
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = value;
        eventDescription.encryptingMode = mode.getModeId();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        when(mConfig.needToSendPreloadInfo()).thenReturn(false);

        assertThat(mComposer.getValue(new EventFromDbModel(cv), mConfig)).isEqualTo(mExpectedBytes);
        verify(mEncryptedStringValueComposer).getValue(mEventCaptor.capture(), same(mConfig));
        JSONAssert.assertEquals(new JSONObject().put("key", "value").toString(), mEventCaptor.getValue().getValue(), true);
    }

    @Test
    public void shouldNotSendPreloadInfoHasNotPreloadInfo() throws Exception {
        String value = new JSONObject().put("key", "value").toString();
        ContentValues cv = new ContentValues();
        EventEncryptionMode mode = EventEncryptionMode.AES_VALUE_ENCRYPTION;
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = value;
        eventDescription.encryptingMode = mode.getModeId();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        when(mConfig.needToSendPreloadInfo()).thenReturn(false);
        assertThat(mComposer.getValue(new EventFromDbModel(cv), mConfig)).isEqualTo(mExpectedBytes);
        verify(mEncryptedStringValueComposer).getValue(mEventCaptor.capture(), same(mConfig));
        JSONAssert.assertEquals(value, mEventCaptor.getValue().getValue(), true);
    }

    @Test
    public void shouldNotSendPreloadInfoBadJson() {
        String value = "bad json";
        ContentValues cv = new ContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = value;
        eventDescription.encryptingMode = EventEncryptionMode.AES_VALUE_ENCRYPTION.getModeId();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        when(mConfig.needToSendPreloadInfo()).thenReturn(false);
        assertThat(mComposer.getValue(new EventFromDbModel(cv), mConfig)).isEqualTo(mExpectedBytes);
        verify(mEncryptedStringValueComposer).getValue(mEventCaptor.capture(), same(mConfig));
        assertThat(mEventCaptor.getValue().getValue()).isEqualTo(value);
    }

    @Test
    public void shouldNotSendPreloadInfoNullValue() {
        ContentValues cv = new ContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.encryptingMode = EventEncryptionMode.AES_VALUE_ENCRYPTION.getModeId();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        when(mConfig.needToSendPreloadInfo()).thenReturn(false);
        assertThat(mComposer.getValue(new EventFromDbModel(cv), mConfig)).isEqualTo(mExpectedBytes);
        verify(mEncryptedStringValueComposer).getValue(mEventCaptor.capture(), same(mConfig));
        assertThat(mEventCaptor.getValue().getValue()).isNull();
    }

    @Test
    public void shouldNotSendPreloadInfoEmptyValue() {
        ContentValues cv = new ContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = "";
        eventDescription.encryptingMode = EventEncryptionMode.AES_VALUE_ENCRYPTION.getModeId();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        when(mConfig.needToSendPreloadInfo()).thenReturn(false);
        assertThat(mComposer.getValue(new EventFromDbModel(cv), mConfig)).isEqualTo(mExpectedBytes);
        verify(mEncryptedStringValueComposer).getValue(mEventCaptor.capture(), same(mConfig));
        assertThat(mEventCaptor.getValue().getValue()).isNull();
    }
}
