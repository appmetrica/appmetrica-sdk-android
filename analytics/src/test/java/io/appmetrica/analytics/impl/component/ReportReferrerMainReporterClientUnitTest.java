package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.protobuf.client.ReferrerInfoClient;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ReportReferrerMainReporterClientUnitTest extends CommonTest {

    private MainReporterComponentUnit mComponentUnit;
    private MainReporterComponentUnit.MainReporterListener mReportReferrerHandler;
    private static final String TEST_REFERRER = "test_referrer";

    @Before
    public void setUp() {
        mComponentUnit = mock(MainReporterComponentUnit.class);
        mReportReferrerHandler = mComponentUnit.new MainReporterListener();
    }

    @Test
    public void testProcessReportShouldCreateReferrerReportIfReferrerExists() {

        mReportReferrerHandler.handleReferrer(new ReferrerInfo(TEST_REFERRER, 0, 0, ReferrerInfo.Source.GP));

        ArgumentCaptor<CounterReport> argument = ArgumentCaptor.forClass(CounterReport.class);
        verify(mComponentUnit, times(1)).handleReport(argument.capture());
        assertThat(argument.getValue().getValueBytes()).is(new Condition<byte[]>() {
            @Override
            public boolean matches(byte[] value) {
                try {
                    ReferrerInfoClient client = ReferrerInfoClient.parseFrom(value);
                    return TEST_REFERRER.equals(client.value);
                } catch (InvalidProtocolBufferNanoException e) {
                    return false;
                }
            }
        });
        assertThat(argument.getValue().getType()).isEqualTo(InternalEvents.EVENT_TYPE_SEND_REFERRER
            .getTypeId());
    }

    @Test
    public void shouldDoNothingOnNullReferrer() {
        mReportReferrerHandler.handleReferrer(null);
        verify(mComponentUnit, never()).handleReport(any(CounterReport.class));
    }
}
