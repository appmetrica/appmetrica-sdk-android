package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class StartupUtilsEncodeClidsTest extends CommonTest {

    @Test
    public void encodeNullClids() {
        assertThat(StartupUtils.encodeClids(null)).isEqualTo("");
    }

    @Test
    public void encodeEmptyClids() {
        assertThat(StartupUtils.encodeClids(new HashMap<String, String>())).isEqualTo("");
    }

    @Test
    public void encodeSingleClid() {
        assertThat(StartupUtils.encodeClids(Collections.singletonMap("clid0", "0"))).isEqualTo("clid0:0");
    }

    @Test
    public void encodeSingleClidWithEmptyKey() {
        assertThat(StartupUtils.encodeClids(Collections.singletonMap("", "0"))).isEqualTo(":0");
    }

    @Test
    public void encodeSingleClidWithInvalidKey() {
        assertThat(StartupUtils.encodeClids(Collections.singletonMap("bad_key", "bad_value"))).isEqualTo("bad_key:bad_value");
    }

    @Test
    public void encodeMultipleClids() {
        Map<String, String> clidsMap = TestUtils.mapOf(
                TestUtils.pair("clid0", "0"),
                TestUtils.pair("clid1", "1")
        );
        String encodedClids = StartupUtils.encodeClids(clidsMap);
        assertThat(StartupUtils.decodeClids(encodedClids)).containsOnly(clidsMap.entrySet().toArray(new Map.Entry[0]));
    }

    @Test
    public void encodeMultipleClidsWithEmptyKey() {
        Map<String, String> clidsMap = TestUtils.mapOf(
                TestUtils.pair("clid0", "0"),
                TestUtils.pair("", "2"),
                TestUtils.pair("clid1", "1")
        );
        String encodedClids = StartupUtils.encodeClids(clidsMap);
        assertThat(StartupUtils.decodeClids(encodedClids)).containsOnly(clidsMap.entrySet().toArray(new Map.Entry[0]));
    }

    @Test
    public void decodeNullString() {
        assertThat(StartupUtils.decodeClids(null)).isNotNull().isEmpty();
    }

    @Test
    public void decodeEmptyString() {
        assertThat(StartupUtils.decodeClids("")).isNotNull().isEmpty();
    }

    @Test
    public void decodeStringWithSingleClid() {
        assertThat(StartupUtils.decodeClids("clid0:0")).containsOnly(
                new AbstractMap.SimpleEntry<String, String>("clid0", "0")
        );
    }

    @Test
    public void decodeStringWithSingleClidWithEmptyKey() {
        assertThat(StartupUtils.decodeClids(":0")).containsOnly(
                new AbstractMap.SimpleEntry<String, String>("", "0")
        );
    }

    @Test
    public void decodeStringWithSingleClidWithInvalidKey() {
        assertThat(StartupUtils.decodeClids("bad_key:bad_value")).containsOnly(
                new AbstractMap.SimpleEntry<String, String>("bad_key", "bad_value")
        );
    }

    @Test
    public void decodeStringWithMultipleClids() {
        assertThat(StartupUtils.decodeClids("clid0:0,clid1:1")).containsOnly(
                new AbstractMap.SimpleEntry<String, String>("clid0", "0"),
                new AbstractMap.SimpleEntry<String, String>("clid1", "1")
        );
    }

    @Test
    public void decodeStringWithMultipleClidsWithEmptyKey() {
        assertThat(StartupUtils.decodeClids("clid0:0,:2,clid1:1")).containsOnly(
                new AbstractMap.SimpleEntry<String, String>("clid0", "0"),
                new AbstractMap.SimpleEntry<String, String>("clid1", "1"),
                new AbstractMap.SimpleEntry<String, String>("", "2")
        );
    }

}
