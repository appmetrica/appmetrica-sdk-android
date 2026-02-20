package io.appmetrica.analytics.impl.startup.parsing;

import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Arrays;
import java.util.Collection;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class CountryTest extends CommonTest {

    @Parameters(name = "Should be {1} for {0}")
    public static Collection<Object[]> data() throws JSONException {
        return Arrays.asList(new Object[][]{
            {new JSONObject().toString(), ""},
            {new JSONObject().put("locale", new JSONObject()).toString(), ""},
            {new JSONObject().put("locale", new JSONObject().put("country", new JSONObject())).toString(), ""},
            {new JSONObject().put("locale", new JSONObject().put("country", new JSONObject().put("reliable", false))).toString(), ""},
            {new JSONObject().put("locale", new JSONObject().put("country", new JSONObject().put("reliable", true))).toString(), ""},
            {new JSONObject().put("locale", new JSONObject().put("country", new JSONObject().put("reliable", true).put("value", "by"))).toString(), "by"},
        });
    }

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    private final JsonHelper.OptJSONObject mJson;
    private final String mExpected;
    private StartupParser mStartupParser;

    public CountryTest(String json, String expected) throws JSONException {
        mJson = new JsonHelper.OptJSONObject(json);
        mExpected = expected;
    }

    @Before
    public void setUp() {
        mStartupParser = new StartupParser();
    }

    @Test
    public void test() {
        StartupResult result = mStartupParser.parseStartupResponse(mJson.toString().getBytes());
        assertThat(result.getCountryInit()).isEqualTo(mExpected);
    }
}
