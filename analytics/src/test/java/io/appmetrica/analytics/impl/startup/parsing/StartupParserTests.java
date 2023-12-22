package io.appmetrica.analytics.impl.startup.parsing;

import io.appmetrica.analytics.impl.IOUtils;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.impl.db.state.converter.StatSendingConverter;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StatSending;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static io.appmetrica.analytics.impl.startup.parsing.StartupResult.Result;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class StartupParserTests extends CommonTest {

    @Mock
    private HostsParser mHostsParser;
    @Mock
    private FeaturesParser mFeaturesParser;
    @Mock
    private RetryPolicyConfigParser mRetryPolicyConfigParser;
    @Mock
    private PermissionsCollectingConfigParser mPermissionsCollectingConfigParser;
    @Mock
    private CacheControlParser mCacheControlParser;
    @Mock
    private AutoInappCollectingConfigParser autoInappCollectingConfigParser;
    @Mock
    private AttributionConfigParser attributionConfigParser;
    @Mock
    private StartupUpdateConfigParser startupUpdateConfigParser;
    @Mock
    private ModulesRemoteConfigsParser modulesRemoteConfigsParser;
    @Mock
    private ExternalAttributionConfigParser externalAttributionConfigParser;

    private StartupParser mStartupParser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mStartupParser = new StartupParser(
            new JsonResponseProvider(),
            mHostsParser,
            mFeaturesParser,
            mRetryPolicyConfigParser,
            mPermissionsCollectingConfigParser,
            new StatSendingConverter(),
            mCacheControlParser,
            autoInappCollectingConfigParser,
            attributionConfigParser,
            startupUpdateConfigParser,
            modulesRemoteConfigsParser,
            externalAttributionConfigParser
        );
    }

    @Test
    public void testAllFields() throws Exception {
        long statSendingInterval = 55;
        long maxValidTimeDifference = 77;
        String sdkListUrl = "sdk.list.url";
        String certificateUrl = "certificate.url";

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                StartupResult startupResult = invocation.getArgument(0);
                startupResult.setCollectingFlags(
                    new CollectingFlags.CollectingFlagsBuilder()
                        .build()
                );
                return startupResult;
            }
        }).when(mFeaturesParser).parse(any(StartupResult.class), any(JsonHelper.OptJSONObject.class));

        final StartupJsonMock response = new StartupJsonMock();
        response.addStatSendingDisabledReportingInterval(statSendingInterval);
        response.addMaxValidDifferenceSeconds(maxValidTimeDifference);
        response.setCertificateUrl(certificateUrl);

        StartupResult result = mStartupParser.parseStartupResponse(jsonToBytes(response));

        assertThat(result.getResult()).isEqualTo(Result.OK);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getCertificateUrl()).isEqualTo(certificateUrl);
        softly.assertThat(result.getDeviceId()).isEqualTo(response.testDeviceId);
        softly.assertThat(result.getDeviceIDHash()).isEqualTo(response.testDeviceHash);
        softly.assertThat(result.getEncodedClids()).isEqualTo(StartupUtils.encodeClids(response.testClids));
        softly.assertThat(result.getValidTimeDifference()).isEqualTo(maxValidTimeDifference);
        softly.assertThat(result.getStatSending()).isEqualToComparingFieldByField(new StatSending(statSendingInterval * 1000));
        softly.assertAll();

        ArgumentCaptor<JsonHelper.OptJSONObject> jsonCaptor = ArgumentCaptor.forClass(JsonHelper.OptJSONObject.class);
        verify(mFeaturesParser).parse(any(StartupResult.class), jsonCaptor.capture());
        verify(mHostsParser).parse(any(StartupResult.class), jsonCaptor.capture());
        verify(mPermissionsCollectingConfigParser).parseIfEnabled(any(StartupResult.class), jsonCaptor.capture());
        verify(mRetryPolicyConfigParser).parse(any(StartupResult.class), jsonCaptor.capture());
        verify(autoInappCollectingConfigParser).parse(any(StartupResult.class), jsonCaptor.capture());
        verify(attributionConfigParser).parse(any(StartupResult.class), jsonCaptor.capture());
        verify(startupUpdateConfigParser).parse(any(StartupResult.class), jsonCaptor.capture());

        for (JsonHelper.OptJSONObject value : jsonCaptor.getAllValues()) {
            JSONAssert.assertEquals(response, value, true);
        }
    }

    @Test
    public void testEmptyDeviceId() throws Exception {
        StartupJsonMock response = new StartupJsonMock();
        response.setDeviceId("", response.testDeviceHash);
        StartupResult parseResult = mStartupParser.parseStartupResponse(jsonToBytes(response));
        assertThat(parseResult.getDeviceId()).isEmpty();
    }

    @Test
    public void testMissingDeviceId() throws Exception {
        StartupJsonMock response = new StartupJsonMock();
        response.setDeviceId(null, response.testDeviceHash);
        StartupResult parseResult = mStartupParser.parseStartupResponse(jsonToBytes(response));
        assertThat(parseResult.getDeviceId()).isEmpty();
    }

    @Test
    public void testDeviceIdIfMissingBlock() throws Exception {
        StartupJsonMock response = new StartupJsonMock();
        response.removeDeviceIdBlock();
        StartupResult parseResult = mStartupParser.parseStartupResponse(jsonToBytes(response));
        assertThat(parseResult.getDeviceId()).isEmpty();
    }

    @Test
    public void testDeviceIdHashIfEmpty() throws Exception {
        StartupJsonMock response = new StartupJsonMock();
        response.setDeviceId(response.testDeviceId, "");
        StartupResult parseResult = mStartupParser.parseStartupResponse(jsonToBytes(response));
        assertThat(parseResult.getDeviceIDHash()).isEmpty();
    }

    @Test
    public void testDeviceIdHashIfMissing() throws Exception {
        StartupJsonMock response = new StartupJsonMock();
        response.setDeviceId(response.testDeviceId, null);
        StartupResult parseResult = mStartupParser.parseStartupResponse(jsonToBytes(response));
        assertThat(parseResult.getDeviceIDHash()).isEmpty();
    }

    @Test
    public void testDeviceIdHashIfMissingBlock() throws Exception {
        StartupJsonMock response = new StartupJsonMock();
        response.removeDeviceIdBlock();
        StartupResult parseResult = mStartupParser.parseStartupResponse(jsonToBytes(response));
        assertThat(parseResult.getDeviceIDHash()).isEmpty();
    }

    @Test
    public void testStatSendingForNullDisabledReportingInterval() throws Exception {
        StartupJsonMock response = new StartupJsonMock();
        response.addStatSendingDisabledReportingInterval(null);
        StartupResult parseResult = mStartupParser.parseStartupResponse(jsonToBytes(response));
        assertThat(parseResult.getStatSending().disabledReportingInterval).isEqualTo(18000000L);
    }

    @Test
    public void testStatSendingForNullStatSendingBlock() throws Exception {
        StartupJsonMock response = new StartupJsonMock();
        StartupResult parseResult = mStartupParser.parseStartupResponse(jsonToBytes(response));
        assertThat(parseResult.getStatSending().disabledReportingInterval).isEqualTo(18000000L);
    }

    @Test
    public void testNonJsonResponse() throws UnsupportedEncodingException {
        final StartupResult parserResult
            = mStartupParser.parseStartupResponse(stringToBytes(TestsData.NON_JSON_SERVER_RESPONSE));

        assertThat(parserResult.getResult()).isEqualTo(Result.BAD);
    }

    @Test
    public void testServerOffsetParsing() {
        // Generate response with hosts
        long obtainServerTime = System.currentTimeMillis();

        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Date", generateDateHeader(obtainServerTime));

        // Parse
        Long parsedTime = mStartupParser.parseServerTime(headers);

        assertThat(parsedTime).isNotNull();
        assertThat(parsedTime / 1000).isEqualTo(obtainServerTime / 1000);
    }

    @Test
    public void testNonServerDateHeader() {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();

        Long parsedTime = mStartupParser.parseServerTime(headers);

        assertThat(parsedTime).isNull();
    }

    @Test
    public void testMaxValidDifferenceShouldBeNullIfUndefinedFromStartup() throws Exception {
        final StartupJsonMock response = new StartupJsonMock();
        final StartupResult result = mStartupParser.parseStartupResponse(jsonToBytes(response));
        assertThat(result.getValidTimeDifference()).isNull();
    }

    @Test
    public void testMaxValidDifferenceShouldBeNullIfTimeJsonIsEmpty() throws Exception {
        final StartupJsonMock response = new StartupJsonMock();
        response.addTimeBlock(new JSONObject());
        final StartupResult result = mStartupParser.parseStartupResponse(jsonToBytes(response));
        assertThat(result.getValidTimeDifference()).isNull();
    }

    private static byte[] jsonToBytes(final JSONObject object) throws UnsupportedEncodingException {
        return stringToBytes(object.toString());
    }

    private static byte[] stringToBytes(final String string) throws UnsupportedEncodingException {
        return string.getBytes(IOUtils.UTF8_ENCODING);
    }

    private static List<String> generateDateHeader(long serverTime) {
        Date serverDate = new Date(serverTime);
        DateFormat df = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss z", Locale.US);
        String dateValue = df.format(serverDate);
        List<String> headerValues = new ArrayList<String>();
        headerValues.add(dateValue);
        return headerValues;
    }
}
