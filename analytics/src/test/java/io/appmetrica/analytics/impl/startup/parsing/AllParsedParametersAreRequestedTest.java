package io.appmetrica.analytics.impl.startup.parsing;

import android.net.Uri;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.db.state.converter.StatSendingConverter;
import io.appmetrica.analytics.impl.modules.ModulesRemoteConfigArgumentsCollector;
import io.appmetrica.analytics.impl.referrer.service.ReferrerHolder;
import io.appmetrica.analytics.impl.request.Obfuscator;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.request.UrlParts;
import io.appmetrica.analytics.impl.request.appenders.StartupParamsAppender;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AllParsedParametersAreRequestedTest extends CommonTest {

    @Mock
    private Obfuscator obfuscator;
    @Mock
    private StartupRequestConfig startupRequestConfig;
    @Mock
    private ReferrerHolder referrerHolder;
    @Mock
    private JsonResponseProvider jsonResponseProvider;
    @Mock
    private ModulesRemoteConfigArgumentsCollector modulesArgumentsCollector;
    private final GetParametersManager getParametersManager = new GetParametersManager();

    private StartupParamsAppender startupParamsAppender;

    @Rule
    public final GlobalServiceLocatorRule rule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(0);
            }
        }).when(obfuscator).obfuscate(anyString());
        when(startupRequestConfig.getChosenClids()).thenReturn(new ClidsInfo.Candidate(Collections.singletonMap("clid0", "0"), DistributionSource.APP));
        when(startupRequestConfig.getReferrerHolder()).thenReturn(referrerHolder);
        AdvertisingIdsHolder advertisingIdsHolder = mock(AdvertisingIdsHolder.class);
        when(advertisingIdsHolder.getGoogle()).thenReturn(new AdTrackingInfoResult());
        when(advertisingIdsHolder.getHuawei()).thenReturn(new AdTrackingInfoResult());
        when(advertisingIdsHolder.getYandex()).thenReturn(new AdTrackingInfoResult());
        when(startupRequestConfig.getAdvertisingIdsHolder()).thenReturn(advertisingIdsHolder);
        when(startupRequestConfig.getDeviceId()).thenReturn("");
        startupParamsAppender = new StartupParamsAppender(obfuscator, modulesArgumentsCollector);
    }

    @Test
    public void featuresAreTheSame() throws JSONException {
        String[] requestedFeatures = getRequestedFeatures();
        List<String> parsedFeatures = getParsedFeatures();
        assertThat(requestedFeatures)
                .doesNotHaveDuplicates()
                .containsOnlyElementsOf(parsedFeatures);
    }

    @Test
    public void blocksAreTheSame() throws Exception {
        Iterable<String> requestedBlocks = getRequestedBlocks();
        List<String> parsedBlocks = getParsedBlocks();
        List<String> transformedToParametersBlocks = getParametersManager.transformToParameters(parsedBlocks);
        assertThat(requestedBlocks).containsExactlyInAnyOrderElementsOf(transformedToParametersBlocks);
    }

    private String[] getRequestedFeatures() {
        Uri.Builder builder = new Uri.Builder();
        startupParamsAppender.appendParams(builder, startupRequestConfig);
        return builder.build().getQueryParameter(UrlParts.FEATURES).split(",");
    }

    private Iterable<String> getRequestedBlocks() {
        Uri.Builder builder = new Uri.Builder();
        startupParamsAppender.appendParams(builder, startupRequestConfig);
        return getParametersManager.getBlocks(builder.build());
    }

    private List<String> getParsedFeatures() throws JSONException {
        JsonHelper.OptJSONObject response = mock(JsonHelper.OptJSONObject.class);
        enableAllFeatures(response);
        FeaturesParser featuresParser = new FeaturesParser();
        featuresParser.parse(mock(StartupResult.class), response);
        ArgumentCaptor<String> parsedKeysCaptor = ArgumentCaptor.forClass(String.class);
        JSONObject featuresList = ((JSONObject) response.get(JsonResponseKey.FEATURES, new JSONObject())).optJSONObject(JsonResponseKey.LIST);
        verify(featuresList, atLeastOnce()).getJSONObject(parsedKeysCaptor.capture());
        return parsedKeysCaptor.getAllValues();
    }

    private List<String> getParsedBlocks() throws Exception {
        RememberingJson response = new RememberingJson();
        JSONObject featuresList = new JSONObject();
        for (String feature : getRequestedFeatures()) {
            featuresList.put(feature, new JSONObject().put(JsonResponseKey.FEATURE_ENABLED, true));
        }
        response.put(JsonResponseKey.FEATURES, new JSONObject().put(JsonResponseKey.LIST, featuresList));
        when(jsonResponseProvider.jsonFromBytes(any(byte[].class))).thenReturn(response);

        StartupParser startupParser = new StartupParser(
                jsonResponseProvider,
                new HostsParser(),
                new FeaturesParser(),
                new RetryPolicyConfigParser(),
                new PermissionsCollectingConfigParser(),
                new StatSendingConverter(),
                new CacheControlParser(),
                new AutoInappCollectingConfigParser(),
                new AttributionConfigParser(),
                new StartupUpdateConfigParser(),
                new ModulesRemoteConfigsParser()
        );
        startupParser.parseStartupResponse("{}".getBytes());

        return response.getRequestedKeys();
    }

    private void enableAllFeatures(@NonNull JsonHelper.OptJSONObject mockedResponse) throws JSONException {
        JSONObject featuresList = mock(JSONObject.class);
        JSONObject featuresJson = mock(JSONObject.class);
        when(featuresList.has(anyString())).thenReturn(true);
        when(featuresList.getJSONObject(anyString())).thenReturn(new JSONObject().put(JsonResponseKey.FEATURE_ENABLED, true));
        when(featuresJson.optJSONObject(JsonResponseKey.LIST)).thenReturn(featuresList);
        when(mockedResponse.get(eq(JsonResponseKey.FEATURES), any(JSONObject.class))).thenReturn(featuresJson);
    }
}
