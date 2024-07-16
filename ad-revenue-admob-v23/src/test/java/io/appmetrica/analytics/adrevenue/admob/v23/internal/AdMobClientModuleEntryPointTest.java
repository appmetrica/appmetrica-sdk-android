package io.appmetrica.analytics.adrevenue.admob.v23.internal;

import io.appmetrica.analytics.adrevenue.admob.v23.impl.Constants;
import io.appmetrica.analytics.adrevenue.admob.v23.impl.processor.AdMobAdRevenueProcessor;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessorsHolder;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
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

public class AdMobClientModuleEntryPointTest extends CommonTest {

    @Mock
    private ModuleAdRevenueProcessorsHolder adRevenueProcessorsHolder;
    @Mock
    private ModuleAdRevenueContext adRevenueContext;
    @Mock
    private ClientContext clientContext;

    @Rule
    public final MockedStaticRule<ReflectionUtils> reflectionUtilsRule =
        new MockedStaticRule<>(ReflectionUtils.class);
    @Rule
    public final MockedConstructionRule<AdMobAdRevenueProcessor> adMobAdRevenueProcessorRule =
        new MockedConstructionRule<>(AdMobAdRevenueProcessor.class);

    private final AdMobClientModuleEntryPoint entryPoint = new AdMobClientModuleEntryPoint();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(clientContext.getModuleAdRevenueContext()).thenReturn(adRevenueContext);
        when(adRevenueContext.getAdRevenueProcessorsHolder()).thenReturn(adRevenueProcessorsHolder);
    }

    @Test
    public void getIdentifier() {
        assertThat(entryPoint.getIdentifier()).isEqualTo(Constants.MODULE_ID);
    }

    @Test
    public void initClientSide() {
        when(ReflectionUtils.detectClassExists(Constants.LIBRARY_MAIN_CLASS)).thenReturn(true);

        entryPoint.initClientSide(clientContext);

        verify(adRevenueProcessorsHolder)
            .register(adMobAdRevenueProcessorRule.getConstructionMock().constructed().get(0));
    }

    @Test
    public void onActivatedIfNoLibrary() {
        when(ReflectionUtils.detectClassExists(Constants.LIBRARY_MAIN_CLASS)).thenReturn(false);

        entryPoint.initClientSide(clientContext);

        verifyNoInteractions(clientContext);
    }
}
