package io.appmetrica.analytics.internal;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import io.appmetrica.analytics.impl.AppAppMetricaServiceCoreExecutionDispatcher;
import io.appmetrica.analytics.impl.AppAppMetricaServiceCoreImpl;
import io.appmetrica.analytics.impl.LocaleHolder;
import io.appmetrica.analytics.impl.service.AppMetricaServiceAction;
import io.appmetrica.analytics.impl.service.MetricaServiceCallback;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaServiceTest extends CommonTest {

    @Rule
    public final GlobalServiceLocatorRule rule = new GlobalServiceLocatorRule();

    @Rule
    public final MockedConstructionRule<AppAppMetricaServiceCoreImpl> cMetricaCore =
        new MockedConstructionRule<>(AppAppMetricaServiceCoreImpl.class);
    @Rule
    public final MockedConstructionRule<AppAppMetricaServiceCoreExecutionDispatcher> cMetricaCoreExecutionDispatcher =
        new MockedConstructionRule<>(AppAppMetricaServiceCoreExecutionDispatcher.class);
    @Mock
    private Configuration configuration;
    @Rule
    public final MockedStaticRule<LocaleHolder> sLocaleHolder = new MockedStaticRule<>(LocaleHolder.class);

    private Context context;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
    }

    @After
    public void tearDown() {
        AppMetricaService.clearInstance();
    }

    @Test
    public void onCreate() throws Exception {
        getMetricaService().onCreate();
        assertThat(cMetricaCore.getConstructionMock().constructed()).hasSize(1);
        assertThat(cMetricaCoreExecutionDispatcher.getConstructionMock().constructed()).hasSize(1);
        AppAppMetricaServiceCoreExecutionDispatcher mockedCore = cMetricaCoreExecutionDispatcher.getConstructionMock().constructed().get(0);
        verify(mockedCore).onCreate();
        verify(mockedCore, never()).updateCallback(any(MetricaServiceCallback.class));

        clearInvocations(mockedCore);
        getMetricaService().onCreate();
        assertThat(cMetricaCore.getConstructionMock().constructed()).hasSize(1);
        assertThat(cMetricaCoreExecutionDispatcher.getConstructionMock().constructed()).hasSize(1);
        verify(mockedCore).updateCallback(any(MetricaServiceCallback.class));
        verify(mockedCore).onCreate();
    }

    @Test
    public void onBindForWakeLockAction() {
        Intent intent = new Intent();
        intent.setAction(AppMetricaServiceAction.ACTION_SERVICE_WAKELOCK + "_some_postfix");
        AppMetricaService service = getMetricaService();
        service.onCreate();
        assertThat(service.onBind(intent)).isInstanceOf(AppMetricaService.WakeLockBinder.class);
    }

    @Test
    public void onBindForIntentWithoutAction() {
        AppMetricaService service = getMetricaService();
        service.onCreate();
        assertThat(service.onBind(new Intent())).isInstanceOf(IAppMetricaService.Stub.class);
    }

    @Test
    public void onBindForAnotherAction() {
        AppMetricaService service = getMetricaService();
        service.onCreate();
        Intent intent = new Intent("Some action");
        assertThat(service.onBind(intent)).isInstanceOf(IAppMetricaService.Stub.class);
    }

    @Test
    public void onUnbindForWakeLockAction() {
        Intent intent = new Intent();
        intent.setAction(AppMetricaServiceAction.ACTION_SERVICE_WAKELOCK + "_some_postfix");
        AppMetricaService service = getMetricaService();
        service.onCreate();
        assertThat(service.onUnbind(intent)).isFalse();
    }

    @Test
    public void onUnbindForIntentWithoutAction() {
        AppMetricaService service = getMetricaService();
        service.onCreate();
        assertThat(service.onUnbind(new Intent())).isFalse();
    }

    @Test
    public void onUnbindForIntentWithData() {
        AppMetricaService service = getMetricaService();
        service.onCreate();
        Intent intent = new Intent();
        intent.setData(Uri.parse("https://yandex.ru"));
        assertThat(service.onUnbind(intent)).isTrue();
    }

    @Test
    public void onConfigurationChanged() {
        AppMetricaService service = getMetricaService();
        service.onCreate();
        service.onConfigurationChanged(configuration);
        verify(cMetricaCoreExecutionDispatcher.getConstructionMock().constructed().get(0))
            .onConfigurationChanged(configuration);
    }

    private AppMetricaService getMetricaService() {
        AppMetricaService service = spy(new AppMetricaService());
        doReturn(RuntimeEnvironment.getApplication()).when(service).getApplicationContext();
        doReturn("blabla").when(service).getPackageName();
        when(service.getApplicationContext()).thenReturn(context);
        return service;
    }
}
