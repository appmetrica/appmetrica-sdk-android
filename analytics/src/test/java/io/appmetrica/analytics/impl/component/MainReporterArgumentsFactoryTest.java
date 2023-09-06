package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class MainReporterArgumentsFactoryTest extends CommonTest {

    private final DataSendingRestrictionControllerImpl mController = mock(DataSendingRestrictionControllerImpl.class);
    private final MainReporterArgumentsFactory mFactory = new MainReporterArgumentsFactory(mController);

    @Test
    public void testEnabled() {
        CounterConfiguration configuration = mock(CounterConfiguration.class);
        doReturn(true).when(configuration).getDataSendingEnabled();
        assertThat(mFactory.shouldSend(new CommonArguments.ReporterArguments(configuration, null).dataSendingEnabled)).isTrue();
        verifyZeroInteractions(mController);
    }

    @Test
    public void testDisabled() {
        CounterConfiguration configuration = mock(CounterConfiguration.class);
        doReturn(false).when(configuration).getDataSendingEnabled();
        assertThat(mFactory.shouldSend(new CommonArguments.ReporterArguments(configuration, null).dataSendingEnabled)).isFalse();
        verifyZeroInteractions(mController);
    }

}
