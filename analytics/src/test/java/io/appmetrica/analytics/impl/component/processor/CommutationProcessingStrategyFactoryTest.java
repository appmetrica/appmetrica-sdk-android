package io.appmetrica.analytics.impl.component.processor;

import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.processor.commutation.ForceStartupHandler;
import io.appmetrica.analytics.impl.component.processor.commutation.UpdatePreActivationConfigHandler;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.RuntimeEnvironment;

import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_STARTUP;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class CommutationProcessingStrategyFactoryTest extends CommonTest {

    @Before
    public void setUp() {
        doReturn(RuntimeEnvironment.getApplication()).when(mComponent).getContext();
        mFactory = new CommutationProcessingStrategyFactory(mComponent);
    }

    @Parameters(name = "For {0} should return {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {EVENT_TYPE_STARTUP, Collections.singletonList(ForceStartupHandler.class)},
                {EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG, Collections.singletonList(UpdatePreActivationConfigHandler.class)}
        });
    }

    @Rule
    public final GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    private final InternalEvents mEvent;
    private final List<Class> mHandlers;

    private final CommutationDispatcherComponent mComponent = mock(CommutationDispatcherComponent.class);
    private CommutationProcessingStrategyFactory mFactory;

    public CommutationProcessingStrategyFactoryTest(InternalEvents event, List<Class> handlers) {
        mEvent = event;
        mHandlers = handlers;
    }

    @Test
    public void testProperHandlerList() {
        assertThat(mFactory.getProcessingStrategy(mEvent.getTypeId()).getEventHandlers()).
                extracting("class").containsOnlyElementsOf(mHandlers);
    }
}
