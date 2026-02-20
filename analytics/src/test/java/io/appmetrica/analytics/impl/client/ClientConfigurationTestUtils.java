package io.appmetrica.analytics.impl.client;

import android.content.Context;
import android.os.ResultReceiver;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;

public class ClientConfigurationTestUtils extends CommonTest {

    public static ClientConfiguration createStubbedConfiguration(Context context, final ResultReceiver resultReceiver) {
        return new ClientConfiguration(new ProcessConfiguration(context, resultReceiver), new CounterConfiguration());
    }
}
