package io.appmetrica.analytics.impl;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.utils.MainProcessDetector;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.io.File;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaServiceDelayHandlerTest extends CommonTest {

    private static final String METRICA_SERVICE_SETTING_FILENAME = "metrica_service_settings.dat";

    @Mock
    private MainProcessDetector mainProcessDetector;
    @Mock
    private FileProvider fileProvider;
    @Mock
    private ActivityManager activityManager;
    private File delayFile;
    private Context context;
    private AppMetricaServiceDelayHandler appMetricaServiceDelayHandler;
    private final ActivityManager.RunningServiceInfo metricaServiceInfo = new ActivityManager.RunningServiceInfo();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        metricaServiceInfo.service = new ComponentName(context.getPackageName(), "io.appmetrica.analytics.internal.AppMetricaService");
        delayFile = new File(RuntimeEnvironment.getApplication().getFilesDir(), METRICA_SERVICE_SETTING_FILENAME);
        when(fileProvider.getFileFromStorage(context, METRICA_SERVICE_SETTING_FILENAME)).thenReturn(delayFile);
        when(mainProcessDetector.isMainProcess()).thenReturn(true);
        when(context.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(activityManager);
        when(fileProvider.getFileFromStorage(context, METRICA_SERVICE_SETTING_FILENAME)).thenReturn(delayFile);
        appMetricaServiceDelayHandler = new AppMetricaServiceDelayHandler(mainProcessDetector, fileProvider);
    }

    @After
    public void tearDown() throws Exception {
        delayFile.delete();
    }

    @Test
    public void setDelayNonMainProcess() {
        when(mainProcessDetector.isMainProcess()).thenReturn(false);
        appMetricaServiceDelayHandler.setDelay(context, 1000);
        verifyZeroInteractions(fileProvider);
    }

    @Test
    public void setDelayMainProcess() throws JSONException {
        when(mainProcessDetector.isMainProcess()).thenReturn(true);
        appMetricaServiceDelayHandler.setDelay(context, 1000);
        JSONAssert.assertEquals(
                new JSONObject().put("delay", 1000).toString(),
                readDelayFile(),
                true
        );
    }

    @Test
    public void setDelayMainProcessNoDirectory() {
        when(fileProvider.getFileFromStorage(context, METRICA_SERVICE_SETTING_FILENAME)).thenReturn(null);
        when(mainProcessDetector.isMainProcess()).thenReturn(true);
        appMetricaServiceDelayHandler.setDelay(context, 1000);
        assertThat(readDelayFile()).isNull();
    }

    @Test
    public void removeDelayNonMainProcess() {
        when(mainProcessDetector.isMainProcess()).thenReturn(false);
        appMetricaServiceDelayHandler.removeDelay(context);
        verifyZeroInteractions(fileProvider);
    }

    @Test
    public void removeDelayMainProcess() {
        when(mainProcessDetector.isMainProcess()).thenReturn(true);
        appMetricaServiceDelayHandler.setDelay(context, 1000);
        assertThat(delayFile.exists()).isTrue();
        appMetricaServiceDelayHandler.removeDelay(context);
        assertThat(delayFile.exists()).isFalse();
    }

    @Test
    public void removeDelayMainProcessNullFile() {
        when(mainProcessDetector.isMainProcess()).thenReturn(true);
        appMetricaServiceDelayHandler.setDelay(context, 1000);
        assertThat(delayFile.exists()).isTrue();
        when(fileProvider.getFileFromStorage(context, METRICA_SERVICE_SETTING_FILENAME)).thenReturn(null);
        appMetricaServiceDelayHandler.removeDelay(context);
        assertThat(delayFile.exists()).isTrue();
    }

    @Test
    public void maybeDelaySecondTime() {
        appMetricaServiceDelayHandler.setDelay(context, 1500);
        appMetricaServiceDelayHandler.maybeDelay(context);
        clearInvocations(activityManager);
        long start = System.currentTimeMillis();
        appMetricaServiceDelayHandler.maybeDelay(context);
        long end = System.currentTimeMillis();
        assertThat(end - start).isLessThan(1000);
        verifyZeroInteractions(activityManager);
    }

    @Test
    public void getActivityManagerThrows() {
        when(context.getSystemService(Context.ACTIVITY_SERVICE)).thenThrow(new RuntimeException());
        appMetricaServiceDelayHandler.setDelay(context, 1500);
        long start = System.currentTimeMillis();
        appMetricaServiceDelayHandler.maybeDelay(context);
        long end = System.currentTimeMillis();
        assertThat(end - start).isGreaterThanOrEqualTo(1500);
    }

    @Test
    public void getRunningServicesThrows() {
        when(activityManager.getRunningServices(anyInt())).thenThrow(new RuntimeException());
        appMetricaServiceDelayHandler.setDelay(context, 1500);
        long start = System.currentTimeMillis();
        appMetricaServiceDelayHandler.maybeDelay(context);
        long end = System.currentTimeMillis();
        assertThat(end - start).isGreaterThanOrEqualTo(1500);
    }

    @Test
    public void hasNonMetricaService() {
        ActivityManager.RunningServiceInfo serviceInfo = new ActivityManager.RunningServiceInfo();
        serviceInfo.service = new ComponentName("io.appmetrica.analytics", "UnknownService");
        when(activityManager.getRunningServices(anyInt())).thenReturn(Arrays.asList(serviceInfo));
        appMetricaServiceDelayHandler.setDelay(context, 1500);
        long start = System.currentTimeMillis();
        appMetricaServiceDelayHandler.maybeDelay(context);
        long end = System.currentTimeMillis();
        assertThat(end - start).isGreaterThanOrEqualTo(1500);
    }

    @Test
    public void maybeDelayNoFile() {
        long start = System.currentTimeMillis();
        appMetricaServiceDelayHandler.maybeDelay(context);
        long end = System.currentTimeMillis();
        assertThat(end - start).isLessThan(1000);
    }

    @Test
    public void maybeDelayZeroTimeout() {
        appMetricaServiceDelayHandler.setDelay(context, 0);
        long start = System.currentTimeMillis();
        appMetricaServiceDelayHandler.maybeDelay(context);
        long end = System.currentTimeMillis();
        assertThat(end - start).isLessThan(1000);
    }

    @Test
    public void maybeDelayHasTimeout() {
        appMetricaServiceDelayHandler.setDelay(context, 1500);
        long start = System.currentTimeMillis();
        appMetricaServiceDelayHandler.maybeDelay(context);
        long end = System.currentTimeMillis();
        assertThat(end - start).isGreaterThanOrEqualTo(1500);
    }

    @Nullable
    private String readDelayFile() {
        return IOUtils.getStringFileLocked(delayFile);
    }
}
