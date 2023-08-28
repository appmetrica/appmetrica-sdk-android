package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.impl.StatisticsRestrictionControllerImpl;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ReporterArgumentsFactoryTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{"controller restrict sending and config allows", true, true, false},
                new Object[]{"controller restrict sending", true, false, false},
                new Object[]{"controller and config allows sending", false, true, true},
                new Object[]{"controller allows sending, but config not", false, false, false}
        );
    }

    private final StatisticsRestrictionControllerImpl mController = mock(StatisticsRestrictionControllerImpl.class);
    private final CounterConfiguration mConfiguration = mock(CounterConfiguration.class);
    private final ReporterArgumentsFactory mFactory = new ReporterArgumentsFactory(mController);
    private final boolean mResult;

    public ReporterArgumentsFactoryTest(String description,
                                        boolean controller,
                                        boolean configuration,
                                        boolean result
    ) {
        doReturn(controller).when(mController).isRestrictedForReporter();
        doReturn(configuration).when(mConfiguration).getStatisticsSending();
        mResult = result;
    }

    @Test
    public void test() {
        assertThat(mFactory.shouldSend(new CommonArguments.ReporterArguments(mConfiguration, null).statisticsSending)).isEqualTo(mResult);
    }

}
