package io.appmetrica.analytics.impl.request;

import android.content.ContentValues;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ClientConfigurationTestUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.MockProvider;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StartupArgumentsTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();
    private Context context;

    private final Map<ContentValues, Map<String, Object>> shadows = new HashMap<>();

    @Rule
    public MockedConstructionRule<ContentValues> contentValuesMockedConstructionRule = new MockedConstructionRule<>(
        ContentValues.class,
        (mock, context) -> {
            Map<String, Object> shadow = new HashMap<>();
            shadows.put(mock, shadow);
            MockProvider.stubContentValues(mock, shadow);
        }
    );

    public static StartupRequestConfig.Arguments empty() {
        return new StartupRequestConfig.Arguments();
    }

    @Before
    public void setUp() {
        context = contextRule.getContext();
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

        StartupRequestConfig.Arguments arguments = emptyArguments.mergeFrom(
            new StartupRequestConfig.Arguments(
                ClientConfigurationTestUtils.createStubbedConfiguration(context, null))
        );
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

        StartupRequestConfig.Arguments arguments = emptyArguments.mergeFrom(
            new StartupRequestConfig.Arguments(ClientConfigurationTestUtils.createStubbedConfiguration(context, null))
        );
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
        ClientConfiguration clientConfiguration =
            ClientConfigurationTestUtils.createStubbedConfiguration(context, null);
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
