package io.appmetrica.analytics.logger.common;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.common.impl.LogMessageConstructor;
import io.appmetrica.analytics.logger.common.impl.LogMessageSplitter;
import io.appmetrica.analytics.logger.common.impl.MultilineLogger;
import io.appmetrica.analytics.logger.common.impl.SystemLogger;

/**
 * Class for release logs. Can be disabled
 */
public abstract class BaseReleaseLogger {

    @NonNull
    private volatile static String packageNameTag = "";
    @NonNull
    private final String logPrefix;
    private volatile boolean enabled = false;

    @NonNull
    private final MultilineLogger logger;

    /**
     * Constructor of ReleaseLogger
     *
     * @param tag String that is passed to system logger as tag
     * @param logPrefix String to add to every log
     */
    public BaseReleaseLogger(@NonNull String tag, @NonNull String logPrefix) {
        this.logger = new MultilineLogger(
            new SystemLogger(tag),
            new LogMessageConstructor(),
            new LogMessageSplitter()
        );
        this.logPrefix = logPrefix;
    }

    /**
     * @return prefix of every log
     */
    @NonNull
    public String getPrefix() {
        return packageNameTag + logPrefix;
    }

    /**
     * Initialises ReleaseLogger
     *
     * @param context Context of the application
     */
    public static void init(@NonNull Context context) {
        packageNameTag = "[" + context.getPackageName() + "] : ";
    }

    /**
     * Sets whether the logger is enabled
     *
     * @param enabled whether the logger is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Logs message with arguments to info log
     *
     * @param message String with placeholders
     * @param args arguments
     */
    public void info(@Nullable String message, @Nullable Object... args) {
        if (enabled) {
            logger.info(getPrefix(), message, args);
        }
    }

    /**
     * Logs message with arguments to warning log
     *
     * @param message String with placeholders
     * @param args arguments
     */
    public void warning(@Nullable String message, @Nullable Object... args) {
        if (enabled) {
            logger.warning(getPrefix(), message, args);
        }
    }

    /**
     * Logs message with arguments to error log
     *
     * @param message String with placeholders
     * @param args arguments
     */
    public void error(@Nullable String message, @Nullable Object... args) {
        if (enabled) {
            logger.error(getPrefix(), message, args);
        }
    }

    /**
     * Logs message with arguments to error log
     *
     * @param throwable Throwable to log
     * @param message String with placeholders
     * @param args arguments
     */
    public void error(@Nullable Throwable throwable, @Nullable String message, @Nullable Object... args) {
        if (enabled) {
            logger.error(getPrefix(), throwable, message, args);
        }
    }
}
