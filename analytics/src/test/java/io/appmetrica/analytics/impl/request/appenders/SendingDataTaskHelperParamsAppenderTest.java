package io.appmetrica.analytics.impl.request.appenders;

import android.net.Uri;
import io.appmetrica.analytics.networktasks.internal.NetworkTaskForSendingDataParamsAppender;
import io.appmetrica.analytics.networktasks.internal.RequestBodyEncrypter;
import io.appmetrica.analytics.networktasks.internal.RequestBodyEncryptionMode;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SendingDataTaskHelperParamsAppenderTest extends CommonTest {

    static final String ENCRYPTED_REQUEST = "encrypted_request";
    static final String AES_RSA = "1";

    private RequestBodyEncrypter mRequestBodyEncrypter = mock(RequestBodyEncrypter.class);
    protected Uri.Builder mBuilder;
    protected NetworkTaskForSendingDataParamsAppender mParamsAppender;

    private final String commitHash = "sadasds232";
    private final String sourceBuildType = "public_source_release";

    @Before
    public void setUp() throws Exception {
        mBuilder = new Uri.Builder();
        mParamsAppender = new NetworkTaskForSendingDataParamsAppender(mRequestBodyEncrypter);
    }

    @Test
    public void testAppendParamsAddEncryptionFlag() {
        when(mRequestBodyEncrypter.getEncryptionMode()).thenReturn(RequestBodyEncryptionMode.AES_RSA);
        mParamsAppender.appendEncryptedData(mBuilder);
        assertThat(mBuilder.build().getQueryParameter(ENCRYPTED_REQUEST)).isEqualTo(AES_RSA);
    }

    @Test
    public void testAppendParamsDoesNotAddEncryptionFlagForNonEncryptionMode() {
        when(mRequestBodyEncrypter.getEncryptionMode()).thenReturn(RequestBodyEncryptionMode.NONE);
        mParamsAppender.appendEncryptedData(mBuilder);
        assertThat(mBuilder.build().getQueryParameter(ENCRYPTED_REQUEST)).isNull();
    }

    @Test
    public void testAppendParamsDoesNotAddEncryptionFlagForNull() {
        when(mRequestBodyEncrypter.getEncryptionMode()).thenReturn(null);
        mParamsAppender.appendEncryptedData(mBuilder);
        assertThat(mBuilder.build().getQueryParameter(ENCRYPTED_REQUEST)).isNull();
    }
}
