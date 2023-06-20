package io.appmetrica.analytics.coreutils.internal.logger;

import java.util.Locale;

class DebugLogger extends BaseLogger {

    static final int STACK_OFFSET = 5;

    public DebugLogger(final boolean enabled) {
        super(enabled);
    }

    @Override
    protected String getTag() {
        return "AppMetricaDebug";
    }

    @Override
    protected String getPrefix() {
        return "";
    }

    @Override
    protected String formatMessage(final String message, final Object[] params) {
        String msg = (params == null || params.length == 0) ? message : String.format(Locale.US, message, params);
        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();
        String caller = "<unknown>";

        for (int i = STACK_OFFSET; i < trace.length; i++) {
            String executionPointClassName = trace[i].getClassName();
            if (!YLoggerImpl.REGISTERED_LOGGER_CLASSES.contains(executionPointClassName)) {
                String callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
                callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);

                caller = callingClass + "." + trace[i].getMethodName();
                break;
            }
        }
        return String.format(Locale.US, "[%d] %s: %s", Thread.currentThread().getId(), caller, msg);
    }
}
