package io.appmetrica.analytics.impl;

import android.os.SystemClock;
import android.util.Base64;
import io.appmetrica.analytics.impl.revenue.ad.AdRevenueWrapper;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.RandomStringGenerator;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Random;
import kotlin.Pair;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ClientCounterReportTest extends CommonTest {

    @Mock
    private PublicLogger mPublicLogger;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLongNameIsTruncated() {
        String name = generateLongEventName();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setName(name);
        assertThat(report.getName().length()).isLessThan(name.length());
    }

    @Test
    public void testLongNameContainsNameFromReportAfterTruncate() {
        String name = generateLongEventName();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setName(name);
        assertThat(name.contains(report.getName())).isTrue();
    }

    @Test
    public void testLongNameAfterAddingLessOrEqualsLimit() {
        String name = generateLongEventName();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setName(name);
        assertThat(report.getName().length()).isLessThanOrEqualTo(EventLimitationProcessor.EVENT_NAME_MAX_LENGTH);
    }

    @Test
    public void testCalculateValidBytesTruncatedForLongName() {
        String name = generateLongEventName();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setName(name);
        assertThat(report.getBytesTruncated()).isEqualTo(name.getBytes().length - report.getName().getBytes().length);
    }

    @Test
    public void testShortNameNotTruncated() {
        String name = generateShortEventName();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setName(name);
        assertThat(report.getName()).isEqualTo(name);
    }

    @Test
    public void testNotCalculateBytesTruncatedForShortName() {
        String name = generateShortEventName();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setName(name);
        assertThat(report.getBytesTruncated()).isZero();
    }

    @Test
    public void testCalculateValidBytesTruncatedForLongNameAfterLongName() {
        String firstName = generateLongEventName();
        String secondName = generateLongEventName();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setName(firstName);
        report.setName(secondName);
        assertThat(report.getBytesTruncated()).isEqualTo(secondName.getBytes().length - report.getName().getBytes().length);
    }

    @Test
    public void testCalculateValidBytesTruncatedForLongNameAfterShortName() {
        String first = generateShortEventName();
        String second = generateLongEventName();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setName(first);
        report.setName(second);
        assertThat(report.getBytesTruncated()).isEqualTo(second.getBytes().length - report.getName().getBytes().length);
    }

    @Test
    public void testCalculateZeroBytesTruncatedForShortNameAfterLongName() {
        String first = generateLongEventName();
        String second = generateShortEventName();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setName(first);
        report.setName(second);
        assertThat(report.getBytesTruncated()).isZero();
    }

    @Test
    public void testCalculateZeroBytesTruncatedForNullNameAfterLongName() {
        String first = generateLongEventValue();
        String second = null;
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setName(first);
        report.setName(second);
        assertThat(report.getBytesTruncated()).isZero();
    }

    @Test
    public void testLongReportIsTruncated() {
        String value = generateLongEventValue();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setValue(value);
        assertThat(report.getValue().length()).isLessThan(value.length());
    }

    @Test
    public void testLongReportPartiallyAddedToReport() {
        String value = generateLongEventValue();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setValue(value);
        assertThat(value.contains(report.getValue())).isTrue();
    }

    @Test
    public void testLongReportWillLessOrEqualsLimit() {
        String value = generateLongEventValue();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setValue(value);
        assertThat(report.getValue().length()).isLessThanOrEqualTo(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE);
    }

    @Test
    public void testCalculateValidBytesTruncatedForLongValue() {
        String value = generateLongEventValue();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setValue(value);
        assertThat(report.getBytesTruncated()).isEqualTo(value.getBytes().length - report.getValue().getBytes().length);
    }

    @Test
    public void testShortReportValueNotTruncated() {
        String value = generateShortEventValue();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setValue(value);
        assertThat(report.getValue()).isEqualTo(value);
    }

    @Test
    public void testNotCalculateBytesTruncatedForShortReportValue() {
        String value = generateShortEventValue();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setValue(value);
        assertThat(report.getBytesTruncated()).isZero();
    }

    @Test
    public void testCalculateValidBytesTruncatedForLongReportValueAfterLongReportValue() {
        String first = generateLongEventValue();
        String second = generateLongEventName();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setValue(first);
        report.setValue(second);
        assertThat(report.getBytesTruncated()).isEqualTo(second.getBytes().length - report.getValue().getBytes().length);
    }

    @Test
    public void testCalculateValidBytesTruncatedForLongReportValueAfterShortReportValue() {
        String first = generateShortEventValue();
        String second = generateLongEventValue();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setValue(first);
        report.setValue(second);
        assertThat(report.getBytesTruncated()).isEqualTo(second.getBytes().length - report.getValue().getBytes().length);
    }

    @Test
    public void testCalculateZeroBytesTruncatedForShortReportValueAfterLongReportValue() {
        String first = generateLongEventValue();
        String second = generateShortEventValue();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setValue(first);
        report.setValue(second);
        assertThat(report.getBytesTruncated()).isZero();
    }

    @Test
    public void testCalculateZeroBytesTruncatedForNullReportValueAfterLongReportValue() {
        String first = generateLongEventValue();
        String second = null;
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setValue(first);
        report.setValue(second);
        assertThat(report.getBytesTruncated()).isZero();
    }

    @Test
    public void testCalculateValidBytesTruncatedForSomeFields() {
        String name = generateLongEventName();
        String value = generateLongEventValue();
        String userInfo = generateLongUserInfo();
        CounterReport report = new ClientCounterReport(mPublicLogger);
        report.setName(name);
        report.setValue(value);
        int expectedTruncatedBytes = 0;
        expectedTruncatedBytes += name.getBytes().length - report.getName().getBytes().length;
        expectedTruncatedBytes += value.getBytes().length - report.getValue().getBytes().length;
        assertThat(report.getBytesTruncated()).isEqualTo(expectedTruncatedBytes);
    }

    @Test
    public void testTrimmedFieldsAreEmpty() {
        String name = "name";
        String value = "value";
        ClientCounterReport report = new ClientCounterReport(value, name, 0, mPublicLogger);
        assertThat(report.getTrimmedFields()).isEmpty();
    }

    @Test
    public void testTrimmedFieldsForSomeFields() {
        String name = generateLongEventName();
        String value = generateLongEventValue();
        ClientCounterReport report = new ClientCounterReport(value, name, 0, mPublicLogger);
        assertThat(report.getTrimmedFields()).containsOnly(
                new AbstractMap.SimpleEntry(ClientCounterReport.TrimmedField.NAME, name.getBytes().length - report.getName().getBytes().length),
                new AbstractMap.SimpleEntry(ClientCounterReport.TrimmedField.VALUE, value.getBytes().length - report.getValue().getBytes().length)
        );
    }

    @Test
    public void testTrimmedFieldsArePassed() {
        final HashMap<ClientCounterReport.TrimmedField, Integer> map = new HashMap<ClientCounterReport.TrimmedField, Integer>();
        map.put(ClientCounterReport.TrimmedField.VALUE, 1000);
        ClientCounterReport report = new ClientCounterReport(mPublicLogger).withTrimmedFields(map);
        assertThat(report.getTrimmedFields()).isEqualTo(map);
    }

    @Test
    public void testDefaultConstructorCreationElapsedRealtime() {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        assertThat(new ClientCounterReport(mPublicLogger).getCreationElapsedRealtime() - elapsedRealtime).isLessThan(1000);
    }

    @Test
    public void testCreationElapsedRealtime() {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        ClientCounterReport clientCounterReport = new ClientCounterReport("Test value", 0, mPublicLogger);
        assertThat(clientCounterReport.getCreationElapsedRealtime()
                - elapsedRealtime).isLessThan(1000);
    }

    @Test
    public void testAddingCreationElapsedRealtime() {
        long expected = 43534545L;
        ClientCounterReport report = new ClientCounterReport(mPublicLogger);
        report.setCreationEllapsedRealtime(expected);
        assertThat(report.getCreationElapsedRealtime()).isEqualTo(expected);
    }

    @Test
    public void testCreationTimestamp() {
        long currentTimestamp = System.currentTimeMillis();
        ClientCounterReport clientCounterReport = new ClientCounterReport("Test value", 0, mPublicLogger);
        assertThat(clientCounterReport.getCreationTimestamp() - currentTimestamp).isLessThan(1000);
    }

    @Test
    public void testDefaultConstructorCreationTimestamp() {
        long currentTimestamp = System.currentTimeMillis();
        assertThat(new ClientCounterReport(mPublicLogger).getCreationTimestamp() - currentTimestamp).isLessThan(1000);
    }

    @Test
    public void testAddingCreationTimestamp() {
        long expected = 43454535L;
        ClientCounterReport report = new ClientCounterReport(mPublicLogger);
        report.setCreationTimestamp(expected);
        assertThat(report.getCreationTimestamp()).isEqualTo(expected);
    }

    @Test
    public void testUserProfileIDWasNotTrimmed() {
        String original = generateString(EventLimitationProcessor.USER_PROFILE_ID_MAX_LENGTH - 1);
        assertThat(EventLimitationProcessor.valueWasTrimmed(
                original,
                ClientCounterReport.formSetUserProfileIDEvent(original, mPublicLogger).getProfileID()
        )).isFalse();
    }

    @Test
    public void testUserProfileIDTrimmed() {
        String original = generateString(EventLimitationProcessor.USER_PROFILE_ID_MAX_LENGTH + 1);
        assertThat(EventLimitationProcessor.valueWasTrimmed(
                original,
                ClientCounterReport.formSetUserProfileIDEvent(original, mPublicLogger).getProfileID()
        )).isTrue();
    }

    @Test
    public void testLongValueBytesTruncated() {
        byte[] original = generateByteArray(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE + 10);
        ClientCounterReport report = new ClientCounterReport(mPublicLogger);
        report.setValueBytes(original);

        SoftAssertions assertion = new SoftAssertions();
        assertion.assertThat(report.getValueBytes()).as("value").isEqualTo(generateByteArray(
                EventLimitationProcessor.REPORT_VALUE_MAX_SIZE
        ));
        assertion.assertThat(report.getBytesTruncated()).as("bytes truncated").isEqualTo(10);
        assertion.assertAll();
    }

    @Test
    public void testFormUserProfileEvent() {
        CounterReport counterReport = ClientCounterReport.formUserProfileEvent();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(counterReport.getType())
                .isEqualTo(InternalEvents.EVENT_TYPE_SEND_USER_PROFILE.getTypeId());
        softAssertions.assertThat(counterReport.getName()).isEmpty();
        softAssertions.assertThat(counterReport.getValue()).isEmpty();
        softAssertions.assertAll();
    }

    @Test
    public void withExtendedValueLessThanLimit() {
        String original = generateString(EventLimitationProcessor.REPORT_EXTENDED_VALUE_MAX_SIZE);
        ClientCounterReport report = new ClientCounterReport(mPublicLogger);
        report.withExtendedValue(original);

        SoftAssertions assertion = new SoftAssertions();
        assertion.assertThat(report.getValue()).as("value").isEqualTo(original);
        assertion.assertThat(report.getBytesTruncated()).as("bytes truncated").isZero();
        assertion.assertAll();
    }

    @Test
    public void withExtendedValueMoreThanLimit() {
        String fittingPart = generateString(EventLimitationProcessor.REPORT_EXTENDED_VALUE_MAX_SIZE);
        String original = fittingPart + "aaaaabbbbb";
        ClientCounterReport report = new ClientCounterReport(mPublicLogger);
        report.withExtendedValue(original);

        SoftAssertions assertion = new SoftAssertions();
        assertion.assertThat(report.getValue()).as("value").isEqualTo(fittingPart);
        assertion.assertThat(report.getBytesTruncated()).as("bytes truncated").isEqualTo(10);
        assertion.assertAll();
    }

    @Test
    public void formJsEvent() {
        String eventName = "Event name";
        String eventValue = "Event value";
        CounterReport counterReport = ClientCounterReport.formJsEvent(eventName, eventValue, mPublicLogger);

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(counterReport.getType())
                .isEqualTo(InternalEvents.EVENT_TYPE_REGULAR.getTypeId());
        softAssertions.assertThat(counterReport.getName()).isEqualTo(eventName);
        softAssertions.assertThat(counterReport.getValue()).isEqualTo(eventValue);
        softAssertions.assertThat(counterReport.getSource()).isEqualTo(EventSource.JS);
        softAssertions.assertAll();
    }

    @Test
    public void testAdRevenueEvent() {
        final AdRevenueWrapper adRevenueWrapper = mock(AdRevenueWrapper.class);
        final byte[] bytes = "some_data".getBytes(StandardCharsets.UTF_8);
        final int bytesTruncated = 42;
        when(adRevenueWrapper.getDataToSend()).thenReturn(new Pair<>(bytes, bytesTruncated));

        final CounterReport report = ClientCounterReport.formAdRevenueEvent(mock(PublicLogger.class), adRevenueWrapper);

        assertThat(report.getType()).isEqualTo(InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT.getTypeId());
        assertThat(report.getValue()).isEqualTo(new String(Base64.encode(bytes, 0)));
        assertThat(report.getBytesTruncated()).isEqualTo(bytesTruncated);
    }

    private byte[] generateByteArray(int length) {
        byte[] array = new byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = (byte) (i % 256);
        }
        return array;
    }

    private String generateLongEventValue() {
        return generateString(new Random().nextInt(10000) + EventLimitationProcessor.REPORT_VALUE_MAX_SIZE + 1);
    }

    private String generateShortEventValue() {
        return generateString(new Random().nextInt(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE - 1) + 1);
    }

    private String generateLongEventName() {
        return generateString(new Random().nextInt(10000) + EventLimitationProcessor.EVENT_NAME_MAX_LENGTH + 1);
    }

    private String generateShortEventName() {
        return generateString(new Random().nextInt(EventLimitationProcessor.EVENT_NAME_MAX_LENGTH - 1) + 1);
    }

    private String generateLongUserInfo() {
        return generateString(new Random().nextInt(10000) + EventLimitationProcessor.USER_INFO_MAX_LENGTH + 1);
    }

    private String generateShortUserInfo() {
        return generateString(new Random().nextInt(EventLimitationProcessor.USER_INFO_MAX_LENGTH - 1) + 1);
    }

    private String generateString(int size) {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator(size);
        return randomStringGenerator.nextString();
    }
}
