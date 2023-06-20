package io.appmetrica.analytics.impl.utils.executors;

import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    private static final String PREFIX = "IAA-";

    public static final String CLIENT_DEFAULT_THREAD = PREFIX + "CDE";
    public static final String CLIENT_REPORTS_SENDER_THREAD = PREFIX + "CRS";
    public static final String CLIENT_API_PROXY_THREAD = PREFIX + "CAPT";

    public static final String SERVICE_CORE = PREFIX + "SC";
    public static final String SERVICE_TASKS_EXECUTOR = PREFIX + "STE";
    public static final String SERVICE_SUPPORT_IO_EXECUTOR = PREFIX + "SIO";
    public static final String SERVICE_MODULE_THREAD = PREFIX + "SMH-1";
    public static final String SERVICE_NETWORK_TASK_PROCESSOR_EXECUTOR =
            PREFIX + "SNTPE";
    public static final String SERVICE_DEFAULT_EXECUTOR = PREFIX + "SDE";

    public static final String SERVICE_HMS_REFERRER_THREAD = PREFIX + "SHMSR";

    public static final String DB_WORKER_THREAD_PATTERN = PREFIX + "DW-%s";

    public static final String CUSTOM_MODULE_EXECUTOR_PATTERN = PREFIX + "M-%s";
    private static final AtomicInteger sThreadNum = new AtomicInteger(0);

    private final String mThreadName;

    public NamedThreadFactory(String threadName) {
        mThreadName = threadName;
    }

    public InterruptionSafeThread newThread(Runnable r) {
        return new InterruptionSafeThread(r, getThreadNameWithNumber());
    }

    private String getThreadNameWithNumber() {
        return adoptThreadName(mThreadName);
    }

    public InterruptionSafeHandlerThread newHandlerThread() {
        return new InterruptionSafeHandlerThread(getThreadNameWithNumber());
    }

    public static InterruptionSafeThread newThread(String name, Runnable r) {
        return new NamedThreadFactory(name).newThread(r);
    }

    public static InterruptionSafeHandlerThread newHandlerThread(String name) {
        return new NamedThreadFactory(name).newHandlerThread();
    }

    public static String adoptThreadName(String threadName) {
        return threadName + "-" + nextThreadNum();
    }

    public static int nextThreadNum() {
        return sThreadNum.incrementAndGet();
    }
}
