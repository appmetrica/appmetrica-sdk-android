package io.appmetrica.analytics.coreutils.internal.logger;

import androidx.annotation.NonNull;
import java.util.List;

interface ILogMessageSplitter {
    List<String> split(@NonNull String input);
}
