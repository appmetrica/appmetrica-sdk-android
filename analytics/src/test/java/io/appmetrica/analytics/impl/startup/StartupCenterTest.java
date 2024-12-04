package io.appmetrica.analytics.impl.startup;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.id.RetryStrategy;
import io.appmetrica.analytics.impl.request.StartupArgumentsTest;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.ServiceMigrationCheckedRule;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupCenterTest extends CommonTest {

    private Context context;
    private StartupCenter mStartupCenter = new StartupCenter();
    private static final String PACKAGE_NAME = "testPackage";

    @Rule
    public RuleChain mRuleChain = RuleChain.outerRule(
            new GlobalServiceLocatorRule())
            .around(new ServiceMigrationCheckedRule()
    );

    @Rule
    public MockedConstructionRule<StartupUnit> startupUnitMockedConstructionRule =
        new MockedConstructionRule<>(StartupUnit.class);

    @Rule
    public MockedConstructionRule<StartupUnitComponents> startupUnitComponentsMockedConstructionRule =
        new MockedConstructionRule<>(StartupUnitComponents.class);

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        when(GlobalServiceLocator.getInstance().getAppSetIdGetter().getAppSetId()).thenReturn(new AppSetId(null, AppSetIdScope.UNKNOWN));
        when(GlobalServiceLocator.getInstance().getAdvertisingIdGetter()
                .getIdentifiersForced(any(RetryStrategy.class)))
                .thenReturn(new AdvertisingIdsHolder());
        when(GlobalServiceLocator.getInstance().getMultiProcessSafeUuidProvider().readUuid())
            .thenReturn(new IdentifiersResult(UUID.randomUUID().toString(), IdentifierStatus.OK, null));
    }

    @Test
    public void testCreateNewStartupUnit() {
        assertThat(mStartupCenter.getStartupUnit(PACKAGE_NAME)).isNull();
        mStartupCenter.getOrCreateStartupUnit(context, createStartupUserMock(PACKAGE_NAME), StartupArgumentsTest.empty());
        assertThat(mStartupCenter.getStartupUnit(PACKAGE_NAME)).isNotNull();
    }

    @Test
    public void testStartupUnitStillTheSame() {
        ComponentId component = createStartupUserMock(PACKAGE_NAME);
        StartupUnit unit = mStartupCenter.getOrCreateStartupUnit(context, component, StartupArgumentsTest.empty());
        assertThat(mStartupCenter.getOrCreateStartupUnit(context, createStartupUserMock(PACKAGE_NAME), StartupArgumentsTest.empty()))
                .isSameAs(unit);
    }

    @Test
    public void testRegisterListenerNullStartup() {
        StartupListener startupListener = mock(StartupListener.class);
        assertThat(mStartupCenter.getListeners(PACKAGE_NAME)).isNullOrEmpty();
        mStartupCenter.registerStartupListener(createStartupUserMock(PACKAGE_NAME), startupListener);
        assertThat(mStartupCenter.getListeners(PACKAGE_NAME)).hasSize(1);
        verify(startupListener, never()).onStartupChanged(nullable(StartupState.class));
    }

    @Test
    public void testRegisterListenerNotNullStartup() {
        StartupState startupState = mock(StartupState.class);
        mStartupCenter.getResultListener().onStartupChanged(PACKAGE_NAME, startupState);
        StartupListener startupListener = mock(StartupListener.class);
        assertThat(mStartupCenter.getListeners(PACKAGE_NAME)).isNullOrEmpty();
        mStartupCenter.registerStartupListener(createStartupUserMock(PACKAGE_NAME), startupListener);
        assertThat(mStartupCenter.getListeners(PACKAGE_NAME)).hasSize(1);
        verify(startupListener).onStartupChanged(startupState);
    }

    @Test
    public void testRegisterMoreThanOneListener() {
        StartupListener startupListener = mock(StartupListener.class);
        assertThat(mStartupCenter.getListeners(PACKAGE_NAME)).isNullOrEmpty();
        mStartupCenter.registerStartupListener(createStartupUserMock(PACKAGE_NAME), startupListener);
        mStartupCenter.registerStartupListener(createStartupUserMock(PACKAGE_NAME), startupListener);
        assertThat(mStartupCenter.getListeners(PACKAGE_NAME)).hasSize(2);
    }

    @Test
    public void testCallListenerWithResult() {
        ComponentId component = createStartupUserMock(PACKAGE_NAME);
        StartupListener startupListener = mock(StartupListener.class);
        mStartupCenter.registerStartupListener(component, startupListener);
        mStartupCenter.getResultListener().onStartupChanged(PACKAGE_NAME, mock(StartupState.class));
        verify(startupListener, times(1)).onStartupChanged(any(StartupState.class));
        verify(startupListener, never()).onStartupError(any(StartupError.class), any(StartupState.class));
    }

    @Test
    public void testNotCallListenerForOtherPackage() {
        ComponentId component = createStartupUserMock(PACKAGE_NAME);
        StartupListener startupListener = mock(StartupListener.class);
        mStartupCenter.registerStartupListener(component, startupListener);
        mStartupCenter.getResultListener().onStartupChanged(PACKAGE_NAME + ".other", mock(StartupState.class));
        verify(startupListener, never()).onStartupChanged(any(StartupState.class));
        verify(startupListener, never()).onStartupError(any(StartupError.class), any(StartupState.class));
    }

    @Test
    public void testCallListenerWithError() {
        ComponentId component = createStartupUserMock(PACKAGE_NAME);
        StartupListener startupListener = mock(StartupListener.class);
        mStartupCenter.registerStartupListener(component, startupListener);
        StartupState startupState = mock(StartupState.class);
        mStartupCenter.getResultListener().onStartupError(PACKAGE_NAME, StartupError.UNKNOWN, startupState);
        verify(startupListener, never()).onStartupChanged(any(StartupState.class));
        verify(startupListener, times(1)).onStartupError(StartupError.UNKNOWN, startupState);
    }

    @Test
    public void testUnregisterAllListeners() {
        mStartupCenter.getOrCreateStartupUnit(context, createStartupUserMock(PACKAGE_NAME), StartupArgumentsTest.empty());
        ComponentId component1 = createStartupUserMock(PACKAGE_NAME);
        StartupListener startupListener = mock(StartupListener.class);
        mStartupCenter.registerStartupListener(component1, startupListener);

        mStartupCenter.unregisterStartupListener(component1, startupListener);
        assertThat(mStartupCenter.getListeners(PACKAGE_NAME)).isEmpty();
    }

    @Test
    public void testUnregisterOnlyOneListeners() {
        ComponentId component1 = createStartupUserMock(PACKAGE_NAME);
        StartupListener startupListener = mock(StartupListener.class);
        mStartupCenter.registerStartupListener(component1, startupListener);
        mStartupCenter.registerStartupListener(createStartupUserMock(PACKAGE_NAME), mock(StartupListener.class));

        mStartupCenter.unregisterStartupListener(component1, startupListener);
        assertThat(mStartupCenter.getListeners(PACKAGE_NAME)).hasSize(1);
    }

    public ComponentId createStartupUserMock(String packageName) {
        return new ComponentId(packageName, UUID.randomUUID().toString());
    }
}
