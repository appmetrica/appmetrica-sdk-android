package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.impl.db.state.converter.BodyDecoder;
import io.appmetrica.analytics.impl.startup.parsing.StartupParser;
import io.appmetrica.analytics.impl.startup.parsing.StartupResult;
import io.appmetrica.analytics.networktasks.internal.ResponseDataHolder;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupNetworkResponseHandlerTest extends CommonTest {

    @Mock
    private StartupParser mStartupParser;
    @Mock
    private Map<String, List<String>> mHeaders;
    @Mock
    private StartupResult mResult;
    @Mock
    private BodyDecoder mBodyDecoder;
    @Mock
    private ResponseDataHolder responseDataHolder;

    private String mPassword = "hBnBQbZrmjPXEWVJ";
    private byte[] mResponseBody = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
    private byte[] mUncompressedBody = {36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 59, 50};
    private Map<String, List<String>> headersMap = new HashMap<String, List<String>>() {{
        put(CONTENT_ENCODING, Collections.singletonList(ENCODING_ENCRYPTED));
    }};
    private Map<String, List<String>> lowercaseHeadersMap = new HashMap<String, List<String>>() {{
        put("content-encoding", Collections.singletonList(ENCODING_ENCRYPTED));
    }};
    private StartupNetworkResponseHandler mResponseHandler;

    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String ENCODING_ENCRYPTED = "encrypted";

    private static PrintStream logsStream;

    @BeforeClass
    public static void setUpLogs() {
        logsStream = ShadowLog.stream;
        ShadowLog.stream = System.out;
    }

    @AfterClass
    public static void removeLogs() {
        ShadowLog.stream = logsStream;
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(responseDataHolder.getResponseCode()).thenReturn(HttpsURLConnection.HTTP_OK);
        when(responseDataHolder.getResponseData()).thenReturn(mResponseBody);
        when(responseDataHolder.getResponseHeaders()).thenReturn(mHeaders);

        mResponseHandler = new StartupNetworkResponseHandler(mStartupParser, mBodyDecoder);

        when(mStartupParser.parseStartupResponse(mResponseBody)).thenReturn(mResult);
        when(mResult.getResult()).thenReturn(StartupResult.Result.OK);
        when(mBodyDecoder.decode(eq(mResponseBody), eq(mPassword))).thenReturn(mUncompressedBody);
    }

    @Test
    public void testHandleWithoutEncodingHeader() {
        assertValidResponseWithoutEncryption();
    }

    @Test
    public void testHandleWithErrorResponseCode() {
        when(responseDataHolder.getResponseCode()).thenReturn(304);
        assertThat(mResponseHandler.handle(responseDataHolder)).isNull();
        verifyNoMoreInteractions(mStartupParser);
    }

    @Test
    public void testHandleForErrorParsing() {
        when(mResult.getResult()).thenReturn(StartupResult.Result.BAD);
        assertThat(mResponseHandler.handle(responseDataHolder)).isNull();
        verify(mStartupParser, times(1)).parseStartupResponse(mResponseBody);
        verifyNoMoreInteractions(mStartupParser);
    }

    @Test
    public void testHandleForNullBody() {
        when(responseDataHolder.getResponseData()).thenReturn(null);
        assertThat(mResponseHandler.handle(responseDataHolder)).isNull();
        verifyNoInteractions(mStartupParser);
    }

    @Test
    public void testHandleForNullContentEncodingHeader() {
        when(mHeaders.get(CONTENT_ENCODING)).thenReturn(null);
        assertValidResponseWithoutEncryption();
    }

    @Test
    public void testHandleForEmptyContentEncodingHeader() {
        when(mHeaders.get(CONTENT_ENCODING)).thenReturn(new ArrayList<String>());
        assertValidResponseWithoutEncryption();
    }

    @Test
    public void testHandleForUnexpectedContentEncodingHeader() {
        when(mHeaders.get(CONTENT_ENCODING)).thenReturn(Arrays.asList("Custom content encoding"));
        assertValidResponseWithoutEncryption();
    }

    @Test
    public void testHandleForLowercaseContentEncodingHeader() {
        when(responseDataHolder.getResponseHeaders()).thenReturn(lowercaseHeadersMap);
        when(mStartupParser.parseStartupResponse(mUncompressedBody)).thenReturn(mResult);
        assertValidResponseWithEncryption();
    }

    @Test
    public void testHandleForSuccessfulDecryption() throws Exception {
        when(responseDataHolder.getResponseHeaders()).thenReturn(mHeaders);
        when(responseDataHolder.getResponseHeaders()).thenReturn(headersMap);
        when(mStartupParser.parseStartupResponse(mUncompressedBody)).thenReturn(mResult);
        assertValidResponseWithEncryption();
    }

    @Test
    public void testHandleForSuccessfulDecryptionNoListeners() throws Exception {
        mResponseHandler = new StartupNetworkResponseHandler(mStartupParser, mBodyDecoder);
        when(responseDataHolder.getResponseHeaders()).thenReturn(headersMap);
        when(mStartupParser.parseStartupResponse(mUncompressedBody)).thenReturn(mResult);
        assertValidResponseWithEncryption();
        verifyNoMoreInteractions(mStartupParser);
    }

    @Test
    public void testHandleForFailedDecryption() throws Exception {
        when(responseDataHolder.getResponseHeaders()).thenReturn(headersMap);
        when(mBodyDecoder.decode(eq(mResponseBody), eq(mPassword))).thenReturn(null);
        assertThat(mResponseHandler.handle(responseDataHolder)).isNull();
        verify(mBodyDecoder).decode(eq(mResponseBody), eq(mPassword));
        verifyNoMoreInteractions(mStartupParser);
    }

    @Test
    public void testDecryption() {
        mResponseBody = new byte[]{105, 21, -11, -110, 40, -75, -45, 31, 43, 69, -99, -54, -42, -76, -29, -123, -113,
                54, -113, 8, -56, -24, 6, -9, -96, 80, -89, 63, -63, -76, 45, 15, 20, 69, -115, -63, -3, -88, 97, -122,
                125, -50, -90, -127, 16, 10, 88, -8};
        when(responseDataHolder.getResponseData()).thenReturn(mResponseBody);
        when(responseDataHolder.getResponseHeaders()).thenReturn(headersMap);
        when(mBodyDecoder.decode(eq(mResponseBody), eq(mPassword))).thenReturn("TEST DATA".getBytes());
        when(mStartupParser.parseStartupResponse("TEST DATA".getBytes())).thenReturn(mResult);
        mResponseHandler = new StartupNetworkResponseHandler(mStartupParser, mBodyDecoder);
        assertThat(mResponseHandler.handle(responseDataHolder)).isSameAs(mResult);
        verify(mStartupParser, times(1)).parseStartupResponse("TEST DATA".getBytes());
    }

    private void assertValidResponseWithEncryption() {
        assertThat(mResponseHandler.handle(responseDataHolder)).isSameAs(mResult);
        verify(mBodyDecoder).decode(mResponseBody, mPassword);
        verify(mStartupParser, times(1)).parseStartupResponse(mUncompressedBody);
    }

    private void assertValidResponseWithoutEncryption() {
        assertThat(mResponseHandler.handle(responseDataHolder)).isSameAs(mResult);
        verify(mStartupParser, times(1)).parseStartupResponse(mResponseBody);
    }
}
