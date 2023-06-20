package io.appmetrica.analytics.impl.startup.parsing;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.db.state.converter.CacheControlConverter;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.CacheControl;
import io.appmetrica.analytics.testutils.CommonTest;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CacheControlParserTest extends CommonTest {

    @Mock
    private CacheControlConverter mConverter;
    @Mock
    private CacheControl mCacheControl;
    @Captor
    private ArgumentCaptor<StartupStateProtobuf.StartupState.CacheControl> mProtoCaptor;

    private Preparer mPreparer;
    private CacheControlParser mCacheControlParser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mPreparer = new Preparer();

        when(mConverter.toModel(any(StartupStateProtobuf.StartupState.CacheControl.class)))
                .thenReturn(mCacheControl);

        mCacheControlParser = new CacheControlParser(mConverter);
    }

    @Test
    public void testDefaultConstructor() {
        assertThat(new CacheControlParser().getConverter()).isNotNull();
    }

    @Test
    public void testParseFromStartup() {
        assertThat(mCacheControlParser.parseFromJson(mPreparer.getJson())).isEqualTo(mCacheControl);
    }

    @Test
    public void testParseToProto() throws Exception {
        StartupStateProtobuf.StartupState.CacheControl proto = parseAndInterceptProtoConfig();

        ObjectPropertyAssertions<StartupStateProtobuf.StartupState.CacheControl> assertions =
                ObjectPropertyAssertions(proto)
                .withFinalFieldOnly(false);

        assertions.checkField("lastKnownLocationTtl", 40000L);

        assertions.checkAll();
    }

    @Test
    public void testParseToProtoForMissingLastKnownLocationTtl() throws Exception {
        mPreparer.removeLastKnownLocationTtl();
        assertThat(parseAndInterceptProtoConfig().lastKnownLocationTtl).isEqualTo(10000L);
    }

    @Test
    public void testParseToProtoIfMissingBlock() throws Exception {
        mPreparer.clear();
        assertInterceptedProtoContainsDefaultValues();
    }

    @Test
    public void testParseToProtoForEmptyBlock() throws Exception {
        mPreparer.clearConfig();
        assertInterceptedProtoContainsDefaultValues();
    }

    private void assertInterceptedProtoContainsDefaultValues() throws Exception {
        StartupStateProtobuf.StartupState.CacheControl proto = parseAndInterceptProtoConfig();

        ObjectPropertyAssertions<StartupStateProtobuf.StartupState.CacheControl> assertions =
                ObjectPropertyAssertions(proto)
                        .withFinalFieldOnly(false);

        assertions.checkField("lastKnownLocationTtl", 10000L);

        assertions.checkAll();
    }

    private StartupStateProtobuf.StartupState.CacheControl parseAndInterceptProtoConfig() {
        mCacheControlParser.parseFromJson(mPreparer.getJson());
        verify(mConverter).toModel(mProtoCaptor.capture());
        return mProtoCaptor.getValue();
    }

    private static class Preparer {

        private static final String INITIAL_VALUE_STRING =
                "{" +
                        "\"cache_control\":{" +
                        "\"last_known_location_ttl\":40" +
                        "}" +
                        "}";

        private JSONObject mRootJson;

        public Preparer() throws JSONException {
            mRootJson = new JSONObject(INITIAL_VALUE_STRING);
        }

        void removeLastKnownLocationTtl() throws JSONException {
            getOrCreateCacheControlJson().remove("last_known_location_ttl");
        }

        void clearConfig() throws JSONException {
            mRootJson.put("cache_control", new JSONObject());
        }

        void clear() throws Exception {
            mRootJson = new JSONObject();
        }

        JSONObject getOrCreateCacheControlJson() throws JSONException {
            JSONObject configJson = mRootJson.optJSONObject("cache_control");
            if (configJson == null) {
                configJson = new JSONObject();
                mRootJson.put("cache_control", configJson);
            }
            return configJson;
        }

        JSONObject getJson() {
            return mRootJson;
        }
    }
}
