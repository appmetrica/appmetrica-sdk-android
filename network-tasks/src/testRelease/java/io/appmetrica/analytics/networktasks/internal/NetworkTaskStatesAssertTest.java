package io.appmetrica.analytics.networktasks.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy;
import io.appmetrica.analytics.coreutils.internal.asserts.DebugAssert;
import io.appmetrica.analytics.networktasks.internal.ExponentialBackoffPolicy;
import io.appmetrica.analytics.testutils.MockedStaticRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

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
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskAddedPendingTask() {
        networkTask.onTaskAdded();
        networkTask.onTaskAdded();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskAddedCreatedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskAdded();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskAddedExecutingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onTaskAdded();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskAddedExecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();
        networkTask.onTaskAdded();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskAddedFailedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        networkTask.onTaskAdded();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskAddedUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onTaskAdded();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskAddedFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();
        networkTask.onTaskAdded();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskAddedRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();
        networkTask.onTaskAdded();
        checkAssertIsNotExecuted();
    }

    // end region

    // region onCreate

    @Test
    public void onCreateEmptyTask() {
        networkTask.onCreateNetworkTask();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onCreatePendingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onCreateAlreadyCreatedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onCreateNetworkTask();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onCreateExecutingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onCreateNetworkTask();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onCreateExecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();
        networkTask.onCreateNetworkTask();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onCreateFailedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        networkTask.onCreateNetworkTask();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onCreateUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onCreateNetworkTask();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onCreateFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();
        networkTask.onCreateNetworkTask();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onCreateRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();
        networkTask.onCreateNetworkTask();
        checkAssertIsNotExecuted();
    }

    // end region

    // region onPerformRequest

    @Test
    public void onPerformRequestEmptyTask() {
        networkTask.onPerformRequest();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onPerformRequestPendingTask() {
        networkTask.onTaskAdded();
        networkTask.onPerformRequest();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onPerformRequestCreatedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onPerformRequestExecutingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onPerformRequest();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onPerformRequestCompletedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();
        networkTask.onPerformRequest();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onPerformRequestFailedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        networkTask.onPerformRequest();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onPerformRequestUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onPerformRequest();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onPerformRequestFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();
        networkTask.onPerformRequest();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onPerformRequestRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();
        networkTask.onPerformRequest();
        checkAssertIsNotExecuted();
    }

    // end region

    // region onRequestComplete

    @Test
    public void onRequestCompleteEmptyTask() {
        networkTask.onRequestComplete();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestCompletePendingTask() {
        networkTask.onTaskAdded();
        networkTask.onRequestComplete();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestCompleteCreatedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onRequestComplete();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestCompleteExecutingCompletedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestCompleteAlreadyExecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();
        networkTask.onRequestComplete();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestCompleteFailedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        networkTask.onRequestComplete();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestCompleteUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onRequestComplete();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestCompleteFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();
        networkTask.onRequestComplete();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestCompleteRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();
        networkTask.onRequestComplete();
        checkAssertIsNotExecuted();
    }

    // end region

    // region onRequestError

    @Test
    public void onRequestErrorEmptyTask() {
        networkTask.onRequestError(null);
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestErrorPendingTask() {
        networkTask.onTaskAdded();
        networkTask.onRequestError(null);
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestErrorPreparingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onRequestError(null);
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestErrorExecutingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestErrorUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onRequestError(null);
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestErrorFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        networkTask.onTaskRemoved();
        networkTask.onRequestError(null);
        checkAssertIsNotExecuted();
    }

    @Test
    public void onRequestErrorRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);
        networkTask.onTaskRemoved();
        networkTask.onRequestError(null);
        checkAssertIsNotExecuted();
    }

    // end region

    // region onShouldNotExecute

    @Test
    public void onShouldNotExecuteEmptyTask() {
        networkTask.onShouldNotExecute();
        networkTask.onTaskAdded();
        checkAssertIsNotExecuted();
    }

    // end region

    // region onTaskFinished

    @Test
    public void onTaskFinishedEmptyTask() {
        networkTask.onTaskFinished();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskFinishedPendingTask() {
        networkTask.onTaskAdded();
        networkTask.onTaskFinished();

        networkTask.onCreateNetworkTask();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskFinishedCreatedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();

        networkTask.onPerformRequest();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskFinishedExecutingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onTaskFinished();

        networkTask.onRequestComplete();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskFinishedExecutedTaskSuccess() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();

        networkTask.onTaskFinished();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskFinishedExecutedTaskFailure() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();

        networkTask.onTaskFinished();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskFinishedFailedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);

        networkTask.onTaskFinished();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskFinishedUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onTaskFinished();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskFinishedAlreadyFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();
        networkTask.onTaskFinished();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskFinishedRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();
        networkTask.onTaskFinished();
        checkAssertIsNotExecuted();
    }

    // end region

    // region onTaskRemoved

    @Test
    public void onTaskRemovedEmptyTask() {
        networkTask.onTaskRemoved();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskRemovedPendingTask() {
        networkTask.onTaskAdded();
        networkTask.onTaskRemoved();

        networkTask.onCreateNetworkTask();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskRemovedCreatedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();

        networkTask.onPerformRequest();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskRemovedExecutingTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onTaskRemoved();

        networkTask.onRequestComplete();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskRemovedExecutedTaskSuccess() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();

        networkTask.onTaskRemoved();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskRemovedExecutedTaskFailure() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestComplete();

        networkTask.onTaskRemoved();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskRemovedFailedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onPerformRequest();
        networkTask.onRequestError(null);

        networkTask.onTaskRemoved();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskRemovedUnexecutedTask() {
        networkTask.onTaskAdded();
        networkTask.onShouldNotExecute();
        networkTask.onTaskRemoved();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskRemovedFinishedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskFinished();
        networkTask.onTaskRemoved();
        checkAssertIsNotExecuted();
    }

    @Test
    public void onTaskRemovedAlreadyRemovedTask() {
        networkTask.onTaskAdded();
        networkTask.onCreateNetworkTask();
        networkTask.onTaskRemoved();
        networkTask.onTaskRemoved();
        checkAssertIsNotExecuted();
    }

    // end region

    private void checkAssertIsNotExecuted() {
        sDebugAssert.getStaticMock().verifyNoInteractions();
    }
}
