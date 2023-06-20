package io.appmetrica.analytics.impl.request.appenders;

import android.net.Uri;
import io.appmetrica.analytics.TestData;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.StatisticsRestrictionControllerImpl;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.modules.ModulesRemoteConfigArgumentsCollector;
import io.appmetrica.analytics.impl.referrer.service.ReferrerHolder;
import io.appmetrica.analytics.impl.request.Obfuscator;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.request.UrlParts;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupParamsAppenderClidsTest extends CommonTest {

    @Mock
    private StartupRequestConfig startupRequestConfig;
    @Mock
    private Obfuscator obfuscator;
    @Mock
    private ReferrerHolder referrerHolder;
    @Mock
    private StatisticsRestrictionControllerImpl statisticsRestrictionController;
    @Mock
    private ModulesRemoteConfigArgumentsCollector modulesArgumentsCollector;
    private Map<String, String> chosenClids;
    private DistributionSource clidsSource;
    private String clidsSourceString;
    private final String obfuscatedClidsSetKey = "cs";
    private final String obfuscatedClidsSetSourceKey = "css";
    private final String obfuscatedDistributionCustomizationSetKey = "dc";
    private Uri.Builder builder = new Uri.Builder();
    private StartupParamsAppender startupParamsAppender;

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(GlobalServiceLocator.getInstance().getStatisticsRestrictionController())
                .thenReturn(statisticsRestrictionController);
        when(statisticsRestrictionController.isRestrictedForSdk()).thenReturn(true);
        when(statisticsRestrictionController.isRestrictedForReporter()).thenReturn(true);
        chosenClids = new HashMap<String, String>();
        chosenClids.put("clid00", "0");
        chosenClids.put("clid11", "1");
        clidsSource = DistributionSource.APP;
        clidsSourceString = "api";
        when(startupRequestConfig.getReferrerHolder()).thenReturn(referrerHolder);
        when(startupRequestConfig.getChosenClids()).thenReturn(new ClidsInfo.Candidate(null, clidsSource));
        when(startupRequestConfig.getClidsFromClient()).thenReturn(TestData.TEST_CLIDS);

        when(obfuscator.obfuscate(UrlParts.CLIDS_SET)).thenReturn(obfuscatedClidsSetKey);
        when(obfuscator.obfuscate(UrlParts.CLIDS_SET_SOURCE)).thenReturn(obfuscatedClidsSetSourceKey);
        when(obfuscator.obfuscate(UrlParts.DISTRIBUTION_CUSTOMIZATION)).thenReturn(obfuscatedDistributionCustomizationSetKey);

        startupParamsAppender = new StartupParamsAppender(obfuscator, modulesArgumentsCollector);
    }

    @Test
    public void hasClids() {
        when(startupRequestConfig.getChosenClids()).thenReturn(new ClidsInfo.Candidate(chosenClids, clidsSource));
        startupParamsAppender.appendParams(builder, startupRequestConfig);

        Uri request = builder.build();

        String clidsParam = request.getQueryParameter(obfuscatedClidsSetKey);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(clidsParam).isNotEmpty();
        softly.assertThat(StartupUtils.decodeClids(clidsParam)).isEqualTo(chosenClids);
        softly.assertThat(request.getQueryParameter(obfuscatedClidsSetSourceKey)).isEqualTo(clidsSourceString);
        softly.assertThat(request.getQueryParameter(obfuscatedDistributionCustomizationSetKey)).isEqualTo("1");
        softly.assertAll();
    }

    @Test
    public void noClids() {
        startupParamsAppender.appendParams(builder, startupRequestConfig);
        Uri request = builder.build();

        assertThat(request.getQueryParameter(obfuscatedClidsSetKey)).isNull();
        assertThat(request.getQueryParameter(obfuscatedClidsSetSourceKey)).isNull();
        assertThat(request.getQueryParameter(obfuscatedDistributionCustomizationSetKey)).isNull();
    }

    @Test
    public void emptyClids() {
        when(startupRequestConfig.getChosenClids()).thenReturn(new ClidsInfo.Candidate(new HashMap<String, String>(), clidsSource));
        startupParamsAppender.appendParams(builder, startupRequestConfig);
        Uri request = builder.build();

        assertThat(request.getQueryParameter(obfuscatedClidsSetKey)).isNull();
        assertThat(request.getQueryParameter(obfuscatedClidsSetSourceKey)).isNull();
        assertThat(request.getQueryParameter(obfuscatedDistributionCustomizationSetKey)).isNull();
    }
}
