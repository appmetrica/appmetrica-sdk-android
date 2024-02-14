package io.appmetrica.analytics.logger.impl;

import androidx.annotation.NonNull;
import org.json.JSONObject;

class MessageLogConsumerProvider {

    @NonNull
    private final IMessageLogConsumer<String> infoMessageLogConsumer;
    @NonNull
    private final IMessageLogConsumer<String> warningMessageLogConsumer;
    @NonNull
    private final IMessageLogConsumer<JSONObject> jsonInfoLogConsumer;

    public MessageLogConsumerProvider(@NonNull BaseLogger baseLogger) {
        IMessageLogConsumer<String> singleInfoMessageLogConsumer = new SingleInfoMessageLogConsumer(baseLogger);
        IMessageLogConsumer<String> singleWarningMessageLogConsumer =
                new SingleWarningMessageLogConsumer(baseLogger);
        ILogMessageSplitter splitter = new LogMessageByLineLimitSplitter();

        infoMessageLogConsumer = new MultilineMessageLogConsumer(singleInfoMessageLogConsumer, splitter);
        warningMessageLogConsumer = new MultilineMessageLogConsumer(singleWarningMessageLogConsumer, splitter);
        jsonInfoLogConsumer = new ObjectLogConsumer<JSONObject>(
                new MultilineMessageLogConsumer(singleInfoMessageLogConsumer, new LogMessageByLineBreakSplitter()),
                new JsonObjectLogDumper()
        );
    }

    @NonNull
    public IMessageLogConsumer<String> getDebugLogConsumer() {
        return infoMessageLogConsumer;
    }

    @NonNull
    public IMessageLogConsumer<String> getInfoLogConsumer() {
        return infoMessageLogConsumer;
    }

    @NonNull
    public IMessageLogConsumer<String> getWarningMessageLogConsumer() {
        return warningMessageLogConsumer;
    }

    @NonNull
    public IMessageLogConsumer<JSONObject> getJsonInfoLogConsumer() {
        return jsonInfoLogConsumer;
    }
}
