package io.appmetrica.analytics.impl.client;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Process;
import android.os.ResultReceiver;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.impl.DataResultReceiver;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ProcessConfigurationTest extends CommonTest {

    private ResultReceiver mResultReceiver = mock(ResultReceiver.class);
    private ProcessConfiguration mProcessConfiguration = new ProcessConfiguration(RuntimeEnvironment.getApplication(), mResultReceiver);
    private AppMetricaConfig.Builder configBuilder = AppMetricaConfig
            .newConfigBuilder(TestsData.generateApiKey());

    @Test
    public void testGetClidsShouldReturnWrittenValidClids() {
        HashMap<String, String> clids = new HashMap<String, String>();
        clids.put("clid1", "1");
        mProcessConfiguration.setClientClids(clids);
        assertThat(mProcessConfiguration.getClientClids()).isEqualTo(clids);
    }

    @Test
    public void testGetClidsShouldReturnReWrittenValidClids() {
        HashMap<String, String> initialClids = new HashMap<String, String>();
        initialClids.put("clid1", "1");
        HashMap<String, String> newClids = new HashMap<String, String>();
        newClids.put("clid2", "2");
        mProcessConfiguration.setClientClids(initialClids);
        mProcessConfiguration.setClientClids(newClids);
        assertThat(mProcessConfiguration.getClientClids()).isEqualTo(newClids);
    }

    @Test
    public void testGetClidsShouldReturnNullIfNothingWasWritten() {
        assertThat(mProcessConfiguration.getClientClids()).isNull();
    }

    @Test
    public void testGetClidsShouldReturnNullIfNullWasWritten() {
        mProcessConfiguration.setClientClids(null);
        assertThat(mProcessConfiguration.getClientClids()).isNull();
    }

    @Test
    public void testGetClidsShouldReturnNullIfNormalClidsWasReWrittenByNull() {
        HashMap<String, String> clids = new HashMap<String, String>();
        clids.put("clid1", "1");
        mProcessConfiguration.setClientClids(clids);
        mProcessConfiguration.setClientClids(null);
        assertThat(mProcessConfiguration.getClientClids()).isNull();
    }

    @Test
    public void testGetClidsReturnNormalClidsIfNullWasReWrittenByNormalClids() {
        HashMap<String, String> clids = new HashMap<String, String>();
        clids.put("clid1", "1");
        mProcessConfiguration.setClientClids(null);
        mProcessConfiguration.setClientClids(clids);
        assertThat(mProcessConfiguration.getClientClids()).isEqualTo(clids);
    }

    @Test
    public void testGetDistributionReferrerShouldReturnWrittenValidValue() {
        String distributionReferrer = "distributionReferrer";
        mProcessConfiguration.setDistributionReferrer(distributionReferrer);
        assertThat(mProcessConfiguration.getDistributionReferrer()).isEqualTo(distributionReferrer);
    }

    @Test
    public void testGetDistributionReferrerShouldReturnNullIfNullWasWritten() {
        mProcessConfiguration.setDistributionReferrer(null);
        assertThat(mProcessConfiguration.getDistributionReferrer()).isNull();
    }

    @Test
    public void testGetDistributionReferrerShouldReturnReWrittenValidClids() {
        mProcessConfiguration.setDistributionReferrer("inital referrer");
        String newReferrer = "new referrer";
        mProcessConfiguration.setDistributionReferrer(newReferrer);
        assertThat(mProcessConfiguration.getDistributionReferrer()).isEqualTo(newReferrer);
    }

    @Test
    public void testGetDistributionReferrerShouldReturnNullIfValidDistributionReferrerWasReWrittenByNull() {
        mProcessConfiguration.setDistributionReferrer("inital referrer");
        mProcessConfiguration.setDistributionReferrer(null);
        assertThat(mProcessConfiguration.getDistributionReferrer()).isNull();
    }

    @Test
    public void testGetDistributionReferrerReturnValidValueIfNullWasReWrittenByValidValue() {
        mProcessConfiguration.setDistributionReferrer(null);
        String validReferrer = "valid distribution referrer";
        mProcessConfiguration.setDistributionReferrer(validReferrer);
        assertThat(mProcessConfiguration.getDistributionReferrer()).isEqualTo(validReferrer);
    }

    @Test
    public void testGetInstallReferrerSourceShouldReturnWrittenValidValue() {
        String source = "gpl";
        mProcessConfiguration.setInstallReferrerSource(source);
        assertThat(mProcessConfiguration.getInstallReferrerSource()).isEqualTo(source);
    }

    @Test
    public void testGetInstallReferrerSourceShouldReturnNullIfNullWasWritten() {
        mProcessConfiguration.setInstallReferrerSource(null);
        assertThat(mProcessConfiguration.getInstallReferrerSource()).isNull();
    }

    @Test
    public void testGetInstallReferrerSourcShouldReturnReWritten() {
        mProcessConfiguration.setInstallReferrerSource("inital source");
        String newSource = "new source";
        mProcessConfiguration.setInstallReferrerSource(newSource);
        assertThat(mProcessConfiguration.getInstallReferrerSource()).isEqualTo(newSource);
    }

    @Test
    public void testGetInstallReferrerSourceShouldReturnNullIfValidDistributionReferrerWasReWrittenByNull() {
        mProcessConfiguration.setInstallReferrerSource("inital source");
        mProcessConfiguration.setInstallReferrerSource(null);
        assertThat(mProcessConfiguration.getInstallReferrerSource()).isNull();
    }

    @Test
    public void testGetInstallReferrerSourceReturnValidValueIfNullWasReWrittenByValidValue() {
        mProcessConfiguration.setInstallReferrerSource(null);
        String newSource = "valid sourcer";
        mProcessConfiguration.setInstallReferrerSource(newSource);
        assertThat(mProcessConfiguration.getInstallReferrerSource()).isEqualTo(newSource);
    }

    @Test
    public void testDefaultCustomHosts() {
        assertThat(mProcessConfiguration.getCustomHosts()).isNull();
    }

    @Test
    public void testWriteNullCustomHosts() {
        mProcessConfiguration.setCustomHosts(null);
        assertThat(mProcessConfiguration.getCustomHosts()).isNull();
    }

    @Test
    public void testWriteEmptyCustomHosts() {
        mProcessConfiguration.setCustomHosts(new ArrayList<String>());
        assertThat(mProcessConfiguration.getCustomHosts()).isNull();
    }

    @Test
    public void testWriteCustomHosts() {
        List<String> hosts = getTestCustomHosts();
        mProcessConfiguration.setCustomHosts(hosts);
        assertThat(mProcessConfiguration.getCustomHosts()).isEqualTo(hosts);
    }

    @Test
    public void testRewriteCustomHosts() {
        mProcessConfiguration.setCustomHosts(new ArrayList<String>() {
            {
                add("http://initial.custom.host.ru");
            }
        });
        List<String> hosts = getTestCustomHosts();
        mProcessConfiguration.setCustomHosts(hosts);
        assertThat(mProcessConfiguration.getCustomHosts()).isEqualTo(hosts);
    }

    @Test
    public void testRewriteDefaultValueWithCustomHostsFromConfig() {
        List<String> customHosts = getTestCustomHosts();
        configBuilder.withCustomHosts(customHosts);
        mProcessConfiguration.update(configBuilder.build());
        assertThat(mProcessConfiguration.getCustomHosts()).containsExactlyElementsOf(customHosts);
    }

    @Test
    public void testRewriteNonDefaultValueWithCustomHostsFromConfig() {
        List<String> customHosts = getTestCustomHosts();
        configBuilder.withCustomHosts(customHosts);
        mProcessConfiguration.setCustomHosts(new ArrayList<String>() {
            {
                add("https://another.custom.host.ru");
            }
        });
        mProcessConfiguration.update(configBuilder.build());
        assertThat(mProcessConfiguration.getCustomHosts()).containsExactlyElementsOf(customHosts);
    }

    @Test
    public void testShouldNotRewriteNonDefaultCustomHostsWithNullValueFromConfig() {
        List<String> customHosts = getTestCustomHosts();
        configBuilder.withAdditionalConfig("YMM_customHosts", null);
        mProcessConfiguration.setCustomHosts(customHosts);
        mProcessConfiguration.update(configBuilder.build());
        assertThat(mProcessConfiguration.getCustomHosts()).containsExactlyElementsOf(customHosts);
    }

    @Test
    public void testRewriteCustomHostFromConfig() {
        List<String> customHosts = getTestCustomHosts();
        configBuilder.withAdditionalConfig("YMM_customHosts", Collections.singletonList("https://another.custom.host.ru"));
        mProcessConfiguration.update(configBuilder.build());
        mProcessConfiguration.setCustomHosts(customHosts);
        assertThat(mProcessConfiguration.getCustomHosts()).containsExactlyElementsOf(customHosts);
    }

    @Test
    public void testNullClidsValueOverridesByConfig() {
        HashMap<String, String> clidsFromConfig = new HashMap<String, String>();
        clidsFromConfig.put("clid1", "1");
        configBuilder.withAdditionalConfig("YMM_clids", clidsFromConfig);
        configBuilder.withAdditionalConfig("YMM_preloadInfoAutoTracking", false);
        mProcessConfiguration.update(configBuilder.build());
        assertThat(mProcessConfiguration.getClientClids()).containsExactly(clidsFromConfig.entrySet().toArray(new Map.Entry[clidsFromConfig.size()]));
    }

    @Test
    public void testValidClidsValueOverridesByConfig() {
        HashMap<String, String> clids = new HashMap<String, String>();
        clids.put("clid1", "1");
        HashMap<String, String> clidsFromConfig = new HashMap<String, String>();
        clidsFromConfig.put("clid1", "2");
        configBuilder.withAdditionalConfig("YMM_clids", clidsFromConfig);
        configBuilder.withAdditionalConfig("YMM_preloadInfoAutoTracking", false);
        mProcessConfiguration.setClientClids(clids);
        mProcessConfiguration.update(configBuilder.build());
        assertThat(mProcessConfiguration.getClientClids()).containsExactly(clidsFromConfig.entrySet().toArray(new Map.Entry[clidsFromConfig.size()]));
    }

    @Test
    public void testValidClidsShouldNotOverridesByConfigIfConfigClidsIsNull() {
        HashMap<String, String> clids = new HashMap<String, String>();
        clids.put("clid1", "1");
        configBuilder.withAdditionalConfig("YMM_clids", null);
        configBuilder.withAdditionalConfig("YMM_preloadInfoAutoTracking", false);
        mProcessConfiguration.setClientClids(clids);
        mProcessConfiguration.update(configBuilder.build());
        assertThat(mProcessConfiguration.getClientClids()).containsExactly(clids.entrySet().toArray(new Map.Entry[clids.size()]));
    }

    @Test
    public void testDistributionReferrerOverridesByConfig() {
        String distributionReferrer = "distributionReferrer";
        configBuilder.withAdditionalConfig("YMM_distributionReferrer", distributionReferrer);
        mProcessConfiguration.update(configBuilder.build());
        assertThat(mProcessConfiguration.getDistributionReferrer()).isEqualTo(distributionReferrer);
        assertThat(mProcessConfiguration.getInstallReferrerSource()).isEqualTo("api");
    }

    @Test
    public void testValidDistributionReferrerValueOverridesByConfig() {
        mProcessConfiguration.setDistributionReferrer("Distribution referrer");
        String distributionReferrerFromConfig = "Distribution referrer from config";
        configBuilder.withAdditionalConfig("YMM_distributionReferrer", distributionReferrerFromConfig);
        mProcessConfiguration.update(configBuilder.build());
        assertThat(mProcessConfiguration.getDistributionReferrer()).isEqualTo(distributionReferrerFromConfig);
        assertThat(mProcessConfiguration.getInstallReferrerSource()).isEqualTo("api");
    }

    @Test
    public void testValidDistributionReferrerShouldNotOverridesByConfigIfConfigValueIsNull() {
        String distributionReferrer = "distribution referrer";
        mProcessConfiguration.setDistributionReferrer(distributionReferrer);
        configBuilder.withAdditionalConfig("YMM_distributionReferrer", null);
        mProcessConfiguration.update(configBuilder.build());
        assertThat(mProcessConfiguration.getDistributionReferrer()).isEqualTo(distributionReferrer);
        assertThat(mProcessConfiguration.getInstallReferrerSource()).isNull();
    }

    @Test
    public void testProcessID() {
        assertThat(new ProcessConfiguration(RuntimeEnvironment.getApplication(), mResultReceiver).getProcessID()).isEqualTo(Process.myPid());
    }

    @Test
    public void testProcessSessionID() {
        assertThat(new ProcessConfiguration(RuntimeEnvironment.getApplication(), mResultReceiver).getProcessSessionID()).isEqualTo(ProcessConfiguration.PROCESS_SESSION_ID);
    }

    @Test
    public void testPackageName() {
        assertThat(new ProcessConfiguration(RuntimeEnvironment.getApplication(), mResultReceiver).getPackageName()).
                isEqualTo(RuntimeEnvironment.getApplication().getPackageName());
    }

    @Test
    public void testApiLevel() {
        assertThat(new ProcessConfiguration(RuntimeEnvironment.getApplication(), mResultReceiver).getSdkApiLevel()).
                isEqualTo(BuildConfig.API_LEVEL);
    }

    @Test
    public void testParcelable() {
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("clid key 1", "clid value 1");
        clids.put("clid key 2", "clid value 2");
        List<String> hosts = java.util.Arrays.asList("host1", "host2");
        String referrer = "referrer";
        String installReferrerSource = "gpl";

        DataResultReceiver receiver = new DataResultReceiver(new Handler(Looper.getMainLooper()), mock(DataResultReceiver.Receiver.class));

        ProcessConfiguration processConfiguration = new ProcessConfiguration(RuntimeEnvironment.getApplication(), receiver);
        processConfiguration.setClientClids(clids);
        processConfiguration.setCustomHosts(hosts);
        processConfiguration.setDistributionReferrer(referrer);
        processConfiguration.setInstallReferrerSource(installReferrerSource);
        Parcel parcel = Parcel.obtain();
        processConfiguration.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ProcessConfiguration fromParcel = ProcessConfiguration.CREATOR.createFromParcel(parcel);
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(fromParcel.getProcessID()).isEqualTo(Process.myPid());
        assertions.assertThat(fromParcel.getProcessSessionID()).isEqualTo(ProcessConfiguration.PROCESS_SESSION_ID);
        assertions.assertThat(fromParcel.getSdkApiLevel()).isEqualTo(BuildConfig.API_LEVEL);
        assertions.assertThat(fromParcel.getPackageName()).isEqualTo(RuntimeEnvironment.getApplication().getPackageName());
        assertions.assertThat(fromParcel.getCustomHosts()).containsExactlyInAnyOrderElementsOf(hosts);
        assertions.assertThat(fromParcel.getClientClids()).containsAllEntriesOf(clids).hasSameSizeAs(clids);
        assertions.assertThat(fromParcel.getDistributionReferrer()).isEqualTo(referrer);
        assertions.assertThat(fromParcel.getInstallReferrerSource()).isEqualTo(installReferrerSource);
        assertions.assertThat(fromParcel.getDataResultReceiver()).isNotNull();
        assertions.assertAll();
    }

    private List<String> getTestCustomHosts() {
        List<String> customHosts = new ArrayList<String>();
        customHosts.add("https://custom.host1.ru");
        customHosts.add("https://custom.host2.ru");
        return customHosts;
    }

}
