package io.appmetrica.analytics.testutils;

import android.os.Handler;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;

public class ClientExecutorProviderStub extends ClientExecutorProvider {

    private final ICommonExecutor apiProxyExecutor = new StubbedBlockingExecutor();

    @NonNull
    @Override
    public ICommonExecutor getApiProxyExecutor() {
        return apiProxyExecutor;
    }

    @NonNull
    @Override
    public IHandlerExecutor getDefaultExecutor() {
        return new StubbedBlockingExecutor();
    }

    @NonNull
    @Override
    public ICommonExecutor getReportSenderExecutor() {
        return new StubbedBlockingExecutor();
    }

    @NonNull
    @Override
    public Handler getMainHandler() {
        return TestUtils.createBlockingExecutionHandlerStub();
    }
}
