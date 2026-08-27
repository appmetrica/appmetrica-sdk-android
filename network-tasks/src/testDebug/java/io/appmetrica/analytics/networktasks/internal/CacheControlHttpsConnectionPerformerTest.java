package io.appmetrica.analytics.networktasks.internal;

import io.appmetrica.analytics.networkapi.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CacheControlHttpsConnectionPerformerTest {

    @Mock
    private CacheControlHttpsConnectionPerformer.RequestExecutor requestExecutor;
    @Mock
    private CacheControlHttpsConnectionPerformer.Client client;
    @Mock
    private Response response;
    @Mock
    private SSLSocketFactory sslSocketFactory;
    private final String url = "some.url";
    private final String oldETag = "old-etag";
    private CacheControlHttpsConnectionPerformer performer;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        performer = new CacheControlHttpsConnectionPerformer(requestExecutor, sslSocketFactory);
        when(client.getOldETag()).thenReturn(oldETag);
        when(requestExecutor.execute(oldETag, url, sslSocketFactory)).thenReturn(response);
    }

    @Test
    public void executeThrows() {
        doThrow(new RuntimeException()).when(requestExecutor).execute(oldETag, url, sslSocketFactory);
        performer.performConnection(url, client);
        verify(client).onError();
    }

    @Test
    public void badRequest() {
        when(response.getCode()).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST);
        performer.performConnection(url, client);
        verify(client).onError();
    }

    @Test
    public void notModified() {
        when(response.getCode()).thenReturn(HttpsURLConnection.HTTP_NOT_MODIFIED);
        performer.performConnection(url, client);
        verify(client).onNotModified();
    }

    @Test
    public void okNoETag() {
        byte[] responseData = "response".getBytes();
        when(response.getCode()).thenReturn(HttpsURLConnection.HTTP_OK);
        when(response.getResponseData()).thenReturn(responseData);
        performer.performConnection(url, client);
        verify(client).onResponse("", responseData);
    }

    @Test
    public void okResponse() throws IOException {
        String etag = "new-etag";
        byte[] responseData = "response".getBytes();
        when(response.getCode()).thenReturn(HttpsURLConnection.HTTP_OK);
        when(response.getResponseData()).thenReturn(responseData);
        when(response.getHeaders()).thenReturn(Collections.singletonMap("etag", Arrays.asList(etag)));
        performer.performConnection(url, client);
        verify(client).onResponse(etag, responseData);
    }
}
