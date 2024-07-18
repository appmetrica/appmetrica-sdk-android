package io.appmetrica.analytics.adrevenue.fyber.v3.impl;

import com.fyber.fairbid.ads.ImpressionData;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueReporter;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class FyberAdRevenueProcessorTest extends CommonTest {

    private final ImpressionData impressionData = mock(ImpressionData.class);
    private final ModuleAdRevenue moduleAdRevenue = mock(ModuleAdRevenue.class);
    private final ModuleAdRevenueReporter moduleAdRevenueReporter =
        mock(ModuleAdRevenueReporter.class);
    private final ModuleAdRevenueContext moduleAdRevenueContext =
        mock(ModuleAdRevenueContext.class);
    private final AdRevenueConverter converter = mock(AdRevenueConverter.class);
    private final ClientContext clientContext = mock(ClientContext.class);

    @Rule
    public final MockedStaticRule<ReflectionUtils> reflectionUtilsRule =
        new MockedStaticRule<>(ReflectionUtils.class);

    private final FyberAdRevenueProcessor processor = new FyberAdRevenueProcessor(
        converter,
        clientContext
    );

    @Before
    public void setUp() {
        when(clientContext.getModuleAdRevenueContext()).thenReturn(moduleAdRevenueContext);
        when(moduleAdRevenueContext.getAdRevenueReporter()).thenReturn(moduleAdRevenueReporter);
        when(converter.convert(impressionData)).thenReturn(moduleAdRevenue);
        when(ReflectionUtils.isArgumentsOfClasses(
            new Object[] { impressionData },
            ImpressionData.class
        )).thenReturn(true);
    }

    @Test
    public void process() {
        assertThat(processor.process(impressionData)).isTrue();
        verify(moduleAdRevenueReporter).reportAutoAdRevenue(moduleAdRevenue);
    }

    @Test
    public void processWithWrongNumberOfParameters() {
        assertThat(processor.process()).isFalse();
        verifyNoInteractions(moduleAdRevenue);
    }

    @Test
    public void processWithWrongParameters() {
        when(ReflectionUtils.isArgumentsOfClasses(
            new Object[] { impressionData },
            ImpressionData.class
        )).thenReturn(false);

        assertThat(processor.process(impressionData)).isFalse();
        verifyNoInteractions(moduleAdRevenue);
    }
}
