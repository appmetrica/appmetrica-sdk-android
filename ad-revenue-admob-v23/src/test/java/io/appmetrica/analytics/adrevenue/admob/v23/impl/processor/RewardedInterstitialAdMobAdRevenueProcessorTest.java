package io.appmetrica.analytics.adrevenue.admob.v23.impl.processor;

import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import io.appmetrica.analytics.adrevenue.admob.v23.impl.AdRevenueConverter;
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueReporter;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class RewardedInterstitialAdMobAdRevenueProcessorTest extends CommonTest {

    @Mock
    private AdRevenueConverter converter;
    @Mock
    private ClientContext clientContext;
    @Mock
    private ModuleAdRevenueContext adRevenueContext;
    @Mock
    private ModuleAdRevenueReporter adRevenueReporter;
    @Mock
    private PublicLogger publicLogger;
    @Mock
    private ModuleAdRevenue adRevenue;
    @Mock
    private RewardedInterstitialAd rewardedInterstitialAd;

    @Mock
    private AdValue adValue;
    @Mock
    private AdView adView;

    @Rule
    public MockedStaticRule<LoggerStorage> loggerStorageRule =
        new MockedStaticRule<>(LoggerStorage.class);
    @Rule
    public MockedStaticRule<ReflectionUtils> reflectionUtilsRule =
        new MockedStaticRule<>(ReflectionUtils.class);

    private BaseAdMobAdRevenueProcessor processor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        processor = new RewardedInterstitialAdMobAdRevenueProcessor(converter, clientContext);

        when(LoggerStorage.getMainPublicOrAnonymousLogger()).thenReturn(publicLogger);
        when(clientContext.getModuleAdRevenueContext()).thenReturn(adRevenueContext);
        when(adRevenueContext.getAdRevenueReporter()).thenReturn(adRevenueReporter);
        when(converter.convertRewardedInterstitialAd(adValue, adView)).thenReturn(adRevenue);

        when(ReflectionUtils.isArgumentsOfClasses(
            new Object[]{adValue, adView, rewardedInterstitialAd},
            AdValue.class,
            AdView.class,
            RewardedInterstitialAd.class
        )).thenReturn(true);
    }

    @Test
    public void process() {
        assertThat(processor.process(adValue, adView, rewardedInterstitialAd)).isTrue();
        verify(converter).convertRewardedInterstitialAd(adValue, adView);
        verify(adRevenueReporter).reportAutoAdRevenue(adRevenue);
        verify(publicLogger).info("Ad Revenue from AdMob was reported");
    }

    @Test
    public void processWithWrongValues() {
        when(ReflectionUtils.isArgumentsOfClasses(
            new Object[]{adValue, adView, rewardedInterstitialAd},
            AdValue.class,
            AdView.class,
            RewardedInterstitialAd.class
        )).thenReturn(false);

        assertThat(processor.process(adValue, adView, rewardedInterstitialAd)).isFalse();
        verifyNoInteractions(converter, adRevenueReporter, publicLogger);
    }
}