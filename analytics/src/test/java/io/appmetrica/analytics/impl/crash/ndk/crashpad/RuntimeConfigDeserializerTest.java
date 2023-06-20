package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.util.Base64;
import io.appmetrica.analytics.testutils.CommonTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class RuntimeConfigDeserializerTest extends CommonTest {

    private final RuntimeConfigDeserializer deserializer = new RuntimeConfigDeserializer();

    @Test
    public void fineCase() throws JSONException, IllegalAccessException {
        String errorEnv = "errorEnv";
        String version = "someVersion";
        RuntimeConfig config = deserializer.deserialize(Base64.encodeToString(
                new JSONObject().put("arg_ee", errorEnv).put("arg_hv", version).toString().getBytes(), Base64.DEFAULT
        ));
        ObjectPropertyAssertions(config)
                .checkField("errorEnvironment", errorEnv)
                .checkField("handlerVersion", version)
                .checkAll();
    }

    @Test
    public void emptyFields() throws JSONException, IllegalAccessException {
        RuntimeConfig config = deserializer.deserialize(Base64.encodeToString(
                new JSONObject().put("arg_ee", "").put("arg_hv", "").toString().getBytes(), Base64.DEFAULT
        ));
        ObjectPropertyAssertions(config)
                .checkField("errorEnvironment", (String) null)
                .checkField("handlerVersion", (String) null)
                .checkAll();
    }

    @Test
    public void noFields() throws JSONException, IllegalAccessException {
        RuntimeConfig config = deserializer.deserialize(Base64.encodeToString(
                new JSONObject().toString().getBytes(), Base64.DEFAULT
        ));
        ObjectPropertyAssertions(config)
                .checkField("errorEnvironment", null)
                .checkField("handlerVersion", null)
                .checkAll();
    }

    @Test
    public void notBase64() throws JSONException {
        RuntimeConfig config = deserializer.deserialize("!@$#%^&*^$%#@");
        assertThat(config.errorEnvironment).isNull();
    }
}
