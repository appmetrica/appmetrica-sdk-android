package io.appmetrica.analytics.networktasks.internal;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues;
import io.appmetrica.analytics.coreapi.internal.device.ScreenInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.Identifiers;
import io.appmetrica.analytics.coreutils.internal.services.FrameworkDetector;
import io.appmetrica.analytics.coreutils.internal.system.ConstantDeviceInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.NetworkServiceLocatorRule;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ComponentLoaderTest extends CommonTest {

    private BaseRequestConfig.ComponentLoader
        <BaseRequestConfig, BaseRequestConfig.BaseRequestArguments,
            BaseRequestConfig.DataSource<BaseRequestConfig.BaseRequestArguments>>
        mLoader;

    @Rule
    public MockedStaticRule<ConstantDeviceInfo> sConstantDeviceInfo = new MockedStaticRule<>(ConstantDeviceInfo.class);

    @Rule
    public NetworkServiceLocatorRule networkServiceLocatorRule = new NetworkServiceLocatorRule();

    private final String packageName = "test.package.name";
    @Mock
    private Context context;

    private final String uuid = UUID.randomUUID().toString();
    private final String deviceId = UUID.randomUUID().toString();
    private final String deviceIdHash = UUID.randomUUID().toString();
    private final Identifiers identifiers = new Identifiers(uuid, deviceId, deviceIdHash);
    private ConstantDeviceInfo constantDeviceInfo;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(context.getPackageName()).thenReturn(packageName);
        constantDeviceInfo = new ConstantDeviceInfo();
        when(ConstantDeviceInfo.getInstance()).thenReturn(constantDeviceInfo);

        mLoader = new BaseRequestConfig.ComponentLoader<
            BaseRequestConfig,
            BaseRequestConfig.BaseRequestArguments,
            BaseRequestConfig.DataSource<BaseRequestConfig.BaseRequestArguments>
            >(context, packageName) {
            @NonNull
            @Override
            protected BaseRequestConfig createBlankConfig() {
                return new BaseRequestConfig();
            }
        };
    }

    @Test
    public void loadDeviceTypeFromUser() {
        when(NetworkServiceLocator.getInstance().getNetworkAppContext().getScreenInfoProvider().getScreenInfo())
            .thenReturn(new ScreenInfo(0, 0, 0, 0, DeviceTypeValues.TV));

        BaseRequestConfig config = mLoader.load(
            new BaseRequestConfig.DataSource<BaseRequestConfig.BaseRequestArguments>(
                identifiers,
                new BaseRequestConfig.BaseRequestArguments(
                    "tablet",
                    "10300",
                    "3123"
                ) {

                    @Override
                    public Object mergeFrom(@NonNull Object other) {
                        return other;
                    }

                    @Override
                    public boolean compareWithOtherArguments(@NonNull Object other) {
                        return false;
                    }
                }
            )
        );
        assertThat(config.getDeviceType()).isEqualTo("tablet");
    }

    @Test
    public void loadDeviceTypeFromAppMetrica() {
        when(NetworkServiceLocator.getInstance().getNetworkAppContext().getScreenInfoProvider().getScreenInfo())
            .thenReturn(new ScreenInfo(0, 0, 0, 0, DeviceTypeValues.TV));

        BaseRequestConfig config = mLoader.load(
            new BaseRequestConfig.DataSource<BaseRequestConfig.BaseRequestArguments>(
                identifiers,
                new BaseRequestConfig.BaseRequestArguments(
                    null,
                    "10300",
                    "3123"
                ) {

                    @Override
                    public Object mergeFrom(@NonNull Object other) {
                        return other;
                    }

                    @Override
                    public boolean compareWithOtherArguments(@NonNull Object other) {
                        return false;
                    }
                }
            )
        );
        Assertions.assertThat(config.getDeviceType()).isEqualTo("tv");
    }

    @Test
    public void testLoadWhenAllArgumentsExist() {
        int screenWidth = 2880;
        int screenHeight = 1440;
        int screenDpi = 55;
        float screenScaleFactor = 7.8f;
        when(NetworkServiceLocator.getInstance().getNetworkAppContext().getScreenInfoProvider().getScreenInfo()).thenReturn(
            new ScreenInfo(screenWidth, screenHeight, screenDpi, screenScaleFactor, DeviceTypeValues.TV)
        );

        ConstantDeviceInfo constantDeviceInfo = ConstantDeviceInfo.getInstance();

        BaseRequestConfig config = mLoader.load(
            new BaseRequestConfig.DataSource<BaseRequestConfig.BaseRequestArguments>(
                identifiers,
                new BaseRequestConfig.BaseRequestArguments(
                    "phone",
                    "10300",
                    "3123"
                ) {

                    @Override
                    public Object mergeFrom(@NonNull Object other) {
                        return other;
                    }

                    @Override
                    public boolean compareWithOtherArguments(@NonNull Object other) {
                        return false;
                    }
                }
            )
        );
        NetworkAppContext networkAppContext = NetworkServiceLocator.getInstance().getNetworkAppContext();
        SoftAssertions softAssertion = new SoftAssertions();
        softAssertion.assertThat(config.getAnalyticsSdkVersionName()).as("analyticsSdkVersionName")
            .isEqualTo(networkAppContext.getSdkInfo().getSdkVersionName());
        softAssertion.assertThat(config.getDeviceType()).as("deviceType")
            .isEqualTo("phone");
        softAssertion.assertThat(config.getAppVersion()).as("appVersion")
            .isEqualTo("10300");
        softAssertion.assertThat(config.getAppBuildNumber()).as("appBuildNumber")
            .isEqualTo("3123");
        softAssertion.assertThat(config.getAppBuildNumberInt()).as("appBuildNumberInt")
            .isEqualTo(3123);
        softAssertion.assertThat(config.getPackageName()).as("packageName")
            .isEqualTo(packageName);
        softAssertion.assertThat(config.getAppFramework()).as("appFramework")
            .isEqualTo(FrameworkDetector.framework());
        softAssertion.assertThat(config.getProtocolVersion()).as("protocolVersion")
            .isEqualTo("2");
        softAssertion.assertThat(config.getKitBuildNumber()).as("kitBuildNumber")
            .isEqualTo(networkAppContext.getSdkInfo().getSdkBuildNumber());
        softAssertion.assertThat(config.getKitBuildType()).as("kitBuildType")
            .isEqualTo(networkAppContext.getSdkInfo().getSdkBuildType());
        softAssertion.assertThat(config.getUuid()).as("uuid")
            .isEqualTo(uuid);
        softAssertion.assertThat(config.getDeviceId()).as("deviceId")
            .isEqualTo(deviceId);
        softAssertion.assertThat(config.getDeviceIDHash()).as("deviceIdHash")
            .isEqualTo(deviceIdHash);
        softAssertion.assertThat(config.getScreenWidth()).as("screenWidth")
            .isEqualTo(screenWidth);
        softAssertion.assertThat(config.getScreenHeight()).as("screenHeight")
            .isEqualTo(screenHeight);
        softAssertion.assertThat(config.getScreenDpi()).as("screenDpi")
            .isEqualTo(screenDpi);
        softAssertion.assertThat(config.getScaleFactor()).as("scaleFactor")
            .isEqualTo(screenScaleFactor);
        softAssertion.assertThat(config.getManufacturer()).as("manufacturer")
            .isEqualTo(constantDeviceInfo.manufacturer);
        softAssertion.assertThat(config.getModel()).as("model")
            .isEqualTo(constantDeviceInfo.model);
        softAssertion.assertThat(config.getOsVersion()).as("osVersion")
            .isEqualTo(constantDeviceInfo.osVersion);
        softAssertion.assertThat(config.getOsApiLevel()).as("osApiLevel")
            .isEqualTo(constantDeviceInfo.osApiLevel);
        softAssertion.assertThat(config.getDeviceRootStatus()).as("deviceRootStatus")
            .isEqualTo(constantDeviceInfo.deviceRootStatus);
        softAssertion.assertThat(config.getLocale()).as("locale")
            .isEqualTo(
                NetworkServiceLocator.getInstance().getNetworkAppContext().getLocaleProvider().getLocales().get(0)
            );
        softAssertion.assertThat(config.getAppPlatform()).as("appPlatform")
            .isEqualTo(ConstantDeviceInfo.APP_PLATFORM);
        softAssertion.assertThat(config.getAdvertisingIdsHolder())
            .isEqualTo(
                NetworkServiceLocator.getInstance().getNetworkAppContext()
                    .getAdvertisingIdGetter().getIdentifiers(mock(Context.class))
            );
        softAssertion.assertAll();
    }
}
