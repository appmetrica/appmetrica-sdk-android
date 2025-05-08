package io.appmetrica.analytics.impl.preparer;

import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class NoneEncodingTypeProviderTest extends CommonTest {

    private final NoneEncodingTypeProvider mProvider = new NoneEncodingTypeProvider();

    @Test
    public void testGetEncryptionModeExternallyEncrypted() {
        assertThat(mProvider.getEncodingType(EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER)).isEqualTo(EventProto.ReportMessage.Session.Event.NONE);
    }

    @Test
    public void testGetEncryptionModeAes() {
        assertThat(mProvider.getEncodingType(EventEncryptionMode.AES_VALUE_ENCRYPTION)).isEqualTo(EventProto.ReportMessage.Session.Event.NONE);
    }

    @Test
    public void testGetEncryptionModeNone() {
        assertThat(mProvider.getEncodingType(EventEncryptionMode.NONE)).isEqualTo(EventProto.ReportMessage.Session.Event.NONE);
    }
}
