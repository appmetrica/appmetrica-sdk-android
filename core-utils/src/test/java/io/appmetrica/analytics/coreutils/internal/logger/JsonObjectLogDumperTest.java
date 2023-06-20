package io.appmetrica.analytics.coreutils.internal.logger;

import java.util.Arrays;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class JsonObjectLogDumperTest {

    private static final String JSON_INDENT_SPACES = "  ";

    private final JSONObject input;
    private final String expected;

    public JsonObjectLogDumperTest(JSONObject input, String expected) {
        this.input = input;
        this.expected = expected;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() throws JSONException {
        return Arrays.asList(new Object[][]{
                {firstJson(), FIRST_JSON_DUMP},
                {secondJson(), SECOND_JSON_DUMP},
                {thirdJson(), THIRD_JSON_DUMP}
        });
    }

    @Test
    public void dumpObject() {
        JsonObjectLogDumper jsonObjectLogDumper = new JsonObjectLogDumper();
        assertThat(jsonObjectLogDumper.dumpObject(input)).isEqualTo(expected);
    }

    private static final String FIRST_JSON_DUMP =
            "{" + "\n" +
                    JSON_INDENT_SPACES + "\"key\": \"value\"" + "\n" +
                    "}";

    private static final String SECOND_JSON_DUMP =
            "{" + "\n" +
                    JSON_INDENT_SPACES + "\"key\": {" + "\n" +
                    JSON_INDENT_SPACES + JSON_INDENT_SPACES + "\"key\": \"value\"" + "\n" +
                    JSON_INDENT_SPACES + "}" + "\n" +
                    "}";

    private static final String THIRD_JSON_DUMP =
            "{" + "\n" +
                    JSON_INDENT_SPACES + "\"key\": [" + "\n" +
                    JSON_INDENT_SPACES + JSON_INDENT_SPACES + "\"first\"," + "\n" +
                    JSON_INDENT_SPACES + JSON_INDENT_SPACES + "\"second\"" + "\n" +
                    JSON_INDENT_SPACES + "]" + "\n" +
                    "}";

    private static JSONObject firstJson() throws JSONException {
        return new JSONObject().put("key", "value");
    }

    private static JSONObject secondJson() throws JSONException {
        return new JSONObject().put("key", new JSONObject().put("key", "value"));
    }

    private static JSONObject thirdJson() throws JSONException {
        return new JSONObject().put("key", new JSONArray(new String[]{"first", "second"}));
    }
}
