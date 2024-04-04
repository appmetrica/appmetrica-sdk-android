package io.appmetrica.analytics.adrevenue.fyber.v3.impl;

import com.fyber.fairbid.ads.Interstitial;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FyberAdRevenueAdapterTest extends CommonTest {

    @Rule
    public final MockedStaticRule<Interstitial> interstitialRule = new MockedStaticRule<>(Interstitial.class);
    @Rule
    public final MockedConstructionRule<FyberAdRevenueDataListener> listenerRule =
        new MockedConstructionRule<>(FyberAdRevenueDataListener.class);

    @Test
    public void registerListener() {
        final ClientContext clientContext = mock(ClientContext.class);
        FyberAdRevenueAdapter.registerListener(clientContext);

        assertThat(listenerRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(listenerRule.getArgumentInterceptor().flatArguments()).containsExactly(clientContext);

        final FyberAdRevenueDataListener listener = listenerRule.getConstructionMock().constructed().get(0);

        interstitialRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                Interstitial.setInterstitialListener(listener);
            }
        });
    }
}
