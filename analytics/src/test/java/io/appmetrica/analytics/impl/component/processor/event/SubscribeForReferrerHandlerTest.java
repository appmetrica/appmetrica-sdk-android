package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SubscribeForReferrerHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit mComponentUnit;
    private SubscribeForReferrerHandler mSubscribeForReferrerHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mSubscribeForReferrerHandler = new SubscribeForReferrerHandler(mComponentUnit);
    }

    @Test
    public void process() {
        CounterReport report = mock(CounterReport.class);
        assertThat(mSubscribeForReferrerHandler.process(report)).isFalse();
        verify(mComponentUnit).subscribeForReferrer();
    }
}
