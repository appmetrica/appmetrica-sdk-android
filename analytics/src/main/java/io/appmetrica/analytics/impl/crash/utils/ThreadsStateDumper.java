package io.appmetrica.analytics.impl.crash.utils;

import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.backport.BiFunction;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads;
import io.appmetrica.analytics.impl.crash.jvm.client.ThreadState;
import io.appmetrica.analytics.impl.utils.process.ProcessNameProvider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ThreadsStateDumper {

    public interface ThreadProvider {

        @NonNull
        Thread getMainThread();

        @Nullable
        StackTraceElement[] getMainThreadStacktrace();

        @NonNull
        Map<Thread, StackTraceElement[]> getAllThreadsStacktraces();

    }

    private final ThreadProvider threadProvider;
    private final BiFunction<Thread, StackTraceElement[], ThreadState> threadConverter;
    private final ProcessNameProvider processNameProvider;

    public ThreadsStateDumper() {
        this(new ThreadProvider() {

                 @Override
                 @NonNull
                 public Thread getMainThread() {
                     return Looper.getMainLooper().getThread();
                 }

                 @Nullable
                 @Override
                 public StackTraceElement[] getMainThreadStacktrace() {
                     return null;
                 }

                 @Override
                 @NonNull
                 public Map<Thread, StackTraceElement[]> getAllThreadsStacktraces() {
                     return Thread.getAllStackTraces();
                 }
             }, new FullStateConverter(),
            ClientServiceLocator.getInstance().getProcessNameProvider()
        );
    }

    public ThreadsStateDumper(
        @NonNull ThreadProvider threadProvider,
        @NonNull BiFunction<Thread, StackTraceElement[], ThreadState> converter,
        @NonNull ProcessNameProvider processNameProvider
    ) {
        this.threadProvider = threadProvider;
        this.threadConverter = converter;
        this.processNameProvider = processNameProvider;
    }

    /**
     * @return String dump of all a thread details and stacktraces.
     */
    public AllThreads getThreadsDumpForAnr() {
        Thread mainThread = threadProvider.getMainThread();
        return new AllThreads(
            getMainThreadState(mainThread),
            getAllThreadsDump(mainThread, null),
            processNameProvider.getProcessName()
        );
    }

    public List<ThreadState> getThreadsDumpForCrash(@Nullable Thread excludedThread) {
        Thread mainThread = threadProvider.getMainThread();
        List<ThreadState> allThreads = getAllThreadsDump(mainThread, excludedThread);
        if (excludedThread != mainThread) {
            allThreads.add(0, getMainThreadState(mainThread));
        }
        return allThreads;
    }

    private ThreadState getMainThreadState(@NonNull Thread mainThread) {
        StackTraceElement[] mainStackTrace = null;
        try {
            mainStackTrace = threadProvider.getMainThreadStacktrace();
            if (mainStackTrace == null) {
                mainStackTrace = mainThread.getStackTrace();
            }
        } catch (SecurityException e) { /* do nothing */ }

        return threadConverter.apply(mainThread, mainStackTrace);
    }

    private List<ThreadState> getAllThreadsDump(final @NonNull Thread mainThread,
                                                @Nullable Thread excludedThread) {
        List<ThreadState> threads = new ArrayList<ThreadState>();

        final Comparator threadComparator = new Comparator<Thread>() {
            @Override
            public int compare(Thread first, Thread second) {
                if (first == second) {
                    return 0;
                }
                return StringUtils.compare(first.getName(), second.getName());
            }
        };

        @SuppressWarnings("unchecked")
        final Map<Thread, StackTraceElement[]> stackTraces = new TreeMap<Thread, StackTraceElement[]>(threadComparator);

        Map<Thread, StackTraceElement[]> allStackTraces = null;

        try {
            allStackTraces = threadProvider.getAllThreadsStacktraces();
        } catch (SecurityException e) { /* do nothing */ }

        if (allStackTraces != null) {
            stackTraces.putAll(allStackTraces);
        }

        if (excludedThread != null) {
            stackTraces.remove(excludedThread);
        }

        for (Map.Entry<Thread, StackTraceElement[]> entry : stackTraces.entrySet()) {
            final Thread thread = entry.getKey();
            if (thread == mainThread || thread == excludedThread) {
                continue;
            }

            final StackTraceElement[] stackTrace = entry.getValue();

            threads.add(threadConverter.apply(thread, stackTrace));
        }

        return threads;
    }
}
