package io.appmetrica.analytics.impl.preparer;

import android.content.ContentValues;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.io.Base64Utils;
import io.appmetrica.analytics.coreutils.internal.io.GZIPUtils;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.protobuf.client.DbProto;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
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

@RunWith(RobolectricTestRunner.class)
public class ProtobufNativeCrashComposerTest extends CommonTest {

    @Mock
    private ReportRequestConfig config;
    private ProtobufNativeCrashComposer protobufNativeCrashComposer = new ProtobufNativeCrashComposer();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void someValue() throws Exception {
        final String originalValue = "original value";
        byte[] compressedBytes = GZIPUtils.gzipBytes(StringUtils.getUTF8Bytes(originalValue));

        ContentValues cv = new ContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = Base64Utils.compressBase64(originalValue.getBytes());
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));

        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(protobufNativeCrashComposer.getValue(event, config)).isEqualTo(compressedBytes);
    }

    @Test
    public void emptyValue() {
        ContentValues cv = new ContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = "";
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));

        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(protobufNativeCrashComposer.getValue(event, config)).isEqualTo(new byte[0]);
    }

    @Test
    public void nullValue() {
        ContentValues cv = new ContentValues();
        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(protobufNativeCrashComposer.getValue(event, config)).isEqualTo(new byte[0]);
    }

    @Test
    public void encodingTypeForNonEncryption() {
        assertThat(protobufNativeCrashComposer.getEncodingType(EventEncryptionMode.NONE))
                .isEqualTo(EventProto.ReportMessage.Session.Event.GZIP);
    }

    @Test
    public void encodingTypeForNonEncryptionForExternallyEventCrypter() {
        assertThat(protobufNativeCrashComposer.getEncodingType(EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER))
                .isEqualTo(EventProto.ReportMessage.Session.Event.GZIP);
    }

    @Test
    public void encodingTypeForAesValueEncryption() {
        assertThat(protobufNativeCrashComposer.getEncodingType(EventEncryptionMode.AES_VALUE_ENCRYPTION))
                .isEqualTo(EventProto.ReportMessage.Session.Event.GZIP);
    }
}
