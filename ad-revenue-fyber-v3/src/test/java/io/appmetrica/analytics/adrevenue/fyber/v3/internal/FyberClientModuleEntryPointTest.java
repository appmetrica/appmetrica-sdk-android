package io.appmetrica.analytics.adrevenue.fyber.v3.internal;

import io.appmetrica.analytics.adrevenue.fyber.v3.impl.FyberAdRevenueProcessor;
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

import static io.appmetrica.analytics.adrevenue.fyber.v3.impl.Constants.LIBRARY_MAIN_CLASS;
import static io.appmetrica.analytics.adrevenue.fyber.v3.impl.Constants.MODULE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class FyberClientModuleEntryPointTest extends CommonTest {

    private final ClientContext clientContext = mock(ClientContext.class);
    private final ModuleAdRevenueProcessorsHolder adRevenueProcessorsHolder =
        mock(ModuleAdRevenueProcessorsHolder.class);
    private final ModuleAdRevenueContext moduleAdRevenueContext =
        mock(ModuleAdRevenueContext.class);

    @Rule
    public final MockedStaticRule<ReflectionUtils> reflectionUtilsRule =
        new MockedStaticRule<>(ReflectionUtils.class);
    @Rule
    public final MockedConstructionRule<FyberAdRevenueProcessor> fyberAdRevenueProcessorRule =
        new MockedConstructionRule<>(FyberAdRevenueProcessor.class);

    private final FyberClientModuleEntryPoint entryPoint = new FyberClientModuleEntryPoint();

    @Before
    public void setUp() {
        when(clientContext.getModuleAdRevenueContext()).thenReturn(moduleAdRevenueContext);
        when(moduleAdRevenueContext.getAdRevenueProcessorsHolder())
            .thenReturn(adRevenueProcessorsHolder);
    }

    @Test
    public void getIdentifier() {
        assertThat(entryPoint.getIdentifier()).isEqualTo(MODULE_ID);
    }

    @Test
    public void initClientSide() {
        when(ReflectionUtils.detectClassExists(LIBRARY_MAIN_CLASS)).thenReturn(true);

        entryPoint.initClientSide(clientContext);

        verify(adRevenueProcessorsHolder)
            .register(fyberAdRevenueProcessorRule.getConstructionMock().constructed().get(0));
    }

    @Test
    public void onActivatedIfNoLibrary() {
        when(ReflectionUtils.detectClassExists(LIBRARY_MAIN_CLASS)).thenReturn(false);

        entryPoint.initClientSide(clientContext);

        verifyNoInteractions(clientContext);
    }
}
