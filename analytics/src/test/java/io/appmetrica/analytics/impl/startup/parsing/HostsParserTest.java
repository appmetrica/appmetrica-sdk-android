package io.appmetrica.analytics.impl.startup.parsing;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class HostsParserTest extends CommonTest {

    private final StartupResult mResult = new StartupResult();
    private final HostsParser mHostsParser = new HostsParser();
    private final List<String> mStartupHosts = Arrays.asList("startup.host.1.by", "startup.host.2.ru");
    private final List<String> mReportHosts = Arrays.asList("report.host.1.by", "report.host.2.ru");
    private final String mReportAdHost = "report.ad.host.by";
    private final String mGetAdHost = "get.ad.host.by";
    private final String customSdkHostKey1 = "am";
    private final String customSdkHostKey2 = "ads";
    private final List<String> customSdkHostsValue1 = Arrays.asList("host1", "host2");
    private final List<String> customSdkHostsValue2 = Collections.singletonList("host3");
    private final Map<String, List<String>> customSdkHosts = new HashMap<>();

    @Before
    public void setUp() {
        customSdkHosts.put(customSdkHostKey1, customSdkHostsValue1);
        customSdkHosts.put(customSdkHostKey2, customSdkHostsValue2);
    }

    @Test
    public void testAllHostsFilled() throws Exception {
        final StartupJsonMock response = new StartupJsonMock();
        response.setStartupHosts(mStartupHosts);
        response.setReportHosts(mReportHosts);
        response.setReportAdHost(mReportAdHost);
        response.setGetAdHost(mGetAdHost);
        response.setHosts(customSdkHostKey1, customSdkHostsValue1);
        response.setHosts(customSdkHostKey2, customSdkHostsValue2);

        mHostsParser.parse(mResult, response);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(mResult.getStartupUrls()).isEqualTo(mStartupHosts);
        softly.assertThat(mResult.getReportHostUrls()).isEqualTo(mReportHosts);
        softly.assertThat(mResult.getReportAdUrl()).isEqualTo(mReportAdHost);
        softly.assertThat(mResult.getGetAdUrl()).isEqualTo(mGetAdHost);
        softly.assertThat(mResult.getCustomSdkHosts()).isEqualTo(customSdkHosts);
        softly.assertAll();
    }

    @Test
    public void testAllHostsEmptyLists() throws Exception {
        final StartupJsonMock response = new StartupJsonMock();
        response.setStartupHosts(new ArrayList<String>());
        response.setDiagnosticHosts(new ArrayList<String>());
        response.setReportHosts(new ArrayList<String>());

        mHostsParser.parse(mResult, response);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(mResult.getStartupUrls()).isNull();
        softly.assertThat(mResult.getReportHostUrls()).isNull();
        softly.assertThat(mResult.getDiagnosticUrls()).isNull();
        softly.assertThat(mResult.getCustomSdkHosts()).isEmpty();
        softly.assertAll();
    }

    @Test
    public void testAllHostsNull() throws Exception {
        final StartupJsonMock response = new StartupJsonMock();
        response.setStartupHosts(null);
        response.setDiagnosticHosts(null);
        response.setReportHosts(null);
        response.setReportAdHost(null);
        response.setGetAdHost(null);

        mHostsParser.parse(mResult, response);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(mResult.getStartupUrls()).isNull();
        softly.assertThat(mResult.getReportHostUrls()).isNull();
        softly.assertThat(mResult.getDiagnosticUrls()).isNull();
        softly.assertThat(mResult.getReportAdUrl()).isEmpty();
        softly.assertThat(mResult.getGetAdUrl()).isEmpty();
        softly.assertThat(mResult.getCustomSdkHosts()).isEmpty();
        softly.assertAll();
    }

    @Test
    public void testMissingQueryHostsJson() throws Exception {
        final StartupJsonMock response = new StartupJsonMock();
        response.removeHostsJson();

        mHostsParser.parse(mResult, response);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(mResult.getStartupUrls()).isNull();
        softly.assertThat(mResult.getReportHostUrls()).isNull();
        softly.assertThat(mResult.getDiagnosticUrls()).isNull();
        softly.assertThat(mResult.getReportAdUrl()).isEmpty();
        softly.assertThat(mResult.getGetAdUrl()).isEmpty();
        softly.assertThat(mResult.getCustomSdkHosts()).isNull();
        softly.assertAll();
    }

    @Test
    public void testMissingQueryHostsListJson() throws Exception {
        final StartupJsonMock response = new StartupJsonMock();
        response.removeHostsListJson();

        mHostsParser.parse(mResult, response);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(mResult.getStartupUrls()).isNull();
        softly.assertThat(mResult.getReportHostUrls()).isNull();
        softly.assertThat(mResult.getDiagnosticUrls()).isNull();
        softly.assertThat(mResult.getReportAdUrl()).isEmpty();
        softly.assertThat(mResult.getGetAdUrl()).isEmpty();
        softly.assertThat(mResult.getCustomSdkHosts()).isNull();
        softly.assertAll();
    }
}
