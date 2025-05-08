package io.appmetrica.analytics.impl.startup.parsing;

import io.appmetrica.analytics.testutils.CommonTest;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class FeaturesParserTest extends CommonTest {

    private StartupJsonMock mStartupJsonMock;
    private final StartupResult mStartupResult = new StartupResult();
    private final FeaturesParser mFeaturesParser = new FeaturesParser();

    @Before
    public void setUp() throws Exception {
        mStartupJsonMock = new StartupJsonMock();
    }

    @Test
    public void testPermissionsEnabled() throws JSONException {
        mStartupJsonMock.addPermissionsCollectingEnabled(true);

        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().permissionsCollectingEnabled).isTrue();
    }

    @Test
    public void testPermissionsDisabled() throws JSONException {
        mStartupJsonMock.addPermissionsCollectingEnabled(false);
        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().permissionsCollectingEnabled).isFalse();
    }

    @Test
    public void testPermissionsEmpty() throws JSONException {
        mStartupJsonMock.addEmptyFeaturesList();
        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().permissionsCollectingEnabled).isFalse();
    }

    @Test
    public void testFeaturesEnabled() throws JSONException {
        mStartupJsonMock.addFeaturesCollecting(true);

        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().featuresCollectingEnabled).isTrue();
    }

    @Test
    public void testFeaturesDisabled() throws JSONException {
        mStartupJsonMock.addFeaturesCollecting(false);

        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().featuresCollectingEnabled).isFalse();
    }

    @Test
    public void testFeaturesEmpty() throws JSONException {
        mStartupJsonMock.addEmptyFeaturesList();
        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().featuresCollectingEnabled).isFalse();
    }

    @Test
    public void testGoogleAidEnabled() throws JSONException {
        mStartupJsonMock.addGoogleAid(true);

        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);

        assertThat(mStartupResult.getCollectionFlags().googleAid).isTrue();
    }

    @Test
    public void testGoogleAidDisabled() throws JSONException {
        mStartupJsonMock.addGoogleAid(false);

        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);

        assertThat(mStartupResult.getCollectionFlags().googleAid).isFalse();
    }

    @Test
    public void testGoogleAidEmpty() throws JSONException {
        mStartupJsonMock.addEmptyFeaturesList();
        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);

        assertThat(mStartupResult.getCollectionFlags().googleAid).isFalse();
    }

    @Test
    public void testSimInfoEnabled() throws JSONException {
        mStartupJsonMock.addSimInfo(true);

        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);

        assertThat(mStartupResult.getCollectionFlags().simInfo).isTrue();
    }

    @Test
    public void testSimInfoDisabled() throws JSONException {
        mStartupJsonMock.addSimInfo(false);

        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);

        assertThat(mStartupResult.getCollectionFlags().simInfo).isFalse();
    }

    @Test
    public void testSimInfoEmpty() throws JSONException {
        mStartupJsonMock.addEmptyFeaturesList();
        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().simInfo).isFalse();
    }

    @Test
    public void testHuaweiOaidEnabled() throws JSONException {
        mStartupJsonMock.addHuaweiOaid(true);
        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().huaweiOaid).isTrue();
    }

    @Test
    public void testHuaweiOaidDisabled() throws JSONException {
        mStartupJsonMock.addHuaweiOaid(false);
        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().huaweiOaid).isFalse();
    }

    @Test
    public void testHuaweiOaidEmpty() throws JSONException {
        mStartupJsonMock.addEmptyFeaturesList();
        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().huaweiOaid).isFalse();
    }

    @Test
    public void testSslPinningEnabled() throws JSONException {
        mStartupJsonMock.addSslPinning(true);
        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().sslPinning).isTrue();
    }

    @Test
    public void testSslPinningDisabled() throws JSONException {
        mStartupJsonMock.addSslPinning(false);
        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().sslPinning).isFalse();
    }

    @Test
    public void testHSslPinningEmpty() throws JSONException {
        mStartupJsonMock.addEmptyFeaturesList();
        mFeaturesParser.parse(mStartupResult, mStartupJsonMock);
        assertThat(mStartupResult.getCollectionFlags().sslPinning).isNull();
    }

}
