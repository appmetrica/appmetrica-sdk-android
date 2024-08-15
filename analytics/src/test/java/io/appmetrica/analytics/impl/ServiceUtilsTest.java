package io.appmetrica.analytics.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.internal.AppMetricaService;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ServiceUtilsTest extends CommonTest {

    private Context context;
    private final ScreenInfo screenInfo = new ScreenInfo(777, 888, 999, 66.6f);
    @Rule
    public final ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        when(ClientServiceLocator.getInstance().getScreenInfoRetriever().retrieveScreenInfo(context))
            .thenReturn(screenInfo);
    }

    @Test
    public void testOwnIntent() {
        Intent intent = ServiceUtils.getOwnMetricaServiceIntent(context);
        assertThat(intent.getAction()).isEqualTo("io.appmetrica.analytics.IAppMetricaService");
        assertThat(intent.getPackage()).isEqualTo(context.getPackageName());
        assertThat(intent.getComponent()).isEqualTo(new ComponentName(context.getPackageName(), "io.appmetrica.analytics.internal.AppMetricaService"));
        assertThat(intent.getFlags() & FLAG_INCLUDE_STOPPED_PACKAGES).isNotZero();
        assertThat(intent.getStringExtra(ServiceUtils.EXTRA_SCREEN_SIZE))
                .isEqualTo(JsonHelper.screenInfoToJsonString(screenInfo));
        Uri data = intent.getData();
        assertThat(data.getScheme()).isEqualTo("appmetrica");
        assertThat(data.getAuthority()).isEqualTo(context.getPackageName());
        assertThat(data.getPath()).isEqualTo("/client");
        assertThat(data.getQueryParameter("pid")).isEqualTo(String.valueOf(android.os.Process.myPid()));
        assertThat(data.getQueryParameter("psid")).isEqualTo(ProcessConfiguration.PROCESS_SESSION_ID);
    }

    @Test
    public void ownIntentForCustomClass() {
        Intent intent = ServiceUtils.getOwnMetricaServiceIntent(context);
        assertThat(intent.getComponent().getClassName()).isEqualTo(AppMetricaService.class.getName());
    }

    @Test
    public void ownIntentNoScreenSize() {
        when(ClientServiceLocator.getInstance().getScreenInfoRetriever().retrieveScreenInfo(context))
            .thenReturn(null);
        Intent intent = ServiceUtils.getOwnMetricaServiceIntent(context);
        assertThat(intent.getBundleExtra(ServiceUtils.EXTRA_SCREEN_SIZE)).isNull();
    }

    @Test
    public void testBaseIntent() {
        Intent intent = ServiceUtils.getBaseIntentToConnect(context);
        assertThat(intent.getAction()).isEqualTo("io.appmetrica.analytics.IAppMetricaService");
        assertThat(intent.getPackage()).isNull();
        assertThat(intent.getFlags() & FLAG_INCLUDE_STOPPED_PACKAGES).isNotZero();
        assertThat(intent.getComponent()).isEqualTo(new ComponentName(context.getPackageName(), "io.appmetrica.analytics.internal.AppMetricaService"));
        assertThat(intent.getBundleExtra(ServiceUtils.EXTRA_SCREEN_SIZE)).isNull();

        Uri data = intent.getData();
        assertThat(data.getScheme()).isEqualTo("appmetrica");
        assertThat(data.getAuthority()).isEqualTo(context.getPackageName());
        assertThat(data.getPath()).isEmpty();
        assertThat(data.getQueryParameter("pid")).isNull();
        assertThat(data.getQueryParameter("psid")).isNull();
    }
}
