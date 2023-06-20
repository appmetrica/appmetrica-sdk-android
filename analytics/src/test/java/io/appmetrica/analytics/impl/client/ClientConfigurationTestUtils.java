package io.appmetrica.analytics.impl.client;

import android.os.ResultReceiver;
import io.appmetrica.analytics.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import org.robolectric.RuntimeEnvironment;

public class ClientConfigurationTestUtils extends CommonTest {

    public static ClientConfiguration createStubbedConfiguration() {
        return createStubbedConfiguration(null);
    }

    public static ClientConfiguration createStubbedConfiguration(final ResultReceiver resultReceiver) {
        return new ClientConfiguration(new ProcessConfiguration(RuntimeEnvironment.getApplication(), resultReceiver), new CounterConfiguration());
    }

}
