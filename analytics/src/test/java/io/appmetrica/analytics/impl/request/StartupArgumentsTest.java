package io.appmetrica.analytics.impl.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ClientConfigurationTestUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class StartupArgumentsTest extends CommonTest {

    public static StartupRequestConfig.Arguments empty() {
        return new StartupRequestConfig.Arguments();
    }

    @Test
    public void testCreationWithoutArguments() {
        StartupRequestConfig.Arguments arguments = new StartupRequestConfig.Arguments(createStubedConfiguration());
        assertArgument(
            arguments,
            "distributionReferrer",
            "gpl",
            Collections.singletonMap("clid", "12"),
            true,
            Arrays.asList("host1", "host2")
        );
    }

    @Test
    public void testCreationWithEmptyArguments() {
        StartupRequestConfig.Arguments arguments = new StartupRequestConfig.Arguments(createStubedConfiguration());

        assertArgument(
            arguments,
            "distributionReferrer",
            "gpl",
            Collections.singletonMap("clid", "12"),
            true,
            Arrays.asList("host1", "host2")
        );
    }

    @Test
    public void testCreationWithNonEmptyArguments() {
        StartupRequestConfig.Arguments emptyArguments = new StartupRequestConfig.Arguments(
            "oldReferrer",
            "gpl",
            Collections.singletonMap("clid2", "55"),
            true,
            Collections.singletonList("newHost")
        );

        StartupRequestConfig.Arguments arguments = emptyArguments.mergeFrom(new StartupRequestConfig.Arguments(ClientConfigurationTestUtils.createStubbedConfiguration()));
        assertArgument(
            arguments,
            "oldReferrer",
            "gpl",
            Collections.singletonMap("clid2", "55"),
            true,
            Collections.singletonList("newHost")
        );
    }

    @Test
    public void testCreationWithAllEmpty() {
        StartupRequestConfig.Arguments emptyArguments = new StartupRequestConfig.Arguments(
            null,
            null,
            null,
            false,
            null
        );

        StartupRequestConfig.Arguments arguments = emptyArguments.mergeFrom(new StartupRequestConfig.Arguments(ClientConfigurationTestUtils.createStubbedConfiguration()));
        assertArgument(
            arguments,
            null,
            null,
            null,
            false,
            null
        );
    }

    @Test
    public void chooseCustomHostsForTrueAndTrue() {
        assertThat(createArgsWithHasCustomHost(true).mergeFrom(createArgsWithHasCustomHost(true)).hasNewCustomHosts)
            .isTrue();
    }

    @Test
    public void chooseCustomHostsForTrueAndFalse() {
        assertThat(createArgsWithHasCustomHost(true).mergeFrom(createArgsWithHasCustomHost(false)).hasNewCustomHosts)
            .isTrue();
    }

    @Test
    public void chooseCustomHostsForFalseAndTrue() {
        assertThat(createArgsWithHasCustomHost(false).mergeFrom(createArgsWithHasCustomHost(true)).hasNewCustomHosts)
            .isTrue();
    }

    @Test
    public void chooseCustomHostsForFalseAndFalse() {
        assertThat(createArgsWithHasCustomHost(false).mergeFrom(createArgsWithHasCustomHost(false)).hasNewCustomHosts)
            .isFalse();
    }

    private StartupRequestConfig.Arguments createArgsWithHasCustomHost(boolean value) {
        return new StartupRequestConfig.Arguments(
            null,
            null,
            null,
            value,
            null
        );
    }

    @Test
    public void testCompareToOtherArguments() {
        assertThat(new StartupRequestConfig.Arguments().compareWithOtherArguments(new StartupRequestConfig.Arguments())).isFalse();
    }

    private ClientConfiguration createStubedConfiguration() {
        ClientConfiguration clientConfiguration = ClientConfigurationTestUtils.createStubbedConfiguration();
        clientConfiguration.getReporterConfiguration().setDeviceType("phone");
        clientConfiguration.getReporterConfiguration().setCustomAppVersion("customAppVersion");
        clientConfiguration.getReporterConfiguration().setAppBuildNumber(666);
        clientConfiguration.getProcessConfiguration().setDistributionReferrer("distributionReferrer");
        clientConfiguration.getProcessConfiguration().setInstallReferrerSource("gpl");
        clientConfiguration.getProcessConfiguration().setCustomHosts(Arrays.asList("host1", "host2"));
        clientConfiguration.getProcessConfiguration().setClientClids(Collections.singletonMap("clid", "12"));
        return clientConfiguration;
    }

    private void assertArgument(@NonNull StartupRequestConfig.Arguments arguments,
                                @Nullable String distributionReferrer,
                                @Nullable String referrerSource,
                                @Nullable Map<String, String> clids,
                                boolean hasNewCustomHosts,
                                @Nullable List<String> newCustomHosts) {
        assertThat(arguments.distributionReferrer).isEqualTo(distributionReferrer);
        assertThat(arguments.installReferrerSource).isEqualTo(referrerSource);
        if (clids == null) {
            assertThat(arguments.clientClids).isNull();
        } else {
            assertThat(arguments.clientClids).containsOnly(clids.entrySet().toArray(new Map.Entry[clids.size()]));
        }
        assertThat(arguments.hasNewCustomHosts).isEqualTo(hasNewCustomHosts);
        if (hasNewCustomHosts) {
            assertThat(arguments.newCustomHosts).containsExactlyElementsOf(newCustomHosts);
        } else {
            assertThat(arguments.newCustomHosts).isNull();
        }
    }

}
