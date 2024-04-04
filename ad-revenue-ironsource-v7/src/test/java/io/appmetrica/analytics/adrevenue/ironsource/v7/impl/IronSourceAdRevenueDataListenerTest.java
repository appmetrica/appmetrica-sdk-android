package io.appmetrica.analytics.adrevenue.ironsource.v7.impl;

import com.ironsource.mediationsdk.impressionData.ImpressionData;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenueReporter;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class IronSourceAdRevenueDataListenerTest extends CommonTest {

    @Rule
    public final MockedConstructionRule<AdRevenueConverter> converterRule =
        new MockedConstructionRule<>(AdRevenueConverter.class);

    public final ClientContext clientContext = mock(ClientContext.class);
    public final ImpressionData impressionData = mock(ImpressionData.class);
    public final AutoAdRevenueReporter autoAdRevenueReporter = mock(AutoAdRevenueReporter.class);
    public final AutoAdRevenue autoAdRevenue = mock(AutoAdRevenue.class);

    public AdRevenueConverter converter;
    public IronSourceAdRevenueDataListener listener;

    @Before
    public void setUp() {
        listener = new IronSourceAdRevenueDataListener(clientContext);
        assertThat(converterRule.getConstructionMock().constructed()).hasSize(1);
        converter = converterRule.getConstructionMock().constructed().get(0);

        when(clientContext.getAutoAdRevenueReporter()).thenReturn(autoAdRevenueReporter);
        when(converter.convert(impressionData)).thenReturn(autoAdRevenue);
        when(impressionData.getAllData()).thenReturn(new JSONObject());
    }

    @Test
    public void onShow() {
        listener.onImpressionSuccess(impressionData);
        verify(autoAdRevenueReporter).reportAutoAdRevenue(autoAdRevenue);
    }

    @Test
    public void onShowIfDataIsNull() {
        listener.onImpressionSuccess(null);
        verifyNoInteractions(clientContext, converter);
    }
}
