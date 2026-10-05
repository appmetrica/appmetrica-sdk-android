package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.gradle.androidtestutils.rules.ContextRule;
import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class AppMetricaCoreComponentsProviderTest extends CommonTest {

    @Rule
    public MockedConstructionRule<AppMetricaCore> appMetricaCoreConstructionRule =
        new MockedConstructionRule<>(AppMetricaCore.class);
    @Rule
    public MockedConstructionRule<AppMetricaImpl> appMetricaImplConstructionRule =
        new MockedConstructionRule<>(AppMetricaImpl.class);
    @Rule
    public ContextRule contextRule = new ContextRule();

    @Mock
    private ClientExecutorProvider clientExecutorProvider;
    @Mock
    private AppMetricaCore appMetricaCore;

    @Test
    public void cachesCore() {
        MockitoAnnotations.openMocks(this);
        Context context = contextRule.getContext();
        AppMetricaCoreComponentsProvider provider = new AppMetricaCoreComponentsProvider();

        assertThat(provider.getCore(context, clientExecutorProvider))
            .isSameAs(provider.getCore(context, clientExecutorProvider));
        assertThat(appMetricaCoreConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(context, clientExecutorProvider);
        assertThat(appMetricaCoreConstructionRule.getConstructionMock().constructed()).hasSize(1);
    }

    @Test
    public void cachesImpl() {
        MockitoAnnotations.openMocks(this);
        Context context = contextRule.getContext();
        AppMetricaCoreComponentsProvider provider = new AppMetricaCoreComponentsProvider();

        assertThat(provider.getImpl(context, appMetricaCore))
            .isSameAs(provider.getImpl(context, appMetricaCore));
        assertThat(appMetricaImplConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(context, appMetricaCore);
        assertThat(appMetricaImplConstructionRule.getConstructionMock().constructed()).hasSize(1);
    }
}
