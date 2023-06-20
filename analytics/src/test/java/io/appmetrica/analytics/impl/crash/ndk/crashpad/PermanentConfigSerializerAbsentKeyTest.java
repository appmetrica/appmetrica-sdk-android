package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.util.Base64;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class PermanentConfigSerializerAbsentKeyTest extends CommonTest {

    @NonNull
    private final String absentKey;

    @ParameterizedRobolectricTestRunner.Parameters(name = "absentKey {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { PermanentConfigSerializer.ARGUMENT_API_KEY },
                { PermanentConfigSerializer.ARGUMENT_PACKAGE_NAME },
                { PermanentConfigSerializer.ARGUMENT_PID },
                { PermanentConfigSerializer.ARGUMENT_PSID },
                { PermanentConfigSerializer.ARGUMENT_REPORTER_TYPE }
        });
    }

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @NonNull
    private PermanentConfigSerializer parser = new PermanentConfigSerializer();

    public PermanentConfigSerializerAbsentKeyTest(@NonNull String absentKey) {
        this.absentKey = absentKey;
    }

    @Test
    public void testAbsentKeyForCurrentSession() throws JSONException {
        String apikey = "apikey";
        String packFromCrashpad = "packFromCrashpad";
        int pid = 132121;
        String somepsid = "somepsid";

        JSONObject config = new JSONObject();

        JSONObject data = new JSONObject()
                .put(PermanentConfigSerializer.ARGUMENT_API_KEY, apikey)
                .put(PermanentConfigSerializer.ARGUMENT_PID, pid)
                .put(PermanentConfigSerializer.ARGUMENT_PACKAGE_NAME, packFromCrashpad)
                .put(PermanentConfigSerializer.ARGUMENT_PSID, somepsid)
                .put(PermanentConfigSerializer.ARGUMENT_REPORTER_TYPE, "main");

        data.remove(absentKey);

        config.put(PermanentConfigSerializer.ARGUMENT_CLIENT_DESCRIPTION, data);

        assertThat(parser.deserialize(Base64.encodeToString(data.toString().getBytes(), 0))).isNull();
    }

}
