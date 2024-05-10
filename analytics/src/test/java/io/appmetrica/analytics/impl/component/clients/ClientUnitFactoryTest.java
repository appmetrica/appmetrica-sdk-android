package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommonArgumentsTestUtils;
import io.appmetrica.analytics.impl.component.CommutationComponentId;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponentFactory;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.DispatcherComponentFactory;
import io.appmetrica.analytics.impl.component.MainReporterComponentId;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponentFactory;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ExactClassMatcher;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClientUnitFactoryTest extends CommonTest {

    @Mock
    ComponentsRepository mComponentsRepository;

    Object mDispatcherComponent;
    CommonArguments mClientConfiguration;
    @Mock
    ClientDescription mClientDescription;

    protected ClientUnit mClientUnit;

    @Captor
    protected ArgumentCaptor<ComponentId> mComponentIdCaptor;
    @Captor
    protected ArgumentCaptor<CommonArguments> mClientConfigurationCaptor;
    @Captor
    protected ArgumentCaptor<DispatcherComponentFactory> mDispatcherComponentFactoryCaptor;

    protected static final String PACKAGE_NAME = "Test package name";
    protected static final String API_KEY = "Test API KEY";

    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        GlobalServiceLocator.init(RuntimeEnvironment.getApplication());
        mClientConfiguration = CommonArgumentsTestUtils.createMockedArguments();
        when(mClientDescription.getPackageName()).thenReturn(PACKAGE_NAME);
        when(mClientDescription.getApiKey()).thenReturn(API_KEY);
    }

    @RunWith(RobolectricTestRunner.class)
    public static class CommutationDispatcherTest extends ClientUnitFactoryTest {

        private MainCommutationClientUnitFactory mFactory;

        @Before
        public void setUp() {
            super.setUp();
            mFactory = new MainCommutationClientUnitFactory();
            mDispatcherComponent = mock(CommutationDispatcherComponent.class);
            doReturn(mDispatcherComponent).when(mComponentsRepository).getOrCreateCommutationComponent(
                    any(ComponentId.class),
                    any(CommonArguments.class),
                    any(DispatcherComponentFactory.class)
            );
            mClientUnit = mFactory.createClientUnit(
                    RuntimeEnvironment.getApplication(),
                    mComponentsRepository,
                    mClientDescription,
                    mClientConfiguration
            );
        }

        @Test
        public void testClientUnitCreation() {
            assertThat(mClientUnit).isExactlyInstanceOf(CommutationClientUnit.class);
            CommutationClientUnit commutationClientUnit = (CommutationClientUnit) mClientUnit;
            assertThat(commutationClientUnit.getContext()).isEqualTo(RuntimeEnvironment.getApplication());
            assertThat(commutationClientUnit.getComponent()).isEqualTo(mDispatcherComponent);
        }

        @Test
        public void testDispatcherCreation() {
            verify(mComponentsRepository, times(1))
                    .getOrCreateCommutationComponent(
                            mComponentIdCaptor.capture(),
                            mClientConfigurationCaptor.capture(),
                            mDispatcherComponentFactoryCaptor.capture()
                    );

            assertThat(mComponentIdCaptor.getValue()).isExactlyInstanceOf(CommutationComponentId.class);
            assertThat(mComponentIdCaptor.getValue().getPackage()).isEqualTo(PACKAGE_NAME);

            assertThat(mClientConfigurationCaptor.getValue()).isEqualTo(mClientConfiguration);
            assertThat(mDispatcherComponentFactoryCaptor.getValue()).isExactlyInstanceOf(CommutationDispatcherComponentFactory.class);

            if (mClientUnit instanceof AbstractClientUnit) {
                assertThat(mComponentIdCaptor.getValue().getApiKey()).isEqualTo(API_KEY);
                RegularDispatcherComponentFactory dispatcherComponentFactory =
                        (RegularDispatcherComponentFactory) mDispatcherComponentFactoryCaptor.getValue();
                assertThat(dispatcherComponentFactory.getComponentUnitFactory())
                        .isExactlyInstanceOf(CommutationDispatcherComponentFactory.class);
            }
        }
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class RegularDispatcherTest<T extends ClientUnitFactory> extends ClientUnitFactoryTest {

        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {
                            MainReporterClientFactory.class,
                            MainReporterClientUnit.class,
                            MainReporterClientFactory.class,
                            MainReporterComponentId.class,
                    },
                    {
                            ReporterClientUnitFactory.class,
                            RegularClientUnit.class,
                            ReporterClientUnitFactory.class,
                            ComponentId.class,
                    },
                    {
                            SelfSdkReportingFactory.class,
                            RegularClientUnit.class,
                            SelfSdkReportingFactory.class,
                            ComponentId.class,
                    },
            });
        }

        private final T mFactory;
        private final Class mComponentUnitFactoryClass;
        private final Class<ClientUnit> mClientUnitClass;
        private final Class<ComponentId> mComponentIdClass;

        @Rule
        public final GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

        public RegularDispatcherTest(Class<T> factory,
                                     Class<ClientUnit> clientUnitClass,
                                     Class componentUnitFactoryClass,
                                     Class<ComponentId> componentIdClass)
                throws IllegalAccessException, InstantiationException {
            mFactory = factory.newInstance();
            mClientUnitClass = clientUnitClass;
            mComponentUnitFactoryClass = componentUnitFactoryClass;
            mComponentIdClass = componentIdClass;
        }

        @Before
        public void setUp() {
            super.setUp();
            mDispatcherComponent = mock(RegularDispatcherComponent.class);
            doReturn(mDispatcherComponent).when(mComponentsRepository).getOrCreateRegularComponent(
                    argThat(new ExactClassMatcher<ComponentId>(mComponentIdClass)),
                    any(CommonArguments.class),
                    any(DispatcherComponentFactory.class)
            );
            mClientUnit = mFactory.createClientUnit(
                    RuntimeEnvironment.getApplication(),
                    mComponentsRepository,
                    mClientDescription,
                    mClientConfiguration
            );
        }

        @Test
        public void testClientUnitCreation() {
            assertThat(mClientUnit).isExactlyInstanceOf(mClientUnitClass);

            if (mClientUnit instanceof CommutationClientUnit) {
                CommutationClientUnit commutationClientUnit = (CommutationClientUnit) mClientUnit;
                assertThat(commutationClientUnit.getContext()).isEqualTo(RuntimeEnvironment.getApplication());
                assertThat(commutationClientUnit.getComponent()).isEqualTo(mDispatcherComponent);
            } else if (mClientUnit instanceof AbstractClientUnit) {
                AbstractClientUnit abstractClientUnit = (AbstractClientUnit) mClientUnit;
                assertThat(abstractClientUnit.getContext()).isEqualTo(RuntimeEnvironment.getApplication());
                assertThat(abstractClientUnit.getComponentUnit()).isEqualTo(mDispatcherComponent);
            }
        }

        @Test
        public void testDispatcherCreation() {
            verify(mComponentsRepository, times(1))
                    .getOrCreateRegularComponent(
                            mComponentIdCaptor.capture(),
                            mClientConfigurationCaptor.capture(),
                            mDispatcherComponentFactoryCaptor.capture()
                    );

            assertThat(mComponentIdCaptor.getValue()).isExactlyInstanceOf(mComponentIdClass);
            assertThat(mComponentIdCaptor.getValue().getPackage()).isEqualTo(PACKAGE_NAME);

            assertThat(mClientConfigurationCaptor.getValue()).isEqualTo(mClientConfiguration);
            assertThat(mDispatcherComponentFactoryCaptor.getValue()).isExactlyInstanceOf(RegularDispatcherComponentFactory.class);

            if (mClientUnit instanceof AbstractClientUnit) {
                assertThat(mComponentIdCaptor.getValue().getApiKey()).isEqualTo(API_KEY);
                RegularDispatcherComponentFactory dispatcherComponentFactory =
                        (RegularDispatcherComponentFactory) mDispatcherComponentFactoryCaptor.getValue();
                assertThat(dispatcherComponentFactory.getComponentUnitFactory())
                        .isExactlyInstanceOf(mComponentUnitFactoryClass);
            }
        }
    }
}
