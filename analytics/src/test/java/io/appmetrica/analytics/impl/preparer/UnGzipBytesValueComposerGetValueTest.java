package io.appmetrica.analytics.impl.preparer;

import android.content.ContentValues;
import io.appmetrica.analytics.coreutils.internal.io.Base64Utils;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.protobuf.client.DbProto;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypter;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypterProvider;
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @see UnGzipBytesValueComposerTest
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
public class UnGzipBytesValueComposerGetValueTest extends CommonTest {

    private byte[] input;
    private byte[] decrypted;
    private byte[] expected;

    public UnGzipBytesValueComposerGetValueTest(byte[] input,  byte[] decrypted, byte[] expected) {
        this.input = input;
        this.decrypted = decrypted;
        this.expected = expected;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {

        byte[] smallEncrypted = randomBytes(1000);
        byte[] mediumEncrypted = randomBytes(200 * 1024);
        byte[] largeEncrypted = randomBytes(1024 * 1024);

        byte[] smallExpected = randomBytes(1000);
        byte[] mediumExpected = randomBytes(200 * 1024);
        byte[] largeExpected = randomBytes(1024 * 1024);

        return Arrays.asList(new Object[][]{
                //#0
                {smallEncrypted, smallExpected, smallExpected},
                //#1
                {mediumEncrypted, mediumExpected, mediumExpected},
                //#2
                {largeEncrypted, largeExpected, largeExpected},
                //#3
                {null, smallEncrypted, new byte[0]},
                //#4
                {new byte[0], smallExpected, smallExpected},
                //#5
                {smallEncrypted, null, new byte[0]},
                //#6
                {smallEncrypted, new byte[0], new byte[0]}
        });
    }

    private static byte[] randomBytes(int length) {
        byte[] result = new byte[length];
        new Random().nextBytes(result);
        return result;
    }

    @Mock
    private EventEncrypterProvider encrypterProvider;
    @Mock
    private EventEncrypter eventEncrypter;
    @Mock
    private ReportRequestConfig config;

    private EventEncryptionMode encryptionMode = EventEncryptionMode.NONE;

    private UnGzipBytesValueComposer composer;
    private String inputString;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        inputString = Base64Utils.compressBase64(input);

        when(encrypterProvider.getEventEncrypter(encryptionMode)).thenReturn(eventEncrypter);
        when(eventEncrypter.decrypt(input)).thenReturn(decrypted);

        composer = new UnGzipBytesValueComposer(encrypterProvider);
    }

    @Test
    public void getValue() {
        ContentValues contentValues = new ContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        if (inputString != null) {
            eventDescription.value = inputString;
        }
        eventDescription.encryptingMode = EventEncryptionMode.NONE.getModeId();
        contentValues.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));

        EventFromDbModel event = new EventFromDbModel(contentValues);
        assertThat(composer.getValue(event, config)).isEqualTo(expected);
    }
}
