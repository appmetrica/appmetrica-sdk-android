package io.appmetrica.analytics.impl.startup.parsing;

import android.util.Pair;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.startup.AttributionConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class AttributionConfigParserTest extends CommonTest {

    @Mock
    private StartupResult result;
    @Captor
    private ArgumentCaptor<AttributionConfig> configCaptor;
    private final AttributionConfigParser parser = new AttributionConfigParser();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void noAttributionBlock() {
        parser.parse(result, new JSONObject());
        verifyZeroInteractions(result);
    }

    @Test
    public void noDeeplinkConditionsBlock() throws Exception {
        JSONObject response = new JSONObject().put(AttributionConfigParser.KEY_ATTRIBUTION, new JSONObject());
        testParse(response, new ArrayList<Pair<String, AttributionConfig.Filter>>());
    }

    @Test
    public void emptyDeeplinkConditions() throws Exception {
        JSONObject response = new JSONObject().put(
                AttributionConfigParser.KEY_ATTRIBUTION, new JSONObject()
                        .put(AttributionConfigParser.KEY_DEEPLINK_CONDITIONS, new JSONArray())
        );
        testParse(response, new ArrayList<Pair<String, AttributionConfig.Filter>>());
    }

    @Test
    public void hasOneCondition() throws Exception {
        JSONObject response = new JSONObject().put(
                AttributionConfigParser.KEY_ATTRIBUTION, new JSONObject()
                        .put(AttributionConfigParser.KEY_DEEPLINK_CONDITIONS, new JSONArray()
                                .put(new JSONObject()
                                        .put(AttributionConfigParser.KEY_KEY, "some key")
                                        .put(AttributionConfigParser.KEY_VALUE, "some value")
                                )
                        )
        );
        testParse(response, Collections.singletonList(
                new Pair<String, AttributionConfig.Filter>("some key", new AttributionConfig.Filter("some value"))
        ));
    }

    @Test
    public void hasMultipleConditions() throws Exception {
        JSONObject response = new JSONObject().put(
                AttributionConfigParser.KEY_ATTRIBUTION, new JSONObject()
                        .put(AttributionConfigParser.KEY_DEEPLINK_CONDITIONS, new JSONArray()
                                .put(new JSONObject()
                                        .put(AttributionConfigParser.KEY_KEY, "some key 1")
                                        .put(AttributionConfigParser.KEY_VALUE, "some value 1")
                                ).put(new JSONObject()
                                        .put(AttributionConfigParser.KEY_KEY, "some key 2")
                                        .put(AttributionConfigParser.KEY_VALUE, "some value 2")
                                )
                        )
        );
        testParse(response, Arrays.asList(
                new Pair<String, AttributionConfig.Filter>("some key 1", new AttributionConfig.Filter("some value 1")),
                new Pair<String, AttributionConfig.Filter>("some key 2", new AttributionConfig.Filter("some value 2"))
        ));
    }

    @Test
    public void hasNullKey() throws Exception {
        JSONObject response = new JSONObject().put(
                AttributionConfigParser.KEY_ATTRIBUTION, new JSONObject()
                        .put(AttributionConfigParser.KEY_DEEPLINK_CONDITIONS, new JSONArray()
                                .put(new JSONObject()
                                        .put(AttributionConfigParser.KEY_KEY, null)
                                        .put(AttributionConfigParser.KEY_VALUE, "some value 1")
                                ).put(new JSONObject()
                                        .put(AttributionConfigParser.KEY_KEY, "some key 2")
                                        .put(AttributionConfigParser.KEY_VALUE, "some value 2")
                                )
                        )
        );
        testParse(response, Arrays.asList(
                new Pair<String, AttributionConfig.Filter>("some key 2", new AttributionConfig.Filter("some value 2"))
        ));
    }

    @Test
    public void hasEmptyKey() throws Exception {
        JSONObject response = new JSONObject().put(
                AttributionConfigParser.KEY_ATTRIBUTION, new JSONObject()
                        .put(AttributionConfigParser.KEY_DEEPLINK_CONDITIONS, new JSONArray()
                                .put(new JSONObject()
                                        .put(AttributionConfigParser.KEY_KEY, "")
                                        .put(AttributionConfigParser.KEY_VALUE, "some value 1")
                                ).put(new JSONObject()
                                        .put(AttributionConfigParser.KEY_KEY, "some key 2")
                                        .put(AttributionConfigParser.KEY_VALUE, "some value 2")
                                )
                        )
        );
        testParse(response, Arrays.asList(
                new Pair<String, AttributionConfig.Filter>("some key 2", new AttributionConfig.Filter("some value 2"))
        ));
    }

    @Test
    public void hasNullValue() throws Exception {
        JSONObject response = new JSONObject().put(
                AttributionConfigParser.KEY_ATTRIBUTION, new JSONObject()
                        .put(AttributionConfigParser.KEY_DEEPLINK_CONDITIONS, new JSONArray()
                                .put(new JSONObject()
                                        .put(AttributionConfigParser.KEY_KEY, "some key 1")
                                ).put(new JSONObject()
                                        .put(AttributionConfigParser.KEY_KEY, "some key 2")
                                        .put(AttributionConfigParser.KEY_VALUE, "some value 2")
                                )
                        )
        );
        testParse(response, Arrays.asList(
                new Pair<String, AttributionConfig.Filter>("some key 1", null),
                new Pair<String, AttributionConfig.Filter>("some key 2", new AttributionConfig.Filter("some value 2"))
        ));
    }

    @Test
    public void attributionIsNotJsonObject() throws JSONException {
        JSONObject response = new JSONObject().put(AttributionConfigParser.KEY_ATTRIBUTION, "some string");
        parser.parse(result, response);
        verifyZeroInteractions(result);
    }

    private void testParse(@NonNull JSONObject response, @NonNull List<Pair<String, AttributionConfig.Filter>> expectedConditions) throws IllegalAccessException {
        parser.parse(result, response);
        verify(result).setAttributionConfig(configCaptor.capture());
        ObjectPropertyAssertions(configCaptor.getValue())
                .checkField("deeplinkConditions", expectedConditions, true)
                .checkAll();
    }

}
