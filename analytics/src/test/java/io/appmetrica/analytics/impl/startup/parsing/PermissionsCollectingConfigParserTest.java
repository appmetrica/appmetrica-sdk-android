package io.appmetrica.analytics.impl.startup.parsing;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.PermissionsCollectingConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class PermissionsCollectingConfigParserTest extends CommonTest {

    private PermissionsCollectingConfigParser mParser = new PermissionsCollectingConfigParser();
    private StartupResult mResult = new StartupResult();

    @Test
    public void testConfigNotParsedIfNoFeature() throws JSONException {
        final StartupJsonMock response = new StartupJsonMock();
        mParser.parseIfEnabled(mResult, response);
        assertThat(mResult.getPermissionsCollectingConfig()).isNull();
    }

    @Test
    public void testConfigNotParsedIfFeatureDisabled() throws JSONException {
        final StartupJsonMock response = new StartupJsonMock();
        setPermissionsCollectingEnabled(false);
        mParser.parseIfEnabled(mResult, response);
        assertThat(mResult.getPermissionsCollectingConfig()).isNull();
    }

    @Test
    public void testConfigParsedIfFeatureEnabled() throws Exception {
        final long checkIntervalSeconds = 44;
        final long forceSendIntervalSeconds = 77;
        final StartupJsonMock response = new StartupJsonMock();
        setPermissionsCollectingEnabled(true);
        final JSONObject config = new JSONObject();
        config.put(JsonResponseKey.CHECK_INTERVAL_SECONDS, checkIntervalSeconds);
        config.put(JsonResponseKey.FORCE_SEND_INTERVAL_SECONDS, forceSendIntervalSeconds);
        response.put(JsonResponseKey.PERMISSIONS_COLLECTING_CONFIG, config);
        mParser.parseIfEnabled(mResult, response);

        ObjectPropertyAssertions<PermissionsCollectingConfig> assertions =
                ObjectPropertyAssertions(mResult.getPermissionsCollectingConfig())
                        .withFinalFieldOnly(false);

        assertions.checkField("mCheckIntervalSeconds", checkIntervalSeconds);
        assertions.checkField("mForceSendIntervalSeconds", forceSendIntervalSeconds);
        assertions.checkAll();
    }

    @Test
    public void testNoBlock() throws Exception {
        final StartupJsonMock response = new StartupJsonMock();
        setPermissionsCollectingEnabled(true);
        mParser.parseIfEnabled(mResult, response);

        ObjectPropertyAssertions<PermissionsCollectingConfig> assertions =
                ObjectPropertyAssertions(mResult.getPermissionsCollectingConfig())
                        .withFinalFieldOnly(false);

        assertions.checkField("mCheckIntervalSeconds", TimeUnit.DAYS.toSeconds(1));
        assertions.checkField("mForceSendIntervalSeconds", TimeUnit.DAYS.toSeconds(5));
        assertions.checkAll();
    }

    @Test
    public void testEmptyBlock() throws Exception {
        final StartupJsonMock response = new StartupJsonMock();
        setPermissionsCollectingEnabled(true);
        response.put(JsonResponseKey.PERMISSIONS_COLLECTING_CONFIG, new JSONObject());
        mParser.parseIfEnabled(mResult, response);
        ObjectPropertyAssertions<PermissionsCollectingConfig> assertions =
                ObjectPropertyAssertions(mResult.getPermissionsCollectingConfig())
                        .withFinalFieldOnly(false);

        assertions.checkField("mCheckIntervalSeconds", TimeUnit.DAYS.toSeconds(1));
        assertions.checkField("mForceSendIntervalSeconds", TimeUnit.DAYS.toSeconds(5));
        assertions.checkAll();
    }

    private void setPermissionsCollectingEnabled(final boolean enabled) {
        mResult.setCollectingFlags(new CollectingFlags.CollectingFlagsBuilder().withPermissionsCollectingEnabled(enabled).build());
    }
}
