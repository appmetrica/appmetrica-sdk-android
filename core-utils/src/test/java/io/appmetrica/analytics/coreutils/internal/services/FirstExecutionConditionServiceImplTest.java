package io.appmetrica.analytics.coreutils.internal.services;

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.testutils.CommonTest;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class FirstExecutionConditionServiceImplTest extends CommonTest {

    private FirstExecutionConditionServiceImpl mFirstExecutionConditionServiceImpl;
    private FirstExecutionConditionServiceImpl.FirstExecutionHandler mFirstExecutionHandler;
    private final int mLaunchDelay = 3;

    @Mock
    private FirstExecutionConditionServiceImpl.FirstExecutionConditionChecker mFirstExecutionConditionChecker;
    @Mock
    private ICommonExecutor mExecutor;
    @Mock
    private WaitForActivationDelayBarrier.ActivationBarrierHelper mActivationBarrierHelper;
    @Mock
    private UtilityServiceConfiguration utilityServiceConfiguration;
    @Mock
    private UtilityServiceProvider utilityServiceProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mFirstExecutionConditionServiceImpl = new FirstExecutionConditionServiceImpl(utilityServiceProvider);
        mFirstExecutionHandler = mFirstExecutionConditionServiceImpl
                .createFirstExecutionHandler(mExecutor, mActivationBarrierHelper, mFirstExecutionConditionChecker);
    }

    @Test
    public void testSetDelay() {
        final long delay = 10;
        mFirstExecutionHandler.setInitialDelaySeconds(delay);
        verify(mFirstExecutionConditionChecker).setDelaySeconds(delay);
    }

    @Test
    public void testOnStartupChanged() {
        mFirstExecutionConditionServiceImpl.updateConfig(utilityServiceConfiguration);
        verify(mFirstExecutionConditionChecker).updateConfig(same(utilityServiceConfiguration));
    }

    @Test
    public void testTryExecuteShould() {
        when(mFirstExecutionConditionChecker.shouldExecute()).thenReturn(true);
        assertThat(mFirstExecutionHandler.tryExecute(mLaunchDelay)).isTrue();
        verify(mActivationBarrierHelper).subscribeIfNeeded(eq(mLaunchDelay * 1000L), same(mExecutor));
        verify(mFirstExecutionConditionChecker).setFirstExecutionAlreadyAllowed();
    }

    @Test
    public void testTryExecuteShouldNot() {
        when(mFirstExecutionConditionChecker.shouldExecute()).thenReturn(false);
        assertThat(mFirstExecutionHandler.tryExecute(mLaunchDelay)).isFalse();
        verifyNoMoreInteractions(mActivationBarrierHelper);
        verify(mFirstExecutionConditionChecker, never()).setFirstExecutionAlreadyAllowed();
    }

    @Test
    public void canExecute() {
        when(mFirstExecutionConditionChecker.shouldExecute()).thenReturn(true);
        assertThat(mFirstExecutionHandler.canExecute()).isTrue();
        assertThat(mFirstExecutionHandler.canExecute()).isTrue();
        verify(mFirstExecutionConditionChecker, times(2)).setFirstExecutionAlreadyAllowed();
        verify(mFirstExecutionConditionChecker, times(2)).shouldExecute();
    }

    @Test
    public void canNotExecute() {
        Class field = Field.class;
        field.getDeclaredFields();
        when(mFirstExecutionConditionChecker.shouldExecute()).thenReturn(false);
        verify(mFirstExecutionConditionChecker, never()).setFirstExecutionAlreadyAllowed();
        assertThat(mFirstExecutionHandler.canExecute()).isFalse();
    }

    @RunWith(RobolectricTestRunner.class)
    public static class FirstExecutionConditionCheckerTest {

        @Mock
        private FirstExecutionConditionServiceImpl.FirstExecutionDelayChecker mFirstExecutionDelayChecker;
        @Mock
        private UtilityServiceConfiguration configuration;

        private FirstExecutionConditionServiceImpl.FirstExecutionConditionChecker mFirstExecutionConditionChecker;
        private final long mFirstStartupTime = 1000;
        private final long mLastStartupTime = 2000;
        private final long mDelay = 10;

        @Before
        public void setUp() {
            MockitoAnnotations.openMocks(this);

            when(configuration.getInitialConfigTime()).thenReturn(mFirstStartupTime);
            when(configuration.getLastUpdateConfigTime()).thenReturn(mLastStartupTime);
        }

        @Test
        public void testDefault() {
            mFirstExecutionConditionChecker = new FirstExecutionConditionServiceImpl.FirstExecutionConditionChecker(
                    null,
                    mFirstExecutionDelayChecker,
                    "Some string tag"
            );
            mFirstExecutionConditionChecker.shouldExecute();
            when(mFirstExecutionDelayChecker.delaySinceFirstStartupWasPassed(0, 0, Long.MAX_VALUE))
                    .thenReturn(true);
            verify(mFirstExecutionDelayChecker).delaySinceFirstStartupWasPassed(0, 0, Long.MAX_VALUE);
            assertThat(mFirstExecutionConditionChecker.shouldExecute()).isTrue();
        }

        @Test
        public void testIfDelayPassed() {
            mFirstExecutionConditionChecker = new FirstExecutionConditionServiceImpl.FirstExecutionConditionChecker(
                    configuration,
                    mFirstExecutionDelayChecker,
                    "Tag"
            );
            when(mFirstExecutionDelayChecker.delaySinceFirstStartupWasPassed(
                    mFirstStartupTime,
                    mLastStartupTime,
                    TimeUnit.SECONDS.toMillis(mDelay)
            )).thenReturn(true);
            mFirstExecutionConditionChecker.setDelaySeconds(mDelay);
            assertThat(mFirstExecutionConditionChecker.shouldExecute()).isTrue();
        }

        @Test
        public void testIfDelayDidNotPass() {
            mFirstExecutionConditionChecker = new FirstExecutionConditionServiceImpl.FirstExecutionConditionChecker(
                    configuration,
                    mFirstExecutionDelayChecker,
                    "Tag"
            );
            when(mFirstExecutionDelayChecker.delaySinceFirstStartupWasPassed(
                    mFirstStartupTime,
                    mLastStartupTime,
                    TimeUnit.SECONDS.toMillis(mDelay)
            )).thenReturn(false);
            mFirstExecutionConditionChecker.setDelaySeconds(mDelay);
            assertThat(mFirstExecutionConditionChecker.shouldExecute()).isFalse();
        }

        @Test
        public void onStartupChanged() {
            mFirstExecutionConditionChecker = new FirstExecutionConditionServiceImpl.FirstExecutionConditionChecker(
                    null,
                    mFirstExecutionDelayChecker,
                    "Tag"
            );
            when(mFirstExecutionDelayChecker.delaySinceFirstStartupWasPassed(
                    mFirstStartupTime,
                    mLastStartupTime,
                    TimeUnit.SECONDS.toMillis(mDelay)
            )).thenReturn(true);
            mFirstExecutionConditionChecker.updateConfig(configuration);
            mFirstExecutionConditionChecker.setDelaySeconds(mDelay);
            assertThat(mFirstExecutionConditionChecker.shouldExecute()).isTrue();
        }

        @Test
        public void testAfterFirstExecution() {
            mFirstExecutionConditionChecker = new FirstExecutionConditionServiceImpl.FirstExecutionConditionChecker(
                    null,
                    mFirstExecutionDelayChecker,
                    "Tag"
            );
            when(mFirstExecutionDelayChecker.delaySinceFirstStartupWasPassed(anyLong(), anyLong(), anyLong()))
                    .thenReturn(false);
            mFirstExecutionConditionChecker.setFirstExecutionAlreadyAllowed();
            assertThat(mFirstExecutionConditionChecker.shouldExecute()).isTrue();
        }

        @Test
        public void tag() {
            String tag = "Some tag";
            assertThat(new FirstExecutionConditionServiceImpl.FirstExecutionConditionChecker(
                    null, mFirstExecutionDelayChecker, tag
            ).tag).isEqualTo(tag);
        }

    }
}
