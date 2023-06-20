package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.util.Base64;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;
import io.appmetrica.analytics.testutils.CommonTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class PermanentConfigSerializerTest extends CommonTest {

    private final PermanentConfigSerializer serializer = new PermanentConfigSerializer();

    @Test
    public void serialization() throws IllegalAccessException, JSONException {
        String apikey = "apikey";
        String packFromCrashpad = "packFromCrashpad";
        int pid = 132121;
        String somepsid = "somepsid";

        ProcessConfiguration configuration = mock(ProcessConfiguration.class);

        doReturn(Integer.valueOf(pid)).when(configuration).getProcessID();
        doReturn(somepsid).when(configuration).getProcessSessionID();
        doReturn(packFromCrashpad).when(configuration).getPackageName();

        JSONObject jsonObject = new JSONObject().put(PermanentConfigSerializer.ARGUMENT_CLIENT_DESCRIPTION, new JSONObject()
                .put(PermanentConfigSerializer.ARGUMENT_API_KEY, apikey)
                .put(PermanentConfigSerializer.ARGUMENT_PACKAGE_NAME, packFromCrashpad)
                .put(PermanentConfigSerializer.ARGUMENT_PID, pid)
                .put(PermanentConfigSerializer.ARGUMENT_PSID, somepsid)
                .put(PermanentConfigSerializer.ARGUMENT_REPORTER_TYPE, "main")
        );

        ClientDescription description = serializer.deserialize(
                Base64.encodeToString(jsonObject.toString().getBytes(), 0)
        );

        assertThat(description).isNotNull();

        JSONObject answer = new JSONObject(new String(Base64.decode(serializer.serialize(apikey, configuration), 0)));

        JSONAssert.assertEquals(jsonObject, answer, true);
    }

    @Test
    public void fineCase() throws IllegalAccessException, JSONException {
        String apikey = "apikey";
        String packFromCrashpad = "packFromCrashpad";
        int pid = 132121;
        String somepsid = "somepsid";

        JSONObject jsonObject = new JSONObject().put(PermanentConfigSerializer.ARGUMENT_CLIENT_DESCRIPTION, new JSONObject()
                .put(PermanentConfigSerializer.ARGUMENT_API_KEY, apikey)
                .put(PermanentConfigSerializer.ARGUMENT_PACKAGE_NAME, packFromCrashpad)
                .put(PermanentConfigSerializer.ARGUMENT_PID, pid)
                .put(PermanentConfigSerializer.ARGUMENT_PSID, somepsid)
                .put(PermanentConfigSerializer.ARGUMENT_REPORTER_TYPE, "main")
        );

        ClientDescription description = serializer.deserialize(
                Base64.encodeToString(jsonObject.toString().getBytes(), 0)
        );

        assertThat(description).isNotNull();

        ObjectPropertyAssertions<ClientDescription> descAssertions = ObjectPropertyAssertions(description)
                .withPrivateFields(true);

        descAssertions.checkField("mApiKey", apikey);
        descAssertions.checkField("mPackageName", packFromCrashpad);
        descAssertions.checkField("mProcessID", pid);
        descAssertions.checkField("mProcessSessionID", somepsid);
        descAssertions.checkField("mReporterType", CounterConfigurationReporterType.MAIN);

        descAssertions.checkAll();
    }

    @Test
    public void pidAsString() throws IllegalAccessException, JSONException {
        String apikey = "apikey";
        String packFromCrashpad = "packFromCrashpad";
        String pid = "132121";
        String somepsid = "somepsid";

        JSONObject jsonObject = new JSONObject().put(PermanentConfigSerializer.ARGUMENT_CLIENT_DESCRIPTION, new JSONObject()
                .put(PermanentConfigSerializer.ARGUMENT_API_KEY, apikey)
                .put(PermanentConfigSerializer.ARGUMENT_PACKAGE_NAME, packFromCrashpad)
                .put(PermanentConfigSerializer.ARGUMENT_PID, pid)
                .put(PermanentConfigSerializer.ARGUMENT_PSID, somepsid)
                .put(PermanentConfigSerializer.ARGUMENT_REPORTER_TYPE, "main")
        );

        ClientDescription description = serializer.deserialize(
                Base64.encodeToString(jsonObject.toString().getBytes(), 0)
        );

        assertThat(description).isNotNull();

        ObjectPropertyAssertions<ClientDescription> descAssertions = ObjectPropertyAssertions(description)
                .withPrivateFields(true);

        descAssertions.checkField("mApiKey", apikey);
        descAssertions.checkField("mPackageName", packFromCrashpad);
        descAssertions.checkField("mProcessID", Integer.parseInt(pid));
        descAssertions.checkField("mProcessSessionID", somepsid);
        descAssertions.checkField("mReporterType", CounterConfigurationReporterType.MAIN);

        descAssertions.checkAll();
    }

    @Test
    public void badType() throws JSONException, IllegalAccessException {
        String apikey = "apikey";
        String packFromCrashpad = "packFromCrashpad";
        float pid = 132121f;
        String somepsid = "somepsid";

        JSONObject jsonObject = new JSONObject().put(PermanentConfigSerializer.ARGUMENT_CLIENT_DESCRIPTION, new JSONObject()
                .put(PermanentConfigSerializer.ARGUMENT_API_KEY, apikey)
                .put(PermanentConfigSerializer.ARGUMENT_PACKAGE_NAME, packFromCrashpad)
                .put(PermanentConfigSerializer.ARGUMENT_PID, pid)
                .put(PermanentConfigSerializer.ARGUMENT_PSID, somepsid)
                .put(PermanentConfigSerializer.ARGUMENT_REPORTER_TYPE, "main")
        );

        ClientDescription description = serializer.deserialize(
                Base64.encodeToString(jsonObject.toString().getBytes(), 0)
        );

        assertThat(description).isNotNull();

        ObjectPropertyAssertions<ClientDescription> descAssertions = ObjectPropertyAssertions(description)
                .withPrivateFields(true);

        descAssertions.checkField("mApiKey", apikey);
        descAssertions.checkField("mPackageName", packFromCrashpad);
        descAssertions.checkField("mProcessID", (int) pid);
        descAssertions.checkField("mProcessSessionID", somepsid);
        descAssertions.checkField("mReporterType", CounterConfigurationReporterType.MAIN);

        descAssertions.checkAll();
    }

}
