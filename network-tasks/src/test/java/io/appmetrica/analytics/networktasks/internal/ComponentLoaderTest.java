package io.appmetrica.analytics.networktasks.internal;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetId;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdProvider;
import io.appmetrica.analytics.coreapi.internal.identifiers.AppSetIdScope;
import io.appmetrica.analytics.coreapi.internal.identifiers.PlatformIdentifiers;
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers;
import io.appmetrica.analytics.coreapi.internal.identifiers.SimpleAdvertisingIdGetter;
import io.appmetrica.analytics.coreapi.internal.model.AppVersionInfo;
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo;
import io.appmetrica.analytics.coreapi.internal.model.SdkEnvironment;
import io.appmetrica.analytics.coreapi.internal.model.SdkInfo;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.SdkEnvironmentProvider;
import io.appmetrica.analytics.coreutils.internal.system.ConstantDeviceInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.UUID;
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
public class ComponentLoaderTest extends CommonTest {

    private BaseRequestConfig.ComponentLoader
        <BaseRequestConfig, BaseRequestConfig.BaseRequestArguments,
            BaseRequestConfig.DataSource<BaseRequestConfig.BaseRequestArguments>>
        mLoader;

    @Rule
    public MockedStaticRule<ConstantDeviceInfo> sConstantDeviceInfo = new MockedStaticRule<>(ConstantDeviceInfo.class);

    private final String packageName = "test.package.name";
    @Mock
    private Context context;
    @Mock
    private SdkEnvironmentProvider sdkEnvironmentProvider;
    @Mock
    private SdkEnvironment sdkEnvironment;
    private AppSetId appSetId = new AppSetId(UUID.randomUUID().toString(), AppSetIdScope.DEVELOPER);
    @Mock
    private AppSetIdProvider appSetIdProvider;
    @Mock
    private SimpleAdvertisingIdGetter advertisingIdGetter;
    @Mock
    private PlatformIdentifiers platformIdentifiers;
    private AppVersionInfo appVersionInfo = new AppVersionInfo("10300", "3123");
    private SdkInfo sdkInfo = new SdkInfo("4.2.2", "13123", "buildType");
    private String appFramework = "native";
    private String deviceType = DeviceTypeValues.TABLET;
    private ScreenInfo screenInfo = new ScreenInfo(100, 200, 300, 400f);
    @Mock
    private AdvertisingIdsHolder advertisingIdsHolder;

    private final String uuid = UUID.randomUUID().toString();
    private final String deviceId = UUID.randomUUID().toString();
    private final String deviceIdHash = UUID.randomUUID().toString();
    private final SdkIdentifiers identifiers = new SdkIdentifiers(uuid, deviceId, deviceIdHash);
    private ConstantDeviceInfo constantDeviceInfo;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(context.getPackageName()).thenReturn(packageName);
        constantDeviceInfo = new ConstantDeviceInfo();
        when(ConstantDeviceInfo.getInstance()).thenReturn(constantDeviceInfo);
        when(appSetIdProvider.getAppSetId()).thenReturn(appSetId);
        when(platformIdentifiers.getAppSetIdProvider()).thenReturn(appSetIdProvider);
        when(advertisingIdGetter.getIdentifiers(context)).thenReturn(advertisingIdsHolder);
        when(platformIdentifiers.getAdvIdentifiersProvider()).thenReturn(advertisingIdGetter);
        when(sdkEnvironmentProvider.getSdkEnvironment()).thenReturn(sdkEnvironment);
        when(sdkEnvironment.getAppVersionInfo()).thenReturn(appVersionInfo);
        when(sdkEnvironment.getAppFramework()).thenReturn(appFramework);
        when(sdkEnvironment.getSdkInfo()).thenReturn(sdkInfo);
        when(sdkEnvironment.getDeviceType()).thenReturn(deviceType);
        when(sdkEnvironment.getScreenInfo()).thenReturn(screenInfo);

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
        BaseRequestConfig config = mLoader.load(
            new BaseRequestConfig.DataSource<BaseRequestConfig.BaseRequestArguments>(
                identifiers,
                sdkEnvironmentProvider,
                platformIdentifiers,
                new BaseRequestConfig.BaseRequestArguments() {

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
    public void testLoadWhenAllArgumentsExist() {

        ConstantDeviceInfo constantDeviceInfo = ConstantDeviceInfo.getInstance();

        BaseRequestConfig config = mLoader.load(
            new BaseRequestConfig.DataSource<BaseRequestConfig.BaseRequestArguments>(
                identifiers,
                sdkEnvironmentProvider,
                platformIdentifiers,
                new BaseRequestConfig.BaseRequestArguments() {

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
        SoftAssertions softAssertion = new SoftAssertions();
        softAssertion.assertThat(config.getAnalyticsSdkVersionName())
            .as("sdk version name")
            .isEqualTo(sdkInfo.getSdkVersionName());
        softAssertion.assertThat(config.getAnalyticsSdkBuildNumber())
            .as("sdk build number")
            .isEqualTo(sdkInfo.getSdkBuildNumber());
        softAssertion.assertThat(config.getAnalyticsSdkBuildType())
            .as("sdk build type")
            .isEqualTo(sdkInfo.getSdkBuildType());
        softAssertion.assertThat(config.getDeviceType()).as("deviceType")
            .isEqualTo(deviceType);
        softAssertion.assertThat(config.getAppVersion()).as("appVersion")
            .isEqualTo(appVersionInfo.getAppVersionName());
        softAssertion.assertThat(config.getAppBuildNumber()).as("appBuildNumber")
            .isEqualTo(appVersionInfo.getAppBuildNumber());
        softAssertion.assertThat(config.getAppSetId())
            .as("app set id")
            .isEqualTo(appSetId.getId());
        softAssertion.assertThat(config.getAppSetIdScope())
            .as("app set id scope")
            .isEqualTo(appSetId.getScope().getValue());
        softAssertion.assertThat(config.getPackageName()).as("packageName")
            .isEqualTo(packageName);
        softAssertion.assertThat(config.getAppFramework()).as("appFramework")
            .isEqualTo(appFramework);
        softAssertion.assertThat(config.getProtocolVersion()).as("protocolVersion")
            .isEqualTo("2");
        softAssertion.assertThat(config.getUuid()).as("uuid")
            .isEqualTo(uuid);
        softAssertion.assertThat(config.getDeviceId()).as("deviceId")
            .isEqualTo(deviceId);
        softAssertion.assertThat(config.getDeviceIDHash()).as("deviceIdHash")
            .isEqualTo(deviceIdHash);
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
        softAssertion.assertThat(config.getAppPlatform()).as("appPlatform")
            .isEqualTo(ConstantDeviceInfo.APP_PLATFORM);
        softAssertion.assertThat(config.getScreenWidth())
            .as("screen width")
            .isEqualTo(screenInfo.getWidth());
        softAssertion.assertThat(config.getScreenHeight())
            .as("scree height")
            .isEqualTo(screenInfo.getHeight());
        softAssertion.assertThat(config.getScreenDpi())
            .as("screen dpi")
            .isEqualTo(screenInfo.getDpi());
        softAssertion.assertThat(config.getScaleFactor())
            .as("screen scale factor")
            .isEqualTo(screenInfo.getScaleFactor());
        softAssertion.assertThat(config.getAdvertisingIdsHolder())
            .as("advertising ids holder")
            .isEqualTo(advertisingIdsHolder);
        softAssertion.assertAll();
    }
}
