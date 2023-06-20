package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommonArgumentsTestUtils;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public abstract class ComponentUnitFactoryBaseTest extends CommonTest {

    protected CommonArguments mCommonArguments;
    private Context mContext;
    private ComponentUnitFactory mFactory;

    @Rule
    public final GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        mCommonArguments = CommonArgumentsTestUtils.createMockedArguments();
        mFactory = createComponentUnitFactory();
    }

    public void testCreateComponentUnit(Class componentUnitClass) {
        ComponentId componentId = mock(ComponentId.class);
        when(componentId.getApiKey()).thenReturn("some api key");
        StartupState startupState = TestUtils.createDefaultStartupState();
        StartupUnit startupUnit = mock(StartupUnit.class);
        when(startupUnit.getStartupState()).thenReturn(startupState);
        ComponentUnit componentUnit = (ComponentUnit) mFactory
                .createComponentUnit(mContext, componentId, mCommonArguments.componentArguments, startupUnit);
        assertThat(componentUnit).isExactlyInstanceOf(componentUnitClass);
        assertThat(componentUnit.getComponentId()).isEqualTo(componentId);
        assertThat(componentUnit.getContext()).isEqualTo(mContext);
        assertThat(componentUnit.getStartupState()).isEqualTo(startupState);
    }

    protected abstract ComponentUnitFactory createComponentUnitFactory();
}
