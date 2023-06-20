package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.os.Bundle;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class CrashpadCrashParserTest extends CommonTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private ClientDescription description;
    @Mock
    private PermanentConfigSerializer serializer;
    @Mock
    private RuntimeConfigDeserializer deserializer;
    @InjectMocks
    private CrashpadCrashParser parser;

    @Before
    public void setUp() {
        doReturn(description).when(serializer).deserialize(any(String.class));
    }

    @Test
    public void noUuid() throws IllegalAccessException {
        String crashUuid = "crashUuid";

        Bundle data = new Bundle();

        String fileName = "fileName";
        long creationTime = 21213213L;

        data.putString("arg_df", fileName);
        data.putLong("arg_ct", creationTime);

        data.putString(CrashpadConstants.ARGUMENT_CLIENT_DESCRIPTION, "description");

        CrashpadCrash crash = parser.apply(crashUuid, data);

        assertThat(crash).isNotNull();

        ObjectPropertyAssertions<CrashpadCrashReport> reportAssertions = ObjectPropertyAssertions(crash.crashReport);

        reportAssertions.checkField("uuid", crashUuid);
        reportAssertions.checkField("dumpFile", fileName);
        reportAssertions.checkField("creationTime", creationTime);

        reportAssertions.checkAll();
    }

    @Test
    public void fineCase() throws IllegalAccessException {
        String crashUuid = "crashUuid";

        String runtimeConfig = "runtimeConfig";

        Bundle data = new Bundle();

        String fileName = "fileName";
        long creationTime = 21213213L;

        data.putString("arg_df", fileName);
        data.putLong("arg_ct", creationTime);

        data.putString(CrashpadConstants.ARGUMENT_CLIENT_DESCRIPTION, "description");
        data.putString(CrashpadConstants.ARGUMENT_RUNTIME_CONFIG, runtimeConfig);

        RuntimeConfig config = mock(RuntimeConfig.class);

        doReturn(config).when(deserializer).deserialize(runtimeConfig);

        CrashpadCrash crash = parser.apply(crashUuid, data);

        assertThat(crash).isNotNull();
        assertThat(crash.runtimeConfig).isSameAs(config);

        ObjectPropertyAssertions<CrashpadCrashReport> reportAssertions = ObjectPropertyAssertions(crash.crashReport);

        reportAssertions.checkField("uuid", crashUuid);
        reportAssertions.checkField("dumpFile", fileName);
        reportAssertions.checkField("creationTime", creationTime);

        reportAssertions.checkAll();
    }
}
