package io.appmetrica.analytics.impl.component;

import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.request.StartupArgumentsTest;

public class CommonArgumentsTestUtils {

    public static CommonArguments createMockedArguments() {
        return new CommonArguments(
                StartupArgumentsTest.empty(),
                emptyReporterArguments(),
                null
        );
    }

    public static CommonArguments createMockedArguments(@NonNull ResultReceiver resultReceiver) {
        return new CommonArguments(
                StartupArgumentsTest.empty(),
                emptyReporterArguments(),
                resultReceiver
        );
    }

    public static CommonArguments.ReporterArguments emptyReporterArguments() {
        return new CommonArguments.ReporterArguments();
    }
}
