package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.impl.stub.AppMetricaCoreStub;
import io.appmetrica.analytics.impl.stub.AppMetricaImplStub;
import io.appmetrica.analytics.impl.utils.UnlockedUserStateProvider;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaServiceCoreComponentsProviderTest extends CommonTest {

    @Rule
    public MockedConstructionRule<AppMetricaCore> cAppMetricaCore = new MockedConstructionRule<>(AppMetricaCore.class);
    @Rule
    public MockedConstructionRule<AppMetricaCoreStub> cAppMetricaCoreStub =
        new MockedConstructionRule<>(AppMetricaCoreStub.class);
    @Rule
    public MockedConstructionRule<AppMetricaImpl> cAppMetricaImpl = new MockedConstructionRule<>(AppMetricaImpl.class);
    @Rule
    public MockedConstructionRule<AppMetricaImplStub> cAppMetricaImplStub =
        new MockedConstructionRule<>(AppMetricaImplStub.class);
    @Rule
    public MockedStaticRule<SdkUtils> sdkUtilsMockedStaticRule = new MockedStaticRule<>(SdkUtils.class);

    private Context context;
    @Mock
    private UnlockedUserStateProvider unlockedUserStateProvider;
    @Mock
    private AppMetricaCore appMetricaCore;
    @Mock
    private ClientExecutorProvider clientExecutorProvider;

    private AppMetricaCoreComponentsProvider coreComponentsProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        coreComponentsProvider = new AppMetricaCoreComponentsProvider(unlockedUserStateProvider);
    }

    @Test
    public void getCoreIfUserUnlocked() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(true);
        coreComponentsProvider.getCore(context, clientExecutorProvider);
        assertThat(cAppMetricaCore.getArgumentInterceptor().flatArguments()).
                containsExactly(context, clientExecutorProvider);
        for (int i = 2; i < 10; i++) {
            assertThat(coreComponentsProvider.getCore(context, clientExecutorProvider))
                    .as("Attempt %d to get component", i)
                    .isEqualTo(cAppMetricaCore.getConstructionMock().constructed().get(0));
        }
        assertThat(cAppMetricaCore.getConstructionMock().constructed()).hasSize(1);
    }

    @Test
    public void getCoreIfUserIsNotUnlocked() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(false);
        coreComponentsProvider.getCore(context, clientExecutorProvider);
        assertThat(cAppMetricaCoreStub.getArgumentInterceptor().flatArguments()).containsExactly();
        for (int i = 2; i < 10; i++) {
            assertThat(coreComponentsProvider.getCore(context, clientExecutorProvider))
                    .as("Attempt %d to get component", i)
                    .isEqualTo(cAppMetricaCoreStub.getConstructionMock().constructed().get(0));
        }
        assertThat(cAppMetricaCoreStub.getConstructionMock().constructed()).hasSize(1);
    }

    @Test
    public void getImplIfUserUnlocked() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(true);
        coreComponentsProvider.getImpl(context, appMetricaCore);
        assertThat(cAppMetricaImpl.getArgumentInterceptor().flatArguments()).
                containsExactly(context, appMetricaCore);
        for (int i = 2; i < 10; i++) {
            assertThat(coreComponentsProvider.getImpl(context, appMetricaCore))
                    .as("Attempt %d to get component", i)
                    .isEqualTo(cAppMetricaImpl.getConstructionMock().constructed().get(0));
        }
        assertThat(cAppMetricaImpl.getConstructionMock().constructed()).hasSize(1);
    }

    @Test
    public void getImplIfUserIsNotUnlocked() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(false);
        coreComponentsProvider.getImpl(context, appMetricaCore);
        assertThat(cAppMetricaImplStub.getConstructionMock().constructed()).hasSize(1);
        assertThat(cAppMetricaImplStub.getArgumentInterceptor().flatArguments()).isEmpty();
        for (int i = 2; i < 10; i++) {
            assertThat(coreComponentsProvider.getImpl(context, appMetricaCore))
                    .as("Attempt %d to get component", i)
                    .isEqualTo(cAppMetricaImplStub.getConstructionMock().constructed().get(0));
        }
        assertThat(cAppMetricaImplStub.getConstructionMock().constructed()).hasSize(1);
    }

    @Test
    public void useOnlyImplementationsOnInitialUserUnlockedStateForFirstCoreCreation() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(true);
        coreComponentsProvider.getCore(context, clientExecutorProvider);
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(false);
        coreComponentsProvider.getImpl(context, appMetricaCore);
        assertThat(cAppMetricaImpl.getArgumentInterceptor().flatArguments())
                .containsExactly(context, appMetricaCore);
    }

    @Test
    public void useOnlyImplementationsOnInitialUserUnlockedStateForFirstImplCreation() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(true);
        coreComponentsProvider.getImpl(context, appMetricaCore);
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(false);
        coreComponentsProvider.getCore(context, clientExecutorProvider);
        assertThat(cAppMetricaCore.getArgumentInterceptor().flatArguments())
                .containsExactly(context, clientExecutorProvider);
    }

    @Test
    public void useOnlyStubsOnInitialUserLockedStateForFirstCoreCreation() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(false);
        coreComponentsProvider.getCore(context, clientExecutorProvider);
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(true);
        coreComponentsProvider.getImpl(context, appMetricaCore);
        assertThat(cAppMetricaImplStub.getConstructionMock().constructed()).hasSize(1);
    }

    @Test
    public void useOnlyStubsOnInitialUserLockedStateForFirstImplCreation() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(false);
        coreComponentsProvider.getImpl(context, appMetricaCore);
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(true);
        coreComponentsProvider.getCore(context, clientExecutorProvider);
        assertThat(cAppMetricaCoreStub.getArgumentInterceptor().flatArguments()).containsExactly();
    }

    @Test
    public void logForUserLockedState() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(false);
        coreComponentsProvider.getImpl(context, appMetricaCore);
        sdkUtilsMockedStaticRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                SdkUtils.logStubUsage();
            }
        });
    }

    @Test
    public void logForUserUnlockedState() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(true);
        coreComponentsProvider.getImpl(context, appMetricaCore);
        sdkUtilsMockedStaticRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                SdkUtils.logStubUsage();
            }
        }, never());
    }

    @Test
    public void useStubsIfUnlocked() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(true);
        assertThat(coreComponentsProvider.shouldUseStubs(context)).isFalse();
    }

    @Test
    public void useStubsIfLocked() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(false);
        assertThat(coreComponentsProvider.shouldUseStubs(context)).isTrue();
    }

    @Test
    public void peekUseStubsBeforeGetIfLocked() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(false);
        assertThat(coreComponentsProvider.peekShouldUseStubs()).isFalse();
    }

    @Test
    public void peekUseStubsBeforeGetIfUnlocked() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(true);
        assertThat(coreComponentsProvider.peekShouldUseStubs()).isFalse();
    }

    @Test
    public void peekUseStubsAfterGetIsLocked() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(false);
        coreComponentsProvider.shouldUseStubs(context);
        assertThat(coreComponentsProvider.peekShouldUseStubs()).isTrue();
    }

    @Test
    public void peekUseStubsAfterGetIfUnlocked() {
        when(unlockedUserStateProvider.isUserUnlocked(context)).thenReturn(true);
        coreComponentsProvider.shouldUseStubs(context);
        assertThat(coreComponentsProvider.peekShouldUseStubs()).isFalse();
    }
}
