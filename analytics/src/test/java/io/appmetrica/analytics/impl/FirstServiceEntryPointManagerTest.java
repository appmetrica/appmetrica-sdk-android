package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentsInitializer;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.modules.ModuleStatus;
import io.appmetrica.analytics.impl.modules.ModuleStatusReporter;
import io.appmetrica.analytics.impl.modules.ModulesSeeker;
import io.appmetrica.analytics.impl.service.migration.ServiceMigrationManager;
import io.appmetrica.analytics.impl.servicecomponents.ServiceComponentsInitializerProvider;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FirstServiceEntryPointManagerTest extends CommonTest {

    private FirstServiceEntryPointManager firstServiceEntryPointManager;

    private Context context;

    @Rule
    public MockedConstructionRule<ServiceMigrationManager> cServiceMigrationManager =
            new MockedConstructionRule<>(ServiceMigrationManager.class);

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Rule
    public MockedConstructionRule<ServiceComponentsInitializerProvider>
        serviceComponentsInitializerProviderMockedConstructionRule =
        new MockedConstructionRule<>(
            ServiceComponentsInitializerProvider.class,
            new MockedConstruction.MockInitializer<ServiceComponentsInitializerProvider>() {
                @Override
                public void prepare(ServiceComponentsInitializerProvider mock,
                                    MockedConstruction.Context context) throws Throwable {
                    when(mock.getServiceComponentsInitializer())
                        .thenReturn(mock(ServiceComponentsInitializer.class));
                }
            }
        );

    private final List<ModuleStatus> modulesStatus = Arrays.asList(
        mock(ModuleStatus.class),
        mock(ModuleStatus.class)
    );

    @Rule
    public MockedConstructionRule<ModulesSeeker> modulesSeekerMockedConstructionRule =
        new MockedConstructionRule<>(
            ModulesSeeker.class,
            new MockedConstruction.MockInitializer<ModulesSeeker>() {
                @Override
                public void prepare(ModulesSeeker mock, MockedConstruction.Context context)
                    throws Throwable {
                    when(mock.discoverServiceModules()).thenReturn(modulesStatus);
                }
            }
        );

    @Rule
    public MockedConstructionRule<ModuleStatusReporter> moduleStatusReporterMockedConstructionRule =
        new MockedConstructionRule<>(ModuleStatusReporter.class);

    @Rule
    public MockedConstructionRule<SystemTimeProvider> timeProviderMockedConstructionRule =
        new MockedConstructionRule<>(SystemTimeProvider.class);

    @Rule
    public ContextRule contextRule = new ContextRule();

    private ServiceComponentsInitializerProvider serviceComponentsInitializerProvider;
    private ModulesSeeker modulesSeeker;

    @Mock
    private IHandlerExecutor executor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = contextRule.getContext();

        when(GlobalServiceLocator.getInstance().getServiceExecutorProvider().getMetricaCoreExecutor())
            .thenReturn(executor);

        firstServiceEntryPointManager = new FirstServiceEntryPointManager();
        serviceComponentsInitializerProvider = serviceComponentsInitializerProvider();
        modulesSeeker = modulesSeeker();
    }

    @After
    public void tearDown() {
        GlobalServiceLocator.destroy();
    }

    @Test
    public void onPossibleFirstEntry() {
        firstServiceEntryPointManager.onPossibleFirstEntry(context);
        // Second and subsequent calls should be ignored
        firstServiceEntryPointManager.onPossibleFirstEntry(context);
        assertThat(GlobalServiceLocator.getInstance()).isNotNull();
        List<ServiceMigrationManager> createdServicesMigrationManagers =
            cServiceMigrationManager.getConstructionMock().constructed();
            ServiceMigrationManager serviceMigrationManager =
                createdServicesMigrationManagers.get(createdServicesMigrationManagers.size() - 1);
        MultiProcessSafeUuidProvider multiProcessSafeUuidProvider =
            GlobalServiceLocator.getInstance().getMultiProcessSafeUuidProvider();
        ModuleStatusReporter moduleStatusReporter = moduleStatusReporter();
        VitalCommonDataProvider vitalCommonDataProvider =
            GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getCommonDataProvider();
        InOrder inOrder = inOrder(
            serviceComponentsInitializerProvider.getServiceComponentsInitializer(),
            modulesSeeker,
            serviceMigrationManager,
            multiProcessSafeUuidProvider,
            moduleStatusReporter,
            vitalCommonDataProvider
        );
        inOrder.verify(serviceComponentsInitializerProvider.getServiceComponentsInitializer()).onCreate(context);
        inOrder.verify(modulesSeeker).discoverServiceModules();
        inOrder.verify(serviceMigrationManager).checkMigration(context);
        inOrder.verify(vitalCommonDataProvider).init();
        inOrder.verify(multiProcessSafeUuidProvider).readUuid();
        inOrder.verify(moduleStatusReporter).reportModulesStatus(modulesStatus);
        inOrder.verifyNoMoreInteractions();
    }

    private ServiceComponentsInitializerProvider serviceComponentsInitializerProvider() {
        // One for singleton instance
        assertThat(serviceComponentsInitializerProviderMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .isEmpty();
        List<ServiceComponentsInitializerProvider> created =
            serviceComponentsInitializerProviderMockedConstructionRule.getConstructionMock().constructed();
        return created.get(created.size() - 1);
    }

    private ModulesSeeker modulesSeeker() {
        assertThat(modulesSeekerMockedConstructionRule.getArgumentInterceptor().flatArguments()).isEmpty();
        List<ModulesSeeker> created = modulesSeekerMockedConstructionRule.getConstructionMock().constructed();
        return created.get(created.size() - 1);
    }

    private ModuleStatusReporter moduleStatusReporter() {
        assertThat(moduleStatusReporterMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(moduleStatusReporterMockedConstructionRule.getArgumentInterceptor().getArguments().get(0))
            .containsExactly(
                executor,
                GlobalServiceLocator.getInstance().getServicePreferences(),
                "service_modules",
                timeProviderMockedConstructionRule.getConstructionMock().constructed().get(0)
            );
        return moduleStatusReporterMockedConstructionRule.getConstructionMock().constructed().get(0);
    }
}
