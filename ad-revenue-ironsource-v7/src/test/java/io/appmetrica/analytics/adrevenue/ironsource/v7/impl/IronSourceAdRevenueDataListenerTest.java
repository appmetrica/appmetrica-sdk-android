package io.appmetrica.analytics.adrevenue.ironsource.v7.impl;

import com.ironsource.mediationsdk.impressionData.ImpressionData;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.common.InternalClientModuleFacade;
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
    public final InternalClientModuleFacade internalClientModuleFacade = mock(InternalClientModuleFacade.class);
    public final ModuleAdRevenue adRevenue = mock(ModuleAdRevenue.class);

    public AdRevenueConverter converter;
    public IronSourceAdRevenueDataListener listener;

    @Before
    public void setUp() {
        listener = new IronSourceAdRevenueDataListener(clientContext);
        assertThat(converterRule.getConstructionMock().constructed()).hasSize(1);
        converter = converterRule.getConstructionMock().constructed().get(0);

        when(clientContext.getInternalClientModuleFacade()).thenReturn(internalClientModuleFacade);
        when(converter.convert(impressionData)).thenReturn(adRevenue);
        when(impressionData.getAllData()).thenReturn(new JSONObject());
    }

    @Test
    public void onShow() {
        listener.onImpressionSuccess(impressionData);
        verify(internalClientModuleFacade).reportAdRevenue(adRevenue);
    }

    @Test
    public void onShowIfDataIsNull() {
        listener.onImpressionSuccess(null);
        verifyNoInteractions(clientContext, converter);
    }
}
