package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.util.Base64;
import io.appmetrica.analytics.testutils.CommonTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class RuntimeConfigStorageTest extends CommonTest {

    private final RuntimeConfigStorage storage = new RuntimeConfigStorage();

    @Test
    public void serializationFromInitialState() throws JSONException {
        JSONObject result = new JSONObject(new String(Base64.decode(storage.serialize(), Base64.DEFAULT)));
        assertThat(result.getString("arg_ee")).isEmpty();
        assertThat(result.has("arg_hv")).isFalse();
    }

    @Test
    public void serializationWithSomeValue() throws JSONException {
        String value = "someVal";
        storage.setErrorEnvironment(value);
        String version = "someVersion";
        storage.setHandlerVersion(version);
        JSONObject result = new JSONObject(new String(Base64.decode(storage.serialize(), Base64.DEFAULT)));
        assertThat(result.getString("arg_ee")).isEqualTo(value);
        assertThat(result.getString("arg_hv")).isEqualTo(version);
    }

}
