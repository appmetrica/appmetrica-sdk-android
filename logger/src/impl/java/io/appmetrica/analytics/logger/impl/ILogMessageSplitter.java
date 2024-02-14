package io.appmetrica.analytics.logger.impl;

import androidx.annotation.NonNull;
import java.util.List;

interface ILogMessageSplitter {
    List<String> split(@NonNull String input);
}
