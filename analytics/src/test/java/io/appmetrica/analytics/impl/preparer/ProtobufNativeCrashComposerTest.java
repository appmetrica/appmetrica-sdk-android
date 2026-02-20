package io.appmetrica.analytics.impl.preparer;

import android.content.ContentValues;
import android.util.Base64;
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
import io.appmetrica.analytics.testutils.MockProvider;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ProtobufNativeCrashComposerTest extends CommonTest {

    @Mock
    private ReportRequestConfig config;

    private final String filledValue = "filled value";
    private final String compressedValue = "compressed value";
    private final String encodedValue = "encoded value";

    @Rule
    public final MockedStaticRule<Base64> base64MockedStaticRule = new MockedStaticRule<>(Base64.class);
    @Rule
    public final MockedStaticRule<GZIPUtils> gzipUtilsMockedStaticRule = new MockedStaticRule<>(GZIPUtils.class);
    private final ProtobufNativeCrashComposer protobufNativeCrashComposer = new ProtobufNativeCrashComposer();

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        when(GZIPUtils.gzipBytes(filledValue.getBytes())).thenReturn(compressedValue.getBytes());
        when(GZIPUtils.unGzipBytes(compressedValue.getBytes())).thenReturn(filledValue.getBytes());
        when(Base64.encodeToString(compressedValue.getBytes(), Base64.DEFAULT)).thenReturn(encodedValue);
        when(Base64.decode(encodedValue, Base64.DEFAULT)).thenReturn(compressedValue.getBytes());
    }

    @Test
    public void someValue() throws Exception {
        final String originalValue = filledValue;
        byte[] compressedBytes = GZIPUtils.gzipBytes(StringUtils.getUTF8Bytes(originalValue));

        ContentValues cv = MockProvider.mockedContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = Base64Utils.compressBase64(originalValue.getBytes());
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));

        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(protobufNativeCrashComposer.getValue(event, config)).isEqualTo(compressedBytes);
    }

    @Test
    public void emptyValue() {
        ContentValues cv = MockProvider.mockedContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = "";
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));

        EventFromDbModel event = new EventFromDbModel(cv);
        assertThat(protobufNativeCrashComposer.getValue(event, config)).isEqualTo(new byte[0]);
    }

    @Test
    public void nullValue() {
        ContentValues cv = MockProvider.mockedContentValues();
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
