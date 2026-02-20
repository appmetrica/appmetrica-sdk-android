package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonUtilsCollectionToJsonTest extends CommonTest {

    @Test
    public void setToJsonNull() {
        assertThat(JsonHelper.setToJsonNullEmptyWise(null)).isNull();
    }

    @Test
    public void setToJsonEmpty() throws JSONException {
        JSONAssert.assertEquals(
            new JSONArray(),
            JsonHelper.setToJsonNullEmptyWise(new HashSet<String>()),
            true
        );
    }

    @Test
    public void setToJsonFilled() throws JSONException {
        Set<String> set = new HashSet<String>();
        set.add("element 1");
        set.add("element 2");
        JSONAssert.assertEquals(
            new JSONArray().put("element 1").put("element 2"),
            JsonHelper.setToJsonNullEmptyWise(set),
            JSONCompareMode.NON_EXTENSIBLE
        );
    }

    @Test
    public void listToJsonNull() {
        assertThat(JsonHelper.listToJson(null)).isNull();
    }

    @Test
    public void listToJsonEmpty() {
        assertThat(JsonHelper.listToJson(new ArrayList<String>())).isNull();
    }

    @Test
    public void listToJsonStringNull() {
        assertThat(JsonHelper.listToJsonString(null)).isNull();
    }

    @Test
    public void listToJsonStringEmpty() {
        assertThat(JsonHelper.listToJsonString(new ArrayList<String>())).isNull();
    }

    @Test
    public void listToJsonNullEmptyWiseNull() {
        assertThat(JsonHelper.listToJsonNullEmptyWise(null)).isNull();
    }

    @Test
    public void listToJsonNullEmptyWiseEmpty() throws JSONException {
        JSONAssert.assertEquals(
            new JSONArray(),
            JsonHelper.listToJsonNullEmptyWise(new ArrayList<String>()),
            true
        );
    }

    @Test
    public void listToJsonFilled() throws JSONException {
        List<String> list = new ArrayList<String>();
        list.add("element 1");
        list.add("element 2");
        list.add("element 2");
        JSONArray expected = new JSONArray().put("element 1").put("element 2").put("element 2");
        JSONAssert.assertEquals(expected, JsonHelper.listToJson(list), true);
        JSONAssert.assertEquals(expected, JsonHelper.listToJsonNullEmptyWise(list), true);
        JSONAssert.assertEquals(expected.toString(), JsonHelper.listToJsonString(list), true);
    }

    @Test
    public void listToJsonDifferentTypes() throws JSONException {
        List<Object> list = new ArrayList<Object>();
        list.add("element 1");
        list.add("element 2");
        list.add(666);
        JSONArray expected = new JSONArray().put("element 1").put("element 2").put(666);
        JSONAssert.assertEquals(expected, JsonHelper.listToJson(list), true);
    }
}
