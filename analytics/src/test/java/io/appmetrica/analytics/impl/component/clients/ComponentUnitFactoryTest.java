package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import android.content.SharedPreferences;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.IReportableComponent;
import io.appmetrica.analytics.impl.component.MainReporterComponentUnit;
import io.appmetrica.analytics.impl.component.ReporterComponentUnit;
import io.appmetrica.analytics.impl.component.SelfSdkReportingComponentUnit;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.impl.utils.ServerTime;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ComponentUnitFactoryTest extends CommonTest {

    private Class<? extends ComponentUnitFactory> mFactoryClass;
    private Class<? extends IReportableComponent> mComponentUnitClass;

    private ComponentUnitFactory mComponentUnitFactory;

    private Context mContext;
    @Mock
    private ComponentId mComponentId;
    @Mock
    private StartupUnit startupUnit;
    @Mock
    private SharedPreferences mSharedPreferences;
    @Mock
    private SharedPreferences.Editor mEditor;

    private StartupState startupState = TestUtils.createDefaultStartupState();

    private CommonArguments.ReporterArguments mClientConfiguration;

    private final CounterConfigurationReporterType mReporterType;

    public ComponentUnitFactoryTest(Class<? extends ComponentUnitFactory> factoryClass,
                                    Class<? extends IReportableComponent> componentUnitClass,
                                    CounterConfigurationReporterType reporterType) {
        mFactoryClass = factoryClass;
        mComponentUnitClass = componentUnitClass;
        mReporterType = reporterType;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {
                        MainReporterClientFactory.class,
                        MainReporterComponentUnit.class,
                        CounterConfigurationReporterType.MAIN
                },
                {
                        ReporterClientUnitFactory.class,
                        ReporterComponentUnit.class,
                        CounterConfigurationReporterType.MANUAL
                },
                {
                        SelfSdkReportingFactory.class,
                        SelfSdkReportingComponentUnit.class,
                        CounterConfigurationReporterType.SELF_SDK
                }

        });
    }

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mContext = TestUtils.createMockedContext();
        when(mContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mSharedPreferences);
        when(mSharedPreferences.edit()).thenReturn(mEditor);
        ServerTime.getInstance().init();
        doReturn(startupState).when(startupUnit).getStartupState();

        mComponentId = new ComponentId("package name", UUID.randomUUID().toString());

        mClientConfiguration = new CommonArguments.ReporterArguments();

        mComponentUnitFactory = mFactoryClass.newInstance();
    }

    @Test
    public void testCreatedComponentUnitClass() {
        IReportableComponent componentUnit = createComponent();

        assertThat(componentUnit).isExactlyInstanceOf(mComponentUnitClass);
    }

    @Test
    public void testComponentUnitArgumentsDispatching() {
        ComponentUnit componentUnit = (ComponentUnit) createComponent();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(componentUnit.getContext()).isEqualTo(mContext);
        softAssertions.assertThat(componentUnit.getComponentId()).isEqualTo(mComponentId);

        softAssertions.assertAll();
    }

    private IReportableComponent createComponent() {
        return mComponentUnitFactory.createComponentUnit(
                mContext,
                mComponentId,
                mClientConfiguration,
                startupUnit
        );
    }
}
