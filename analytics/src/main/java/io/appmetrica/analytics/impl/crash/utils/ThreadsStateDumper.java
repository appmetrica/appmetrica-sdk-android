package io.appmetrica.analytics.impl.crash.utils;

import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.backport.BiFunction;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.ThreadState;
import io.appmetrica.analytics.impl.utils.ProcessDetector;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ThreadsStateDumper {

    interface ThreadProvider {

        Thread getMainThread();

        Map<Thread, StackTraceElement[]> getAllOtherThreads();

    }

    private final ThreadProvider threadProvider;
    private final BiFunction<Thread, StackTraceElement[], ThreadState> threadConverter;
    private final ProcessDetector processDetector;

    public ThreadsStateDumper() {
        this(new ThreadProvider() {
            @Override
            public Thread getMainThread() {
                return Looper.getMainLooper().getThread();
            }

            @Override
            public Map<Thread, StackTraceElement[]> getAllOtherThreads() {
                return Thread.getAllStackTraces();
            }
        }, new FullStateConverter(),
            ClientServiceLocator.getInstance().getProcessDetector()
        );
    }

    @VisibleForTesting()
    ThreadsStateDumper(@NonNull ThreadProvider threadProvider,
                       @NonNull BiFunction<Thread, StackTraceElement[], ThreadState> converter,
                       @NonNull ProcessDetector processDetector) {
        this.threadProvider = threadProvider;
        this.threadConverter = converter;
        this.processDetector = processDetector;
    }

    /**
     * @return String dump of all a thread details {@see {@link #}}
     * and stacktraces.
     */
    //region changed code
    public AllThreads getThreadsDumpForAnr() {
        Thread mainThread = threadProvider.getMainThread();
        return new AllThreads(
                getMainThreadState(mainThread),
                getAllThreadsDump(mainThread, null),
                processDetector.getProcessName()
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
            mainStackTrace = mainThread.getStackTrace();
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
                //region changed code
                return StringUtils.compare(first.getName(), second.getName());
                //endregion
            }
        };

        final Map<Thread, StackTraceElement[]> stackTraces = new TreeMap<Thread, StackTraceElement[]>(threadComparator);

        Map<Thread, StackTraceElement[]> allStackTraces = null;

        try {
            allStackTraces = threadProvider.getAllOtherThreads();
        } catch (SecurityException e) { /* do nothing */ }

        if (allStackTraces != null) {
            stackTraces.putAll(allStackTraces);
        }

        //region changed code
        if (excludedThread != null) {
            stackTraces.remove(excludedThread);
        }
        //endregion

        for (Map.Entry<Thread, StackTraceElement[]> entry : stackTraces.entrySet()) {
            final Thread thread = entry.getKey();
            if (thread == mainThread || thread == excludedThread) {
                continue;
            }

            final StackTraceElement[] stackTrace = entry.getValue();

            //region changed code
            threads.add(threadConverter.apply(thread, stackTrace));
            //endregion
        }

        //region changed code
        return threads;
    }
    //endregion

}
