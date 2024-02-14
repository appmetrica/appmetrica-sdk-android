package io.appmetrica.analytics.logger.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;

public class YLoggerImpl {

    private final boolean enabled;
    @NonNull
    private final BaseLogger baseLogConsumer;
    @NonNull
    private final IMessageLogConsumer<String> infoMessageLogConsumer;
    @NonNull
    private final IMessageLogConsumer<String> debugMessageLogConsumer;
    @NonNull
    private final IMessageLogConsumer<String> warningMessageLogConsumer;
    @NonNull
    private final IMessageLogConsumer<JSONObject> jsonInfoMessageLogConsumer;

    public static final List<String> REGISTERED_LOGGER_CLASSES = Arrays.asList(
            YLoggerImpl.class.getName(),
            MessageLogConsumerProvider.class.getName(),
            SingleWarningMessageLogConsumer.class.getName(),
            MultilineMessageLogConsumer.class.getName(),
            SingleInfoMessageLogConsumer.class.getName(),
            ObjectLogConsumer.class.getName(),
            IMessageLogConsumer.class.getName()
    );

    public YLoggerImpl(@NonNull BaseLogger baseLogConsumer, boolean enabled) {
        this(baseLogConsumer, enabled, new MessageLogConsumerProvider(baseLogConsumer));
    }

    public void debug(@NonNull String tag, @NonNull String message, Object... args) {
        if (enabled) {
            debugMessageLogConsumer.consumeWithTag(tag, message, args);
        }
    }

    public void d(@NonNull String message, Object... args) {
        if (enabled) {
            debugMessageLogConsumer.consume(message, args);
        }
    }

    public void info(@NonNull String tag, @NonNull String message, Object... args) {
        if (enabled) {
            infoMessageLogConsumer.consumeWithTag(tag, message, args);
        }
    }

    public void i(@NonNull String message, Object... args) {
        if (enabled) {
            infoMessageLogConsumer.consume(message, args);
        }
    }

    public void warning(@NonNull String tag, @NonNull String message, Object... args) {
        if (enabled) {
            warningMessageLogConsumer.consumeWithTag(tag, message, args);
        }
    }

    public void w(@NonNull String message, Object... args) {
        if (enabled) {
            warningMessageLogConsumer.consume(message, args);
        }
    }

    public void e(@NonNull String msg, final Object... args) {
        if (enabled) {
            baseLogConsumer.fe(msg, args);
        }
    }

    public void error(@NonNull String tag, @NonNull String msg, Object... args) {
        if (enabled) {
            baseLogConsumer.fe(tag + msg, args);
        }
    }

    public void e(@NonNull Throwable e, @Nullable String msg, final Object... args) {
        if (enabled) {
            baseLogConsumer.fe(e, msg, args);
        }
    }

    public void error(@NonNull String tag, @Nullable Throwable e, @Nullable String msg, Object... args) {
        if (enabled) {
            baseLogConsumer.fe(e, tag + (msg == null ? "" : msg), args);
        }
    }

    public void dumpJson(@NonNull String tag, @NonNull JSONObject json) {
        if (enabled) {
            jsonInfoMessageLogConsumer.consumeWithTag(tag, json);
        }
    }

    @VisibleForTesting
    YLoggerImpl(@NonNull BaseLogger baseLogConsumer,
                boolean enabled,
                @NonNull MessageLogConsumerProvider messageLogConsumerProvider) {
        this.baseLogConsumer = baseLogConsumer;
        this.enabled = enabled;
        this.infoMessageLogConsumer = messageLogConsumerProvider.getInfoLogConsumer();
        this.debugMessageLogConsumer = messageLogConsumerProvider.getDebugLogConsumer();
        this.warningMessageLogConsumer = messageLogConsumerProvider.getWarningMessageLogConsumer();
        this.jsonInfoMessageLogConsumer = messageLogConsumerProvider.getJsonInfoLogConsumer();
    }
}
