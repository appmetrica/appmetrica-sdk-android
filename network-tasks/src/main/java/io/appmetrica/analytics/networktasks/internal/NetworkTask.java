package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy;
import io.appmetrica.analytics.coreutils.internal.asserts.DebugAssert;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLSocketFactory;

public class NetworkTask {

    public enum Method { GET, POST }

    public interface ShouldTryNextHostCondition {

        boolean shouldTryNextHost(int responseCode);
    }

    private enum State { EMPTY, PENDING, PREPARING, EXECUTING, SUCCESS, FAILED, SHOULD_NOT_EXECUTE, FINISHED, REMOVED }

    private static final String TAG = "[NetworkTask]";

    @NonNull
    private State state = State.EMPTY;
    @NonNull
    private final Executor executor;
    @NonNull
    private final IExecutionPolicy connectionBasedExecutionPolicy;
    @NonNull
    private final ExponentialBackoffPolicy exponentialBackoffPolicy;
    @NonNull
    private final UnderlyingNetworkTask underlyingTask;
    @NonNull
    private final List<ShouldTryNextHostCondition> shouldTryNextHostConditions;
    @NonNull
    private final String userAgent;

    public NetworkTask(@NonNull Executor executor,
                       @NonNull IExecutionPolicy connectionBasedExecutionPolicy,
                       @NonNull ExponentialBackoffPolicy exponentialBackoffPolicy,
                       @NonNull UnderlyingNetworkTask underlyingTask,
                       @NonNull List<ShouldTryNextHostCondition> shouldTryNextHostConditions,
                       @NonNull String userAgent) {
        this.executor = executor;
        this.connectionBasedExecutionPolicy = connectionBasedExecutionPolicy;
        this.exponentialBackoffPolicy = exponentialBackoffPolicy;
        this.underlyingTask = underlyingTask;
        this.shouldTryNextHostConditions = shouldTryNextHostConditions;
        this.userAgent = userAgent;
    }

    /**
     * Calls before the start of the task.
     * @return {@code true} if the task has been well prepared and it's needed to be further processed,
     * {@code false} otherwise.
     */
    public boolean onCreateNetworkTask() {
        DebugLogger.INSTANCE.info(TAG, "onCreateNetworkTask");
        boolean result = maybeChangeStateTo(State.PREPARING);
        if (result) {
            return underlyingTask.onCreateTask();
        } else {
            return false;
        }
    }

    /**
     * Calls when task is completed.
     * @return {@code true} if the task is completed correctly
     * (e.g. Http code is 200 and all data provided in full disclosure), {@code false} otherwise.
     */
    public boolean onRequestComplete() {
        DebugLogger.INSTANCE.info(TAG, "onRequestComplete (url = %s)", getUrl());
        boolean switchedState = false;
        boolean successful = false;
        synchronized (this) {
            if (canSwitchStateTo(State.SUCCESS, State.FAILED)) {
                switchedState = true;
                successful = underlyingTask.onRequestComplete();
                if (successful) {
                    state = State.SUCCESS;
                } else {
                    state = State.FAILED;
                }
            }
        }
        if (switchedState) {
            underlyingTask.onPostRequestComplete(successful);
        }
        return successful;
    }

    public boolean onPerformRequest() {
        DebugLogger.INSTANCE.info(TAG, "onPerformRequest");
        boolean result = maybeChangeStateTo(State.EXECUTING);
        if (result) {
            underlyingTask.getFullUrlFormer().incrementAttemptNumber();
            underlyingTask.getFullUrlFormer().buildAndSetFullHostUrl();
            underlyingTask.onPerformRequest();
        }
        return result;
    }

    public void onRequestError(@Nullable final Throwable error) {
        DebugLogger.INSTANCE.info(TAG, "onRequestError: %s", error);
        if (maybeChangeStateTo(State.FAILED)) {
            underlyingTask.onRequestError(error);
        }
    }

    public void onShouldNotExecute() {
        DebugLogger.INSTANCE.info(TAG, "onShouldNotExecute");
        if (maybeChangeStateTo(State.SHOULD_NOT_EXECUTE)) {
            underlyingTask.onShouldNotExecute();
        }
    }

    @Nullable
    public String getUrl() {
        return underlyingTask.getFullUrlFormer().getUrl();
    }

    @NonNull
    public RequestDataHolder getRequestDataHolder() {
        return underlyingTask.getRequestDataHolder();
    }

    @NonNull
    public ResponseDataHolder getResponseDataHolder() {
        return underlyingTask.getResponseDataHolder();
    }

    @NonNull
    public String description() {
        return underlyingTask.description();
    }

    public synchronized boolean shouldTryNextHost() {
        boolean hasMoreHosts = underlyingTask.getFullUrlFormer().hasMoreHosts();
        boolean allConditionsAreTrue = shouldTryNextHostAllConditionsAreTrue(
                underlyingTask.getResponseDataHolder().getResponseCode()
        );
        boolean result = state != State.REMOVED && state != State.FINISHED && hasMoreHosts && allConditionsAreTrue;
        DebugLogger.INSTANCE.info(
            TAG,
            "shouldTryNextHost? %b (state = %s, has more hosts = %b, all conditions are true = %b)",
            result,
            state,
            hasMoreHosts,
            allConditionsAreTrue
        );
        return result;
    }

