package io.appmetrica.analytics.adrevenue.fyber.v3.impl;

import com.fyber.fairbid.ads.ImpressionData;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenueReporter;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class FyberAdRevenueDataListenerTest extends CommonTest {

    @Rule
    public final MockedConstructionRule<AdRevenueConverter> converterRule =
        new MockedConstructionRule<>(AdRevenueConverter.class);

    public final ClientContext clientContext = mock(ClientContext.class);
    public final ImpressionData impressionData = mock(ImpressionData.class);
    public final AutoAdRevenueReporter autoAdRevenueReporter = mock(AutoAdRevenueReporter.class);
    public final AutoAdRevenue autoAdRevenue = mock(AutoAdRevenue.class);

    public AdRevenueConverter converter;
    public FyberAdRevenueDataListener listener;

    @Before
    public void setUp() {
        listener = new FyberAdRevenueDataListener(clientContext);
        assertThat(converterRule.getConstructionMock().constructed()).hasSize(1);
        converter = converterRule.getConstructionMock().constructed().get(0);

        when(clientContext.getAutoAdRevenueReporter()).thenReturn(autoAdRevenueReporter);
        when(converter.convert(impressionData)).thenReturn(autoAdRevenue);
    }

    @Test
    public void onShow() {
        listener.onShow("some string", impressionData);
        verify(autoAdRevenueReporter).reportAutoAdRevenue(autoAdRevenue);
    }

    @Test
    public void onShowIfDataIsNull() {
        listener.onShow("some string", null);
        verifyNoInteractions(clientContext, converter);
    }
}
