package io.appmetrica.analytics.adrevenue.admob.v23.impl.processor;

import io.appmetrica.analytics.adrevenue.admob.v23.impl.AdRevenueConverter;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class AdMobAdRevenueProcessorTest extends CommonTest {

    private final String value = "value";

    @Mock
    private AdRevenueConverter converter;
    @Mock
    private ClientContext clientContext;

    @Rule
    public MockedConstructionRule<BannerAdMobAdRevenueProcessor> bannerRule =
        new MockedConstructionRule<>(BannerAdMobAdRevenueProcessor.class);
    @Rule
    public MockedConstructionRule<InterstitialAdMobAdRevenueProcessor> interstitialRule =
        new MockedConstructionRule<>(InterstitialAdMobAdRevenueProcessor.class);
    @Rule
    public MockedConstructionRule<NativeAdMobAdRevenueProcessor> nativeRule =
        new MockedConstructionRule<>(NativeAdMobAdRevenueProcessor.class);
    @Rule
    public MockedConstructionRule<RewardedAdMobAdRevenueProcessor> rewardedRule =
        new MockedConstructionRule<>(RewardedAdMobAdRevenueProcessor.class);
    @Rule
    public MockedConstructionRule<RewardedInterstitialAdMobAdRevenueProcessor> rewardedIntRule =
        new MockedConstructionRule<>(RewardedInterstitialAdMobAdRevenueProcessor.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void constructor() {
        AdMobAdRevenueProcessor processor = new AdMobAdRevenueProcessor(converter, clientContext);

        assertThat(bannerRule.getArgumentInterceptor().flatArguments())
            .containsExactly(converter, clientContext);
        assertThat(interstitialRule.getArgumentInterceptor().flatArguments())
            .containsExactly(converter, clientContext);
        assertThat(nativeRule.getArgumentInterceptor().flatArguments())
            .containsExactly(converter, clientContext);
        assertThat(rewardedRule.getArgumentInterceptor().flatArguments())
            .containsExactly(converter, clientContext);
        assertThat(rewardedIntRule.getArgumentInterceptor().flatArguments())
            .containsExactly(converter, clientContext);
    }

    @Test
    public void processIfBannerAdMobAdRevenueProcessor() {
        AdMobAdRevenueProcessor processor = new AdMobAdRevenueProcessor(converter, clientContext);

        when(bannerRule.getConstructionMock().constructed().get(0).process(value))
            .thenReturn(true);

        assertThat(processor.process(value)).isTrue();
        verifyNoInteractions(
            interstitialRule.getConstructionMock().constructed().get(0),
            nativeRule.getConstructionMock().constructed().get(0),
            rewardedRule.getConstructionMock().constructed().get(0),
            rewardedIntRule.getConstructionMock().constructed().get(0)
        );
    }

    @Test
    public void processIfInterstitialAdMobAdRevenueProcessor() {
        AdMobAdRevenueProcessor processor = new AdMobAdRevenueProcessor(converter, clientContext);

        when(interstitialRule.getConstructionMock().constructed().get(0).process(value))
            .thenReturn(true);

        assertThat(processor.process(value)).isTrue();
        verifyNoInteractions(
            nativeRule.getConstructionMock().constructed().get(0),
            rewardedRule.getConstructionMock().constructed().get(0),
            rewardedIntRule.getConstructionMock().constructed().get(0)
        );
    }

    @Test
    public void processIfNativeAdMobAdRevenueProcessor() {
        AdMobAdRevenueProcessor processor = new AdMobAdRevenueProcessor(converter, clientContext);

        when(nativeRule.getConstructionMock().constructed().get(0).process(value))
            .thenReturn(true);

        assertThat(processor.process(value)).isTrue();
        verifyNoInteractions(
            rewardedRule.getConstructionMock().constructed().get(0),
            rewardedIntRule.getConstructionMock().constructed().get(0)
        );
    }

    @Test
    public void processIfRewardedAdMobAdRevenueProcessor() {
        AdMobAdRevenueProcessor processor = new AdMobAdRevenueProcessor(converter, clientContext);

        when(rewardedRule.getConstructionMock().constructed().get(0).process(value))
            .thenReturn(true);

        assertThat(processor.process(value)).isTrue();
        verifyNoInteractions(
            rewardedIntRule.getConstructionMock().constructed().get(0)
        );
    }

    @Test
    public void processIfRewardedInterstitialAdMobAdRevenueProcessor() {
        AdMobAdRevenueProcessor processor = new AdMobAdRevenueProcessor(converter, clientContext);

        when(rewardedIntRule.getConstructionMock().constructed().get(0).process(value))
            .thenReturn(true);

        assertThat(processor.process(value)).isTrue();
    }

    @Test
    public void processIfNoProcessor() {
        AdMobAdRevenueProcessor processor = new AdMobAdRevenueProcessor(converter, clientContext);

        assertThat(processor.process(value)).isFalse();
    }
}
