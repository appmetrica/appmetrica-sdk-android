package io.appmetrica.analytics.coreapi.internal.executors;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

public interface IHandlerExecutor extends ICommonExecutor {

    @NonNull
    Handler getHandler();

    @NonNull
    Looper getLooper();

}
