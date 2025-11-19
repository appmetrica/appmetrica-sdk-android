package io.appmetrica.analytics.adrevenue.ironsource.v9.impl;

import com.ironsource.mediationsdk.IronSource;
import com.unity3d.mediation.LevelPlay;
import io.appmetrica.analytics.adrevenue.ironsource.v9.impl.IronSourceAdRevenueAdapter;
import io.appmetrica.analytics.adrevenue.ironsource.v9.impl.IronSourceAdRevenueDataListener;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class IronSourceAdRevenueAdapterTest extends CommonTest {

    @Rule
    public final MockedStaticRule<IronSource> ironSourceRule = new MockedStaticRule<>(IronSource.class);
    @Rule
    public final MockedConstructionRule<IronSourceAdRevenueDataListener> listenerRule =
        new MockedConstructionRule<>(IronSourceAdRevenueDataListener.class);

    @Test
    public void registerListener() {
        final ClientContext clientContext = mock(ClientContext.class);
        IronSourceAdRevenueAdapter.registerListener(clientContext);

        assertThat(listenerRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(listenerRule.getArgumentInterceptor().flatArguments()).containsExactly(clientContext);

        final IronSourceAdRevenueDataListener listener = listenerRule.getConstructionMock().constructed().get(0);

        ironSourceRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                LevelPlay.addImpressionDataListener(listener);
            }
        });
    }
}
