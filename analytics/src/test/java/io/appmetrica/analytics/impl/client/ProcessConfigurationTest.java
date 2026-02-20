package io.appmetrica.analytics.impl.client;

import android.content.ContentValues;
import android.os.Process;
import android.os.ResultReceiver;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.MockProvider;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ProcessConfigurationTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    private final Map<ContentValues, HashMap<String, Object>> contentValuesDataMaps = new HashMap<>();

    @Rule
    public MockedConstructionRule<ContentValues> contentValuesMockedConstructionRule = new MockedConstructionRule<>(
        ContentValues.class,
        (mock, context) -> {
            HashMap<String, Object> dataMap = new HashMap<>();

            if (!context.arguments().isEmpty() && context.arguments().get(0) instanceof ContentValues) {
                ContentValues source = (ContentValues) context.arguments().get(0);
                HashMap<String, Object> sourceDataMap = contentValuesDataMaps.get(source);
                if (sourceDataMap != null) {
                    dataMap.putAll(sourceDataMap);
                }
            }

            contentValuesDataMaps.put(mock, dataMap);
            MockProvider.stubContentValues(mock, dataMap);
        }
    );

    private final ResultReceiver resultReceiver = mock(ResultReceiver.class);
    private ProcessConfiguration processConfiguration;
    private final AppMetricaConfig.Builder configBuilder = AppMetricaConfig
        .newConfigBuilder(TestsData.generateApiKey());

    @Before
    public void setUp() {
        processConfiguration = new ProcessConfiguration(contextRule.getContext(), resultReceiver);
    }

    @Test
    public void testGetClidsShouldReturnWrittenValidClids() {
        HashMap<String, String> clids = new HashMap<String, String>();
        clids.put("clid1", "1");
        processConfiguration.setClientClids(clids);
        assertThat(processConfiguration.getClientClids()).isEqualTo(clids);
    }

    @Test
    public void testGetClidsShouldReturnReWrittenValidClids() {
        HashMap<String, String> initialClids = new HashMap<String, String>();
        initialClids.put("clid1", "1");
        HashMap<String, String> newClids = new HashMap<String, String>();
        newClids.put("clid2", "2");
        processConfiguration.setClientClids(initialClids);
        processConfiguration.setClientClids(newClids);
        assertThat(processConfiguration.getClientClids()).isEqualTo(newClids);
    }

    @Test
    public void testGetClidsShouldReturnNullIfNothingWasWritten() {
        assertThat(processConfiguration.getClientClids()).isNull();
    }

    @Test
    public void testGetClidsShouldReturnNullIfNullWasWritten() {
        processConfiguration.setClientClids(null);
        assertThat(processConfiguration.getClientClids()).isNull();
    }

    @Test
    public void testGetClidsShouldReturnNullIfNormalClidsWasReWrittenByNull() {
        HashMap<String, String> clids = new HashMap<String, String>();
        clids.put("clid1", "1");
        processConfiguration.setClientClids(clids);
        processConfiguration.setClientClids(null);
        assertThat(processConfiguration.getClientClids()).isNull();
    }

    @Test
    public void testGetClidsReturnNormalClidsIfNullWasReWrittenByNormalClids() {
        HashMap<String, String> clids = new HashMap<String, String>();
        clids.put("clid1", "1");
        processConfiguration.setClientClids(null);
        processConfiguration.setClientClids(clids);
        assertThat(processConfiguration.getClientClids()).isEqualTo(clids);
    }

    @Test
    public void testGetDistributionReferrerShouldReturnWrittenValidValue() {
        String distributionReferrer = "distributionReferrer";
        processConfiguration.setDistributionReferrer(distributionReferrer);
        assertThat(processConfiguration.getDistributionReferrer()).isEqualTo(distributionReferrer);
    }

    @Test
    public void testGetDistributionReferrerShouldReturnNullIfNullWasWritten() {
        processConfiguration.setDistributionReferrer(null);
        assertThat(processConfiguration.getDistributionReferrer()).isNull();
    }

    @Test
    public void testGetDistributionReferrerShouldReturnReWrittenValidClids() {
        processConfiguration.setDistributionReferrer("inital referrer");
        String newReferrer = "new referrer";
        processConfiguration.setDistributionReferrer(newReferrer);
        assertThat(processConfiguration.getDistributionReferrer()).isEqualTo(newReferrer);
    }

    @Test
    public void testGetDistributionReferrerShouldReturnNullIfValidDistributionReferrerWasReWrittenByNull() {
        processConfiguration.setDistributionReferrer("inital referrer");
        processConfiguration.setDistributionReferrer(null);
        assertThat(processConfiguration.getDistributionReferrer()).isNull();
    }

    @Test
    public void testGetDistributionReferrerReturnValidValueIfNullWasReWrittenByValidValue() {
        processConfiguration.setDistributionReferrer(null);
        String validReferrer = "valid distribution referrer";
        processConfiguration.setDistributionReferrer(validReferrer);
        assertThat(processConfiguration.getDistributionReferrer()).isEqualTo(validReferrer);
    }

    @Test
    public void testGetInstallReferrerSourceShouldReturnWrittenValidValue() {
        String source = "gpl";
        processConfiguration.setInstallReferrerSource(source);
        assertThat(processConfiguration.getInstallReferrerSource()).isEqualTo(source);
    }

    @Test
    public void testGetInstallReferrerSourceShouldReturnNullIfNullWasWritten() {
        processConfiguration.setInstallReferrerSource(null);
        assertThat(processConfiguration.getInstallReferrerSource()).isNull();
    }

    @Test
    public void testGetInstallReferrerSourcShouldReturnReWritten() {
        processConfiguration.setInstallReferrerSource("inital source");
        String newSource = "new source";
        processConfiguration.setInstallReferrerSource(newSource);
        assertThat(processConfiguration.getInstallReferrerSource()).isEqualTo(newSource);
    }

    @Test
    public void testGetInstallReferrerSourceShouldReturnNullIfValidDistributionReferrerWasReWrittenByNull() {
        processConfiguration.setInstallReferrerSource("inital source");
        processConfiguration.setInstallReferrerSource(null);
        assertThat(processConfiguration.getInstallReferrerSource()).isNull();
    }

    @Test
    public void testGetInstallReferrerSourceReturnValidValueIfNullWasReWrittenByValidValue() {
        processConfiguration.setInstallReferrerSource(null);
        String newSource = "valid sourcer";
        processConfiguration.setInstallReferrerSource(newSource);
        assertThat(processConfiguration.getInstallReferrerSource()).isEqualTo(newSource);
    }

    @Test
    public void testDefaultCustomHosts() {
        assertThat(processConfiguration.getCustomHosts()).isNull();
    }

    @Test
    public void testWriteNullCustomHosts() {
        processConfiguration.setCustomHosts(null);
        assertThat(processConfiguration.getCustomHosts()).isNull();
    }

    @Test
    public void testWriteEmptyCustomHosts() {
        processConfiguration.setCustomHosts(new ArrayList<String>());
        assertThat(processConfiguration.getCustomHosts()).isNull();
    }

    @Test
    public void testWriteCustomHosts() {
        List<String> hosts = getTestCustomHosts();
        processConfiguration.setCustomHosts(hosts);
        assertThat(processConfiguration.getCustomHosts()).isEqualTo(hosts);
    }

    @Test
    public void testRewriteCustomHosts() {
        processConfiguration.setCustomHosts(new ArrayList<String>() {
            {
                add("http://initial.custom.host.ru");
            }
        });
        List<String> hosts = getTestCustomHosts();
        processConfiguration.setCustomHosts(hosts);
        assertThat(processConfiguration.getCustomHosts()).isEqualTo(hosts);
    }

    @Test
    public void testRewriteDefaultValueWithCustomHostsFromConfig() {
        List<String> customHosts = getTestCustomHosts();
        configBuilder.withCustomHosts(customHosts);
        processConfiguration.update(configBuilder.build());
        assertThat(processConfiguration.getCustomHosts()).containsExactlyElementsOf(customHosts);
    }

    @Test
    public void testRewriteNonDefaultValueWithCustomHostsFromConfig() {
        List<String> customHosts = getTestCustomHosts();
        configBuilder.withCustomHosts(customHosts);
        processConfiguration.setCustomHosts(new ArrayList<String>() {
            {
                add("https://another.custom.host.ru");
            }
        });
        processConfiguration.update(configBuilder.build());
        assertThat(processConfiguration.getCustomHosts()).containsExactlyElementsOf(customHosts);
    }

    @Test
    public void testShouldNotRewriteNonDefaultCustomHostsWithNullValueFromConfig() {
        List<String> customHosts = getTestCustomHosts();
        configBuilder.withAdditionalConfig("YMM_customHosts", null);
        processConfiguration.setCustomHosts(customHosts);
        processConfiguration.update(configBuilder.build());
        assertThat(processConfiguration.getCustomHosts()).containsExactlyElementsOf(customHosts);
    }

    @Test
    public void testRewriteCustomHostFromConfig() {
        List<String> customHosts = getTestCustomHosts();
        configBuilder.withAdditionalConfig(
            "YMM_customHosts",
            Collections.singletonList("https://another.custom.host.ru")
        );
        processConfiguration.update(configBuilder.build());
        processConfiguration.setCustomHosts(customHosts);
        assertThat(processConfiguration.getCustomHosts()).containsExactlyElementsOf(customHosts);
    }

    @Test
    public void testNullClidsValueOverridesByConfig() {
        HashMap<String, String> clidsFromConfig = new HashMap<String, String>();
        clidsFromConfig.put("clid1", "1");
        configBuilder.withAdditionalConfig("YMM_clids", clidsFromConfig);
        configBuilder.withAdditionalConfig("YMM_preloadInfoAutoTracking", false);
        processConfiguration.update(configBuilder.build());
        assertThat(processConfiguration.getClientClids())
            .containsExactly(clidsFromConfig.entrySet().toArray(new Map.Entry[clidsFromConfig.size()]));
    }

    @Test
    public void testValidClidsValueOverridesByConfig() {
        HashMap<String, String> clids = new HashMap<String, String>();
        clids.put("clid1", "1");
        HashMap<String, String> clidsFromConfig = new HashMap<String, String>();
        clidsFromConfig.put("clid1", "2");
        configBuilder.withAdditionalConfig("YMM_clids", clidsFromConfig);
        configBuilder.withAdditionalConfig("YMM_preloadInfoAutoTracking", false);
        processConfiguration.setClientClids(clids);
        processConfiguration.update(configBuilder.build());
        assertThat(processConfiguration.getClientClids())
            .containsExactly(clidsFromConfig.entrySet().toArray(new Map.Entry[clidsFromConfig.size()]));
    }

    @Test
    public void testValidClidsShouldNotOverridesByConfigIfConfigClidsIsNull() {
        HashMap<String, String> clids = new HashMap<String, String>();
        clids.put("clid1", "1");
        configBuilder.withAdditionalConfig("YMM_clids", null);
        configBuilder.withAdditionalConfig("YMM_preloadInfoAutoTracking", false);
        processConfiguration.setClientClids(clids);
        processConfiguration.update(configBuilder.build());
        assertThat(processConfiguration.getClientClids())
            .containsExactly(clids.entrySet().toArray(new Map.Entry[clids.size()]));
    }

    @Test
    public void testDistributionReferrerOverridesByConfig() {
        String distributionReferrer = "distributionReferrer";
        configBuilder.withAdditionalConfig("YMM_distributionReferrer", distributionReferrer);
        processConfiguration.update(configBuilder.build());
        assertThat(processConfiguration.getDistributionReferrer()).isEqualTo(distributionReferrer);
        assertThat(processConfiguration.getInstallReferrerSource()).isEqualTo("api");
    }

    @Test
    public void testValidDistributionReferrerValueOverridesByConfig() {
        processConfiguration.setDistributionReferrer("Distribution referrer");
        String distributionReferrerFromConfig = "Distribution referrer from config";
        configBuilder.withAdditionalConfig("YMM_distributionReferrer", distributionReferrerFromConfig);
        processConfiguration.update(configBuilder.build());
        assertThat(processConfiguration.getDistributionReferrer()).isEqualTo(distributionReferrerFromConfig);
        assertThat(processConfiguration.getInstallReferrerSource()).isEqualTo("api");
    }

    @Test
    public void testValidDistributionReferrerShouldNotOverridesByConfigIfConfigValueIsNull() {
        String distributionReferrer = "distribution referrer";
        processConfiguration.setDistributionReferrer(distributionReferrer);
        configBuilder.withAdditionalConfig("YMM_distributionReferrer", null);
        processConfiguration.update(configBuilder.build());
        assertThat(processConfiguration.getDistributionReferrer()).isEqualTo(distributionReferrer);
        assertThat(processConfiguration.getInstallReferrerSource()).isNull();
    }

    @Test
    public void testProcessID() {
        assertThat(new ProcessConfiguration(contextRule.getContext(), resultReceiver).getProcessID())
            .isEqualTo(Process.myPid());
    }

    @Test
    public void testProcessSessionID() {
        assertThat(new ProcessConfiguration(contextRule.getContext(), resultReceiver).getProcessSessionID())
            .isEqualTo(ProcessConfiguration.PROCESS_SESSION_ID);
    }

    @Test
    public void testPackageName() {
        assertThat(new ProcessConfiguration(contextRule.getContext(), resultReceiver).getPackageName()).
            isEqualTo(contextRule.getContext().getPackageName());
    }

    @Test
    public void testApiLevel() {
        assertThat(new ProcessConfiguration(contextRule.getContext(), resultReceiver).getSdkApiLevel()).
            isEqualTo(BuildConfig.API_LEVEL);
    }

    private List<String> getTestCustomHosts() {
        List<String> customHosts = new ArrayList<String>();
        customHosts.add("https://custom.host1.ru");
        customHosts.add("https://custom.host2.ru");
        return customHosts;
    }

}
