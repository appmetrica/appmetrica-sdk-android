package io.appmetrica.analytics.impl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP)
public class PhoneUtilsTest extends CommonTest {
    private Context mContext;

    @Mock
    private ConnectivityManager mConnectivityManager;
    @Mock
    private NetworkInfo mNetworkInfo;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = TestUtils.createMockedContext();
        when(mContext.getSystemService(any(String.class))).thenReturn(mConnectivityManager);
        when(mConnectivityManager.getActiveNetworkInfo()).thenReturn(mNetworkInfo);
    }

    @Test
    public void testNormalizedLocaleWithScriptAndCountry() throws Exception {
        Locale locale = new Locale.Builder().setLanguage("en").setRegion("UK").setScript("Latn").build();
        assertNormalizedLocale(locale, "en-Latn_UK");
    }

    @Test
    public void testNormalizedLocaleWithScriptWithoutCountry() throws Exception {
        Locale locale = new Locale.Builder().setLanguage("az").setScript("Arab").build();
        assertNormalizedLocale(locale, "az-Arab");
    }

    @Test
    public void testNormalizedLocaleWithoutScript() throws Exception {
        assertNormalizedLocale(new Locale("ru", "BE"), "ru_BE");
        assertNormalizedLocale(new Locale("de", "AT"), "de_AT");
        assertNormalizedLocale(Locale.FRANCE, "fr_FR");
        assertNormalizedLocale(Locale.CANADA, "en_CA");
    }

    @Test
    public void testGetConnectionTypeWhenPermittedAndOffline() {
        //noinspection deprecation
        when(mNetworkInfo.isConnected()).thenReturn(false);
        PhoneUtils.NetworkType actual = PhoneUtils.getConnectionType(mContext);
        verify(mContext, times(1)).getSystemService(Mockito.anyString());
        verify(mConnectivityManager, times(1)).getActiveNetworkInfo();
        //noinspection deprecation
        verify(mNetworkInfo, times(1)).isConnected();
        verify(mNetworkInfo, never()).getType();
        assertThat(actual).isEqualTo(PhoneUtils.NetworkType.OFFLINE);
    }

    @Test
    public void testGetConnectionTypeNullNetworkInfo() {
        when(mConnectivityManager.getActiveNetworkInfo()).thenReturn(null);
        assertThat(PhoneUtils.getConnectionType(mContext)).isEqualTo(PhoneUtils.NetworkType.OFFLINE);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    public void testVpn() {
        when(mConnectivityManager.getActiveNetworkInfo()).thenReturn(mNetworkInfo);
        //noinspection deprecation
        when(mNetworkInfo.isConnected()).thenReturn(true);
        when(mNetworkInfo.getType()).thenReturn(ConnectivityManager.TYPE_VPN);
        assertThat(PhoneUtils.getConnectionType(mContext)).isEqualTo(PhoneUtils.NetworkType.VPN);
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    @Config(sdk = Build.VERSION_CODES.M)
    public static class ConnectionTypesMTests {

        @Mock
        private ConnectivityManager mConnectivityManager;
        @Mock
        private NetworkInfo mNetworkInfo;
        @Mock
        private Network mNetwork;
        @Mock
        private NetworkCapabilities mNetworkCapabilities;
        private final Context mContext;

        private final PhoneUtils.NetworkType mExpected;

        @ParameterizedRobolectricTestRunner.Parameters(name = "expected type {1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {NetworkCapabilities.TRANSPORT_BLUETOOTH, PhoneUtils.NetworkType.BLUETOOTH},
                {NetworkCapabilities.TRANSPORT_ETHERNET, PhoneUtils.NetworkType.ETHERNET},
                {NetworkCapabilities.TRANSPORT_CELLULAR, PhoneUtils.NetworkType.CELL},
                {NetworkCapabilities.TRANSPORT_VPN, PhoneUtils.NetworkType.VPN},
                {NetworkCapabilities.TRANSPORT_WIFI, PhoneUtils.NetworkType.WIFI},
                {NetworkCapabilities.TRANSPORT_WIFI_AWARE, PhoneUtils.NetworkType.UNDEFINED},
                {NetworkCapabilities.TRANSPORT_LOWPAN, PhoneUtils.NetworkType.UNDEFINED},
                {99, PhoneUtils.NetworkType.UNDEFINED}
            });
        }

        public ConnectionTypesMTests(final int networkCapabilitiesType, PhoneUtils.NetworkType enumType) {
            MockitoAnnotations.openMocks(this);
            mContext = TestUtils.createMockedContext();
            when(mContext.getSystemService(any(String.class))).thenReturn(mConnectivityManager);
            when(mConnectivityManager.getActiveNetwork()).thenReturn(mNetwork);
            when(mConnectivityManager.getNetworkInfo(mNetwork)).thenReturn(mNetworkInfo);
            //noinspection deprecation
            when(mNetworkInfo.isConnected()).thenReturn(true);
            when(mConnectivityManager.getNetworkCapabilities(mNetwork)).thenReturn(mNetworkCapabilities);
            when(mNetworkCapabilities.hasTransport(networkCapabilitiesType)).thenReturn(true);

            mExpected = enumType;
        }

        @Test
        public void test() {
            assertThat(PhoneUtils.getConnectionType(mContext)).isEqualTo(mExpected);
        }
    }

    @RunWith(RobolectricTestRunner.class)
    @Config(sdk = Build.VERSION_CODES.M)
    public static class ConnectionTypeMTests {

        @Mock
        private ConnectivityManager mConnectivityManager;
        @Mock
        private NetworkInfo mNetworkInfo;
        @Mock
        private Network mNetwork;
        private Context mContext;

        @Before
        public void setUp() {
            MockitoAnnotations.openMocks(this);
            mContext = TestUtils.createMockedContext();
            when(mContext.getSystemService(any(String.class))).thenReturn(mConnectivityManager);
            when(mConnectivityManager.getActiveNetwork()).thenReturn(mNetwork);
            when(mConnectivityManager.getNetworkInfo(mNetwork)).thenReturn(mNetworkInfo);
        }

        @Test
        public void testActiveNetworkIsNull() {
            when(mConnectivityManager.getActiveNetwork()).thenReturn(null);
            assertThat(PhoneUtils.getConnectionType(mContext)).isEqualTo(PhoneUtils.NetworkType.OFFLINE);
        }

        @Test
        public void testNotConnected() {
            //noinspection deprecation
            when(mNetworkInfo.isConnected()).thenReturn(false);
            assertThat(PhoneUtils.getConnectionType(mContext)).isEqualTo(PhoneUtils.NetworkType.OFFLINE);
        }

        @Test
        public void testNetworkCapabilitiesNull() {
            //noinspection deprecation
            when(mNetworkInfo.isConnected()).thenReturn(true);
            when(mConnectivityManager.getNetworkCapabilities(mNetwork)).thenReturn(null);
            assertThat(PhoneUtils.getConnectionType(mContext)).isEqualTo(PhoneUtils.NetworkType.UNDEFINED);
        }

        @Test
        @Config(sdk = Build.VERSION_CODES.O_MR1)
        public void testLowPan() {
            NetworkCapabilities networkCapabilities = mock(NetworkCapabilities.class);
            //noinspection deprecation
            when(mNetworkInfo.isConnected()).thenReturn(true);
            when(mConnectivityManager.getNetworkCapabilities(mNetwork)).thenReturn(networkCapabilities);
            when(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN)).thenReturn(true);
            assertThat(PhoneUtils.getConnectionType(mContext)).isEqualTo(PhoneUtils.NetworkType.LOWPAN);
        }

        @Test
        @Config(sdk = Build.VERSION_CODES.O)
        public void testWifiAware() {
            NetworkCapabilities networkCapabilities = mock(NetworkCapabilities.class);
            //noinspection deprecation
            when(mNetworkInfo.isConnected()).thenReturn(true);
            when(mConnectivityManager.getNetworkCapabilities(mNetwork)).thenReturn(networkCapabilities);
            when(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)).thenReturn(true);
            assertThat(PhoneUtils.getConnectionType(mContext)).isEqualTo(PhoneUtils.NetworkType.WIFI_AWARE);
        }
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    @Config(sdk = Build.VERSION_CODES.P)
    public static class GetConnectionTypeInServerFormatTest {

        private final int mServerType;
        @Nullable
        private final PhoneUtils.NetworkType mSdkType;

        @ParameterizedRobolectricTestRunner.Parameters(name = "for type {0} server type is {1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {PhoneUtils.NetworkType.CELL, EventProto.ReportMessage.Session.CONNECTION_CELL},
                {PhoneUtils.NetworkType.WIFI, EventProto.ReportMessage.Session.CONNECTION_WIFI},
                {PhoneUtils.NetworkType.BLUETOOTH, EventProto.ReportMessage.Session.CONNECTION_BLUETOOTH},
                {PhoneUtils.NetworkType.ETHERNET, EventProto.ReportMessage.Session.CONNECTION_ETHERNET},
                {PhoneUtils.NetworkType.MOBILE_DUN, EventProto.ReportMessage.Session.CONNECTION_MOBILE_DUN},
                {PhoneUtils.NetworkType.MOBILE_HIPRI, EventProto.ReportMessage.Session.CONNECTION_MOBILE_HIPRI},
                {PhoneUtils.NetworkType.MOBILE_MMS, EventProto.ReportMessage.Session.CONNECTION_MOBILE_MMS},
                {PhoneUtils.NetworkType.MOBILE_SUPL, EventProto.ReportMessage.Session.CONNECTION_MOBILE_SUPL},
                {PhoneUtils.NetworkType.VPN, EventProto.ReportMessage.Session.CONNECTION_VPN},
                {PhoneUtils.NetworkType.WIMAX, EventProto.ReportMessage.Session.CONNECTION_WIMAX},
                {PhoneUtils.NetworkType.LOWPAN, EventProto.ReportMessage.Session.CONNECTION_LOWPAN},
                {PhoneUtils.NetworkType.WIFI_AWARE, EventProto.ReportMessage.Session.CONNECTION_WIFI_AWARE},
                {null, EventProto.ReportMessage.Session.CONNECTION_UNDEFINED}
            });
        }

        public GetConnectionTypeInServerFormatTest(@Nullable PhoneUtils.NetworkType sdkType, final int serverType) {
            mSdkType = sdkType;
            mServerType = serverType;
        }

        @Test
        public void testGetConnectionInServerType() {
            assertThat(PhoneUtils.getConnectionTypeInServerFormat(mSdkType)).isEqualTo(mServerType);
        }

    }

    private void assertNormalizedLocale(final Locale locale, final String expected) {
        assertThat(PhoneUtils.normalizedLocale(locale)).isEqualTo(expected);
    }
}
