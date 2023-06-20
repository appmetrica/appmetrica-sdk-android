package io.appmetrica.analytics.networktasks.internal;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdvIdWithLimitedAppenderTest extends CommonTest {

    @Mock
    private Uri.Builder uriBuilder;
    @Mock
    private AdvertisingIdsHolder mAdvertisingIdsHolder;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testNoParameters() {
        AdvIdWithLimitedAppender appender = new AdvIdWithLimitedAppender();
        when(mAdvertisingIdsHolder.getGoogle()).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
        when(mAdvertisingIdsHolder.getHuawei()).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
        when(mAdvertisingIdsHolder.getYandex()).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
        appender.appendParams(uriBuilder, mAdvertisingIdsHolder);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.ADV_ID, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.LIMIT_AD_TRACKING, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID_LIMIT_TRACKING, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID_LIMIT_TRACKING, "");
    }

    @Test
    public void testNoHuaweiParameters() {
        String googleId = "googleId";
        String yandexAdvId = "yandex adv id";

        AdvIdWithLimitedAppender appender = new AdvIdWithLimitedAppender();
        when(mAdvertisingIdsHolder.getGoogle()).thenReturn(
                new AdTrackingInfoResult(new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, googleId, false),
                        IdentifierStatus.OK, null)
        );
        when(mAdvertisingIdsHolder.getHuawei()).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, "some error"));
        when(mAdvertisingIdsHolder.getYandex()).thenReturn(
                new AdTrackingInfoResult(new AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, yandexAdvId, false),
                        IdentifierStatus.OK, null)
        );
        appender.appendParams(uriBuilder, mAdvertisingIdsHolder);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.ADV_ID, googleId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.LIMIT_AD_TRACKING, "0");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID_LIMIT_TRACKING, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID, yandexAdvId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID_LIMIT_TRACKING, "0");
    }

    @Test
    public void testNullHuaweiAdvId() {
        String googleId = "googleId";
        String yandexAdvId = "yandex adv id";

        AdvIdWithLimitedAppender appender = new AdvIdWithLimitedAppender();
        when(mAdvertisingIdsHolder.getGoogle()).thenReturn(
                new AdTrackingInfoResult(new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, googleId, false),
                        IdentifierStatus.OK, null)
        );
        when(mAdvertisingIdsHolder.getHuawei()).thenReturn(new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.HMS, null, false),
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                "some error"
        ));
        when(mAdvertisingIdsHolder.getYandex()).thenReturn(new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, yandexAdvId, false),
                IdentifierStatus.OK, null
        ));
        appender.appendParams(uriBuilder, mAdvertisingIdsHolder);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.ADV_ID, googleId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.LIMIT_AD_TRACKING, "0");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID_LIMIT_TRACKING, "0");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID, yandexAdvId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID_LIMIT_TRACKING, "0");
    }

    @Test
    public void testNoGoogle() {
        AdvIdWithLimitedAppender appender = new AdvIdWithLimitedAppender();
        String hmsId = "hmsId";
        String yandexAdvId = "yandex adv id";
        when(mAdvertisingIdsHolder.getGoogle()).thenReturn(
                new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, "some error")
        );
        when(mAdvertisingIdsHolder.getHuawei()).thenReturn(
                new AdTrackingInfoResult(
                        new AdTrackingInfo(AdTrackingInfo.Provider.HMS, hmsId, false),
                        IdentifierStatus.OK,
                        null
                )
        );
        when(mAdvertisingIdsHolder.getYandex()).thenReturn(
                new AdTrackingInfoResult(
                        new AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, yandexAdvId, false),
                        IdentifierStatus.OK,
                        null
                )
        );
        appender.appendParams(uriBuilder, mAdvertisingIdsHolder);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.ADV_ID, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.LIMIT_AD_TRACKING, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID, hmsId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID_LIMIT_TRACKING, "0");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID, yandexAdvId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID_LIMIT_TRACKING, "0");
    }

    @Test
    public void testNullGoogleAdvId() {
        AdvIdWithLimitedAppender appender = new AdvIdWithLimitedAppender();
        String hmsId = "hmsId";
        String yandexAdvId = "yandex adv id";
        when(mAdvertisingIdsHolder.getGoogle()).thenReturn(new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, null, false),
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                "some error"
        ));
        when(mAdvertisingIdsHolder.getHuawei()).thenReturn(
                new AdTrackingInfoResult(
                        new AdTrackingInfo(AdTrackingInfo.Provider.HMS, hmsId, false),
                        IdentifierStatus.OK,
                        null
                )
        );
        when(mAdvertisingIdsHolder.getYandex()).thenReturn(
                new AdTrackingInfoResult(
                        new AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, yandexAdvId, false),
                        IdentifierStatus.OK,
                        null
                )
        );
        appender.appendParams(uriBuilder, mAdvertisingIdsHolder);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.ADV_ID, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.LIMIT_AD_TRACKING, "0");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID, hmsId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID_LIMIT_TRACKING, "0");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID, yandexAdvId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID_LIMIT_TRACKING, "0");
    }

    @Test
    public void testNoYandex() {
        AdvIdWithLimitedAppender appender = new AdvIdWithLimitedAppender();
        String googleId = "googleId";
        String hmsId = "hmsId";
        when(mAdvertisingIdsHolder.getGoogle()).thenReturn(
                new AdTrackingInfoResult(
                        new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, googleId, false),
                        IdentifierStatus.OK,
                        null
                )
        );
        when(mAdvertisingIdsHolder.getHuawei()).thenReturn(
                new AdTrackingInfoResult(
                        new AdTrackingInfo(AdTrackingInfo.Provider.HMS, hmsId, false),
                        IdentifierStatus.OK,
                        null
                )
        );
        when(mAdvertisingIdsHolder.getYandex()).thenReturn(
                new AdTrackingInfoResult(null, IdentifierStatus.OK, null)
        );
        appender.appendParams(uriBuilder, mAdvertisingIdsHolder);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.ADV_ID, googleId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.LIMIT_AD_TRACKING, "0");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID, hmsId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID_LIMIT_TRACKING, "0");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID_LIMIT_TRACKING, "");
    }

    @Test
    public void testNullYandexAdvId() {
        AdvIdWithLimitedAppender appender = new AdvIdWithLimitedAppender();
        String googleId = "googleId";
        String hmsId = "hmsId";
        when(mAdvertisingIdsHolder.getGoogle()).thenReturn(new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, googleId, false),
                IdentifierStatus.OK, null
        ));
        when(mAdvertisingIdsHolder.getHuawei()).thenReturn(
                new AdTrackingInfoResult(
                        new AdTrackingInfo(AdTrackingInfo.Provider.HMS, hmsId, false),
                        IdentifierStatus.OK,
                        null
                )
        );
        when(mAdvertisingIdsHolder.getYandex()).thenReturn(
                new AdTrackingInfoResult(
                        new AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, null, false),
                        IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                        "some error"
                )
        );
        appender.appendParams(uriBuilder, mAdvertisingIdsHolder);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.ADV_ID, googleId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.LIMIT_AD_TRACKING, "0");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID, hmsId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID_LIMIT_TRACKING, "0");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID, "");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID_LIMIT_TRACKING, "0");
    }

    @Test
    public void testHasParameters() {
        AdvIdWithLimitedAppender appender = new AdvIdWithLimitedAppender();
        String hmsId = "hmsId";
        String googleId = "googleId";
        String yandexAdvId = "yandexId";
        when(mAdvertisingIdsHolder.getGoogle()).thenReturn(
                new AdTrackingInfoResult(
                        new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, googleId, false),
                        IdentifierStatus.OK,
                        null
                )
        );
        when(mAdvertisingIdsHolder.getHuawei()).thenReturn(
                new AdTrackingInfoResult(
                        new AdTrackingInfo(AdTrackingInfo.Provider.HMS, hmsId, false),
                        IdentifierStatus.OK,
                        null
                )
        );
        when(mAdvertisingIdsHolder.getYandex()).thenReturn(
                new AdTrackingInfoResult(
                        new AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, yandexAdvId, false),
                        IdentifierStatus.OK,
                        null
                )
        );
        appender.appendParams(uriBuilder, mAdvertisingIdsHolder);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.ADV_ID, googleId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.LIMIT_AD_TRACKING, "0");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID, hmsId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.HUAWEI_OAID_LIMIT_TRACKING, "0");
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID, yandexAdvId);
        verify(uriBuilder).appendQueryParameter(CommonUrlParts.YANDEX_ADV_ID_LIMIT_TRACKING, "0");
    }

    @RunWith(Parameterized.class)
    public static class BooleanValueTest {

        @Parameterized.Parameters(name = "For {0} should be \"{1}\"")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {null, ""},
                    {Boolean.TRUE, "1"},
                    {Boolean.FALSE, "0"}
            });
        }

        @Nullable
        private final Boolean input;
        @NonNull
        private final String output;

        public BooleanValueTest(@Nullable Boolean input, @NonNull String output) {
            this.input = input;
            this.output = output;
        }

        @Test
        public void test() {
            assertThat(AdvIdWithLimitedAppender.toGetParam(input)).isEqualTo(output);
        }

    }

}
