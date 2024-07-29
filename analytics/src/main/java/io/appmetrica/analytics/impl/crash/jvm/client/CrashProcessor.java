package io.appmetrica.analytics.impl.crash.jvm.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.ICrashTransformer;
import io.appmetrica.analytics.impl.ExtraMetaInfoRetriever;

public abstract class CrashProcessor implements ICrashProcessor {

    public interface Rule {
        boolean shouldReportCrash(Throwable exception);
    }

    @NonNull
    private final Rule mRule;
    @Nullable
    private final ICrashTransformer mCustomCrashTransformer;
    @NonNull
    private final ExtraMetaInfoRetriever extraMetaInfoRetriever;

    CrashProcessor(@NonNull Rule rule,
                   @Nullable ICrashTransformer customCrashTransformer,
                   @NonNull ExtraMetaInfoRetriever extraMetaInfoRetriever) {
        mRule = rule;
        mCustomCrashTransformer = customCrashTransformer;
        this.extraMetaInfoRetriever = extraMetaInfoRetriever;
    }

    @Override
    public void processCrash(@Nullable Throwable originalException, @NonNull AllThreads allThreads) {
        if (mRule.shouldReportCrash(originalException)) {
            Throwable throwableToSend = originalException;
            if (mCustomCrashTransformer != null && throwableToSend != null) {
                Throwable processedThrowable = mCustomCrashTransformer.process(throwableToSend);
                if (processedThrowable == null) {
                    return;
                } else {
                    throwableToSend = processedThrowable;
                }
            }
            sendCrash(
                    UnhandledExceptionFactory.getUnhandledExceptionFromJava(
                            throwableToSend,
                            allThreads,
                            null,
                            extraMetaInfoRetriever.getBuildId(),
                            extraMetaInfoRetriever.isOffline()
                    )
            );
        }
    }

    abstract void sendCrash(@NonNull UnhandledException unhandledException);

    @VisibleForTesting
    public Rule getRule() {
        return mRule;
    }

    @VisibleForTesting
    @Nullable
    public ICrashTransformer getCrashTransformer() {
        return mCustomCrashTransformer;
    }
}
