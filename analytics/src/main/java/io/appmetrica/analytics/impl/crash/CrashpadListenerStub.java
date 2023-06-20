package io.appmetrica.analytics.impl.crash;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;

public class CrashpadListenerStub implements CrashpadListener {

    @Override
    public void addListener(@NonNull Consumer<String> listener) {

    }

    @Override
    public void removeListener(@NonNull Consumer<String> listener) {

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }
}
