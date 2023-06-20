package io.appmetrica.analytics.impl.request;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues;
import io.appmetrica.analytics.coreapi.internal.device.ScreenInfo;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.impl.ClidsInfoStorage;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.ScreenInfoHolder;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.networktasks.internal.NetworkServiceLocator;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import io.appmetrica.analytics.testutils.rules.networktasks.NetworkServiceLocatorRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupRequestConfigLoaderTest extends CoreRequestConfigLoaderTest {

    @Mock
    private SafePackageManager mSafePackageManager;
    @Mock
    private ClidsInfoStorage clidsStorage;
    @Mock
    private ScreenInfoHolder screenInfoHolder;
    private Context mContext;
    private StartupRequestConfig.Loader mLoader;
    private String mPackageName = "another package name";

    @Rule
    public final GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Rule
    public NetworkServiceLocatorRule networkServiceLocatorRule = new NetworkServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(GlobalServiceLocator.getInstance().getScreenInfoHolder()).thenReturn(screenInfoHolder);
        when(screenInfoHolder.getScreenInfo()).thenReturn(new ScreenInfo(0, 0, 0, 0f, DeviceTypeValues.PHONE));
        mContext = TestUtils.createMockedContext();
        mLoader = new StartupRequestConfig.Loader(mContext, mPackageName, mSafePackageManager, clidsStorage);
    }

    @Test
    public void testLoad() {
        List<String> hostUrlsFromStartup = Arrays.asList("startup.url.1", "startup.url.2");
        List<String> hostUrlsFromClient = Arrays.asList("client.url.1", "client.url.2");
        String referrer = "test referrer";
        String referrerSource = "gpl";
        Map<String, String> clidsFromClient = Collections.singletonMap("clid0", "0");
        ClidsInfo.Candidate chosenClids = new ClidsInfo.Candidate(Collections.singletonMap("clid1", "1"), DistributionSource.APP);
        when(clidsStorage.updateAndRetrieveData(new ClidsInfo.Candidate(clidsFromClient, DistributionSource.APP))).thenReturn(chosenClids);
        List<String> newCustomHosts = Arrays.asList("host1", "host2");
        String countryInit = "by";
        long firstStartupServerTime = 495734685;
        StartupState startupState = TestUtils.createDefaultStartupStateBuilder()
                .withHostUrlsFromStartup(hostUrlsFromStartup)
                .withHostUrlsFromClient(hostUrlsFromClient)
                .withHadFirstStartup(true)
                .withCountryInit(countryInit)
                .withFirstStartupServerTime(firstStartupServerTime)
                .build();
        StartupRequestConfig.Arguments componentArguments = new StartupRequestConfig.Arguments(
                null,
                null,
                null,
                referrer,
                referrerSource,
                clidsFromClient,
                true,
                newCustomHosts
        );

        CoreRequestConfig.CoreDataSource<StartupRequestConfig.Arguments> dataSource =
                new CoreRequestConfig.CoreDataSource<>(startupState, componentArguments);
        StartupRequestConfig config = mLoader.load(dataSource);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(config.getDistributionReferrer()).isEqualTo(referrer);
        softly.assertThat(config.getInstallReferrerSource()).isEqualTo(referrerSource);
        softly.assertThat(config.getClidsFromClient()).isEqualTo(clidsFromClient);
        softly.assertThat(config.getChosenClids()).isEqualTo(chosenClids);
        softly.assertThat(config.hasNewCustomHosts()).isTrue();
        softly.assertThat(config.getNewCustomHosts()).isEqualTo(newCustomHosts);
        softly.assertThat(config.hasSuccessfulStartup()).isTrue();
        softly.assertThat(config.getCountryInit()).isEqualTo(countryInit);
        softly.assertThat(config.getFirstStartupTime()).isEqualTo(firstStartupServerTime);
        softly.assertThat(config.getStartupHostsFromStartup()).isEqualTo(hostUrlsFromStartup);
        softly.assertThat(config.getStartupHostsFromClient()).isEqualTo(hostUrlsFromClient);
        softly.assertThat(config.getAppSetId())
            .isEqualTo(
                NetworkServiceLocator.getInstance().getNetworkAppContext().getAppSetIdProvider().getAppSetId().getId()
            );
        softly.assertThat(config.getAppSetIdScope())
            .isEqualTo(
                NetworkServiceLocator.getInstance().getNetworkAppContext().getAppSetIdProvider()
                    .getAppSetId().getScope().getValue()
            );
        softly.assertAll();
    }

    @Test
    public void testNoReferrer() {
        StartupState startupState = TestUtils.createDefaultStartupState();
        StartupRequestConfig.Arguments componentArguments = new StartupRequestConfig.Arguments();

        CoreRequestConfig.CoreDataSource<StartupRequestConfig.Arguments> dataSource =
                new CoreRequestConfig.CoreDataSource<>(startupState, componentArguments);
        StartupRequestConfig config = mLoader.load(dataSource);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(config.getDistributionReferrer()).isNull();
        softly.assertThat(config.getInstallReferrerSource()).isNull();
        softly.assertAll();
    }

    @Test
    public void testHasReferrer() {
        String argumentsReferrer = "arguments referrer";
        String argumentsReferrerSource = "broadcase";
        StartupState startupState = TestUtils.createDefaultStartupState();
        StartupRequestConfig.Arguments componentArguments = new StartupRequestConfig.Arguments(
                null,
                null,
                null,
                argumentsReferrer,
                argumentsReferrerSource,
                null,
                false,
                null
        );

        CoreRequestConfig.CoreDataSource<StartupRequestConfig.Arguments> dataSource =
                new CoreRequestConfig.CoreDataSource<>(startupState, componentArguments);
        StartupRequestConfig config = mLoader.load(dataSource);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(config.getDistributionReferrer()).isEqualTo(argumentsReferrer);
        softly.assertThat(config.getInstallReferrerSource()).isEqualTo(argumentsReferrerSource);
        softly.assertAll();
    }

    @Override
    CoreRequestConfig.CoreLoader getLoader() {
        return mLoader;
    }

    @Override
    SafePackageManager getSafePackageManagerMock() {
        return mSafePackageManager;
    }

    @Override
    Context getContextMock() {
        return mContext;
    }

    @Override
    String getPackageName() {
        return mPackageName;
    }

    @Override
    Object getArguments() {
        return new StartupRequestConfig.Arguments();
    }
}
