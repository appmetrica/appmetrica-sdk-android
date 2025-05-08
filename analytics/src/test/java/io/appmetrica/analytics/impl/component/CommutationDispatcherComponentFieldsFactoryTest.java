package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.impl.LifecycleDependentComponentManager;
import io.appmetrica.analytics.impl.TaskProcessor;
import io.appmetrica.analytics.impl.component.processor.CommutationProcessingStrategyFactory;
import io.appmetrica.analytics.impl.component.processor.commutation.CommutationHandler;
import io.appmetrica.analytics.impl.component.processor.commutation.CommutationReportProcessor;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CommutationDispatcherComponentFieldsFactoryTest extends CommonTest {

    @Mock
    private CommutationDispatcherComponent mComponent;
    @Mock
    private StartupUnit mStartupUnit;
    @Mock
    private LifecycleDependentComponentManager lifecycleDependentComponentManager;
    private CommutationDispatcherComponentFieldsFactory mFieldsFactory;

    @Rule
    public final GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mComponent.getContext()).thenReturn(RuntimeEnvironment.getApplication());
        mFieldsFactory = new CommutationDispatcherComponentFieldsFactory(lifecycleDependentComponentManager);
    }

    @Test
    public void testCreateTaskProcessor() {
        TaskProcessor<CommutationDispatcherComponent> taskProcessor = mFieldsFactory.createTaskProcessor(mComponent, mStartupUnit);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(taskProcessor.getComponent()).isSameAs(mComponent);
        softly.assertAll();
        verify(lifecycleDependentComponentManager).addLifecycleObserver(taskProcessor);
    }

    @Test
    public void testCreateCommutationReportProcessor() {
        CommutationReportProcessor<CommutationHandler, CommutationDispatcherComponent> reportProcessor =
            mFieldsFactory.createCommutationReportProcessor(mComponent);
        assertThat(reportProcessor.getProcessingStrategyFactory()).isExactlyInstanceOf(CommutationProcessingStrategyFactory.class);
    }
}
