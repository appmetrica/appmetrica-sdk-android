package io.appmetrica.analytics.adrevenue.admob.v23.impl.processor;

import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.appopen.AppOpenAd;
import io.appmetrica.analytics.adrevenue.admob.v23.impl.AdRevenueConverter;
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.common.InternalClientModuleFacade;
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

public class AppOpenAdMobAdRevenueProcessorTest extends CommonTest {

    @Mock
    private AdRevenueConverter converter;
    @Mock
    private ClientContext clientContext;
    @Mock
    private InternalClientModuleFacade internalClientModuleFacade;
    @Mock
    private PublicLogger publicLogger;
    @Mock
    private ModuleAdRevenue adRevenue;

    @Mock
    private AdValue adValue;
    @Mock
    private AppOpenAd appOpenAd;

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

        processor = new AppOpenAdMobAdRevenueProcessor(converter, clientContext);

        when(LoggerStorage.getMainPublicOrAnonymousLogger()).thenReturn(publicLogger);
        when(clientContext.getInternalClientModuleFacade()).thenReturn(internalClientModuleFacade);
        when(converter.convertAppOpenAd(adValue, appOpenAd)).thenReturn(adRevenue);

        when(ReflectionUtils.isArgumentsOfClasses(
            new Object[]{adValue, appOpenAd},
            AdValue.class,
            AppOpenAd.class
        )).thenReturn(true);
    }

    @Test
    public void process() {
        assertThat(processor.process(adValue, appOpenAd)).isTrue();
        verify(converter).convertAppOpenAd(adValue, appOpenAd);
        verify(internalClientModuleFacade).reportAdRevenue(adRevenue);
        verify(publicLogger).info("Ad Revenue from AdMob was reported");
    }

    @Test
    public void processWithWrongNumberOfValues() {
        when(ReflectionUtils.isArgumentsOfClasses(
            new Object[]{adValue, appOpenAd},
            AdValue.class,
            AppOpenAd.class
        )).thenReturn(false);

        assertThat(processor.process(adValue, appOpenAd)).isFalse();
        verifyNoInteractions(converter, internalClientModuleFacade, publicLogger);
    }
}
