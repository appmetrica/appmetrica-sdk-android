package io.appmetrica.analytics.networktasks.internal;

import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy;
import io.appmetrica.analytics.coreutils.internal.asserts.DebugAssert;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class NetworkTaskStatesAssertTest {

    @Rule
    public final MockedStaticRule<DebugAssert> sDebugAssert = new MockedStaticRule<>(DebugAssert.class);
    @Mock
    private Executor executor;
    @Mock
    private IExecutionPolicy executionPolicy;
    @Mock
    private ExponentialBackoffPolicy exponentialBackoffPolicy;
    @Mock
    private UnderlyingNetworkTask underlyingNetworkTask;
    @Mock
    private NetworkTask.ShouldTryNextHostCondition shouldTryNextHostCondition;
    @Mock
    private FullUrlFormer fullUrlFormer;

    private List<NetworkTask.ShouldTryNextHostCondition> shouldTryNextHostConditions =
        Collections.singletonList(shouldTryNextHostCondition);

    private NetworkTask networkTask;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(executionPolicy.canBeExecuted()).thenReturn(true);
        when(exponentialBackoffPolicy.canBeExecuted(any(RetryPolicyConfig.class))).thenReturn(true);
        when(underlyingNetworkTask.onCreateTask()).thenReturn(true);
        when(underlyingNetworkTask.onRequestComplete()).thenReturn(true);
        when(underlyingNetworkTask.getFullUrlFormer()).thenReturn(fullUrlFormer);

        networkTask = new NetworkTask(
            executor,
            executionPolicy,
            exponentialBackoffPolicy,
            underlyingNetworkTask,
            shouldTryNextHostConditions,
            "userAgent"
        );
    }

    // region onTaskAdded

    @Test
    public void onTaskAddedEmptyTask() {
        networkTask.onTaskAdded();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskAddedPendingTask() {
        networkTask.onTaskAdded();
        networkTask.onTaskAdded();
        checkAssertIsExecuted(false);
    }

    @Test
    public void oTaskAddedCreatedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskAdded();
        checkAssertIsExecuted(false);
    }

    @Test
    public void onTaskAddedExecutingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onTaskAdded();
        checkAssertIsExecuted(false);
    }

    @Test
    public void onTaskAddedExecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();
        networkTask.onTaskAdded();
        checkAssertIsExecuted(false);
    }

    @Test
    public void onTaskAddedFailedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        networkTask.onTaskAdded();
        checkAssertIsExecuted(false);
    }

    @Test
    public void onTaskAddedUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onTaskAdded();
        checkAssertIsExecuted(false);
    }

    @Test
    public void onTaskAddedFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();
        networkTask.onTaskAdded();
        checkAssertIsExecuted(false);
    }

    @Test
    public void onTaskAddedRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();
        networkTask.onTaskAdded();
        checkAssertIsExecuted(false);
    }

    // end region

    // region onCreate

    @Test
    public void onCreateEmptyTask() {
        networkTask.onCreateNetworkTask();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onCreatePendingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onCreateAlreadyCreatedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onCreateNetworkTask();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onCreateExecutingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onCreateNetworkTask();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onCreateExecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();
        networkTask.onCreateNetworkTask();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onCreateFailedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        networkTask.onCreateNetworkTask();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onCreateUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onCreateNetworkTask();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onCreateFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();
        networkTask.onCreateNetworkTask();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onCreateRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();
        networkTask.onCreateNetworkTask();
        checkAssertIsExecuted(false);
    }

    // end region

    // region onPerformRequest

    @Test
    public void onPerformRequestEmptyTask() {
        networkTask.onPerformRequest();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onPerformRequestPendingTask() {
        networkTask.onTaskAdded();
        networkTask.onPerformRequest();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onPerformRequestCreatedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onPerformRequestExecutingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onPerformRequest();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onPerformRequestCompletedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();
        networkTask.onPerformRequest();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onPerformRequestFailedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        networkTask.onPerformRequest();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onPerformRequestUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onPerformRequest();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onPerformRequestFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();
        networkTask.onPerformRequest();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onPerformRequestRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();
        networkTask.onPerformRequest();
        checkAssertIsExecuted(false);
    }

    // end region

    // region onRequestComplete

    @Test
    public void onRequestCompleteEmptyTask() {
        networkTask.onRequestComplete();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onRequestCompletePendingTask() {
        networkTask.onTaskAdded();
        networkTask.onRequestComplete();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onRequestCompleteCreatedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onRequestComplete();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onRequestCompleteExecutingCompletedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onRequestCompleteAlreadyExecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();
        networkTask.onRequestComplete();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onRequestCompleteFailedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        networkTask.onRequestComplete();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onRequestCompleteUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onRequestComplete();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onRequestCompleteFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();
        networkTask.onRequestComplete();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onRequestCompleteRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();
        networkTask.onRequestComplete();
        checkAssertIsExecuted(false);
    }

    // end region

    // region onRequestError

    @Test
    public void onRequestErrorEmptyTask() {
        networkTask.onRequestError(null);
        checkAssertIsExecuted(null);
    }

    @Test
    public void onRequestErrorPendingTask() {
        networkTask.onTaskAdded();
        networkTask.onRequestError(null);
        checkAssertIsExecuted(null);
    }

    @Test
    public void onRequestErrorPreparingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onRequestError(null);
        checkAssertIsExecuted(null);
    }

    @Test
    public void onRequestErrorExecutingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        checkAssertIsExecuted(true);
    }

    @Test
    public void onRequestErrorUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onRequestError(null);
        checkAssertIsExecuted(null);
    }

    @Test
    public void onRequestErrorFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        networkTask.onTaskFinished();
        networkTask.onRequestError(null);
        checkAssertIsExecuted(null);
    }

    @Test
    public void onRequestErrorRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        networkTask.onTaskRemoved();
        networkTask.onRequestError(null);
        checkAssertIsExecuted(false);
    }

    // end region

    // region onShouldNotExecute

    @Test
    public void onShouldNotExecuteEmptyTask() {
        networkTask.onShouldNotExecute();
        networkTask.onTaskAdded();
        checkAssertIsExecuted(true);
    }

    // end region

    // region onTaskFinished

    @Test
    public void onTaskFinishedEmptyTask() {
        networkTask.onTaskFinished();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onTaskFinishedPendingTask() {
        networkTask.onTaskAdded();
        networkTask.onTaskFinished();

        networkTask.onCreateNetworkTask();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskFinishedCreatedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();

        networkTask.onPerformRequest();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskFinishedExecutingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onTaskFinished();

        networkTask.onRequestComplete();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskFinishedTaskSuccess() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();

        networkTask.onTaskFinished();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskFinishedExecutedTaskFailure() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();

        networkTask.onTaskFinished();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskFinishedFailedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);

        networkTask.onTaskFinished();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskFinishedUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onTaskFinished();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskFinishedAlreadyFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();
        networkTask.onTaskFinished();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onTaskFinishedRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();
        networkTask.onTaskFinished();
        checkAssertIsExecuted(false);
    }

    // end region

    // region onTaskRemoved

    @Test
    public void onTaskRemovedEmptyTask() {
        networkTask.onTaskRemoved();
        checkAssertIsExecuted(null);
    }

    @Test
    public void onTaskRemovedPendingTask() {
        networkTask.onTaskAdded();
        networkTask.onTaskRemoved();

        networkTask.onCreateNetworkTask();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskRemovedCreatedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();

        networkTask.onPerformRequest();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskRemovedExecutingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onTaskRemoved();

        networkTask.onRequestComplete();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskRemovedTaskSuccess() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();

        networkTask.onTaskRemoved();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskRemovedExecutedTaskFailure() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();

        networkTask.onTaskRemoved();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskRemovedFailedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);

        networkTask.onTaskRemoved();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskRemovedUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onTaskRemoved();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskRemovedFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();
        networkTask.onTaskRemoved();
        checkAssertIsExecuted(true);
    }

    @Test
    public void onTaskRemovedAlreadyRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();
        networkTask.onTaskFinished();
        checkAssertIsExecuted(false);
    }

    // end region

    private void checkAssertIsExecuted(final Boolean expected) {
        sDebugAssert.getStaticMock().verify(
            new MockedStatic.Verification() {
                @Override
                public void apply() {
                    DebugAssert.assertNotNull(eq(expected), startsWith("Unexpected state change:"));
                }
            },
            atLeastOnce()
        );
    }
}