    public boolean onTaskAdded() {
        DebugLogger.INSTANCE.info(TAG, "onTaskAdded");
        boolean result = maybeChangeStateTo(State.PENDING);
        if (result) {
            underlyingTask.onTaskAdded();
        }
        return result;
    }

    public void onTaskFinished() {
        DebugLogger.INSTANCE.info(TAG, "onTaskFinished from state %s", state);
        State oldState;
        boolean result;
        synchronized (this) {
            oldState = state;
            result = maybeChangeStateTo(State.FINISHED);
        }
        if (result) {
            underlyingTask.onTaskFinished();
            if (oldState == State.SUCCESS) {
                underlyingTask.onSuccessfulTaskFinished();
            } else if (oldState == State.FAILED || oldState == State.SHOULD_NOT_EXECUTE) {
                underlyingTask.onUnsuccessfulTaskFinished();
            }
        }
    }

    public void onTaskRemoved() {
        DebugLogger.INSTANCE.info(TAG, "onTaskRemoved");
        if (maybeChangeStateTo(State.REMOVED)) {
            underlyingTask.onTaskRemoved();
        }
    }

    @Nullable
    public RetryPolicyConfig getRetryPolicyConfig() {
        return underlyingTask.getRetryPolicyConfig();
    }

    @NonNull
    public Executor getExecutor() {
        return executor;
    }

    @NonNull
    public IExecutionPolicy getConnectionExecutionPolicy() {
        return connectionBasedExecutionPolicy;
    }

    @NonNull
    public ExponentialBackoffPolicy getExponentialBackoffPolicy() {
        return exponentialBackoffPolicy;
    }

    @Nullable
    public SSLSocketFactory getSslSocketFactory() {
        return underlyingTask.getSslSocketFactory();
    }

    private boolean shouldTryNextHostAllConditionsAreTrue(int responseCode) {
        for (ShouldTryNextHostCondition condition : shouldTryNextHostConditions) {
            if (!condition.shouldTryNextHost(responseCode)) {
                return false;
            }
        }
        return true;
    }

    private synchronized boolean maybeChangeStateTo(@NonNull State newState) {
        if (canSwitchStateTo(newState)) {
            state = newState;
            return true;
        }
        return false;
    }

    private synchronized boolean canSwitchStateTo(@NonNull State... newStates) {
        Boolean result = true;
        final State oldState = state;
        for (State newState : newStates) {
            Boolean canSwitch = canSwitch(oldState, newState);
            DebugLogger.INSTANCE.info(TAG, "canSwitchState: %s -> %s: %b", oldState, newState, canSwitch);
            if (!Boolean.TRUE.equals(canSwitch)) {
                result = canSwitch;
                break;
            }
        }
        DebugAssert.assertNotNull(result, String.format("Unexpected state change: from %s to one of %s",
            oldState, Arrays.toString(newStates)));
        if (!Boolean.TRUE.equals(result)) {
            DebugLogger.INSTANCE.warning(
                TAG,
                "cannot change state from %s to one of %s",
                oldState,
                Arrays.toString(newStates)
            );
        }

        return Boolean.TRUE.equals(result);
    }

    @Nullable
    private Boolean canSwitch(@NonNull State from, @NonNull State to) {
        DebugLogger.INSTANCE.info(TAG, "canSwitchState: %s -> %s", from, to);
        switch (to) {
            case EMPTY:
                return null;
            case PENDING:
                return from == State.EMPTY;
            case SHOULD_NOT_EXECUTE:
                if (from == State.PREPARING) {
                    return true;
                }
            case PREPARING:
                if (from == State.PENDING) {
                    return true;
                } else if (from == State.REMOVED) {
                    return false;
                } else {
                    return null;
                }
            case EXECUTING:
                if (from == State.PREPARING || from == State.SUCCESS || from == State.FAILED) {
                    return true;
                } else if (from == State.REMOVED) {
                    return false;
                } else {
                    return null;
                }
            case SUCCESS:
            case FAILED:
                if (from == State.EXECUTING) {
                    return true;
                } else if (from == State.REMOVED) {
                    return false;
                } else {
                    return null;
                }
            case FINISHED:
                if (from == State.SUCCESS || from == State.FAILED || from == State.SHOULD_NOT_EXECUTE ||
                        from == State.PENDING || from == State.PREPARING || from == State.EXECUTING) {
                    return true;
                } else if (from == State.REMOVED) {
                    return false;
                } else {
                    return null;
                }
            case REMOVED:
                if (from == State.EMPTY) {
                    return null;
                }
                return from != State.REMOVED;
            default:
                return false;
        }
    }

    @NonNull
    public UnderlyingNetworkTask getUnderlyingTask() {
        return underlyingTask;
    }

    @NonNull
    public String getUserAgent() {
        return userAgent;
    }

    public boolean isRemoved() {
        return state == State.REMOVED;
    }
}
