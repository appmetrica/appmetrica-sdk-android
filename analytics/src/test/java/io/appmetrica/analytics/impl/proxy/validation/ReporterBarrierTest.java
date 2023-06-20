package io.appmetrica.analytics.impl.proxy.validation;

import io.appmetrica.analytics.AdRevenue;
import io.appmetrica.analytics.ValidationException;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ReporterBarrierTest extends CommonTest {

    @Mock
    private PluginsBarrier pluginsBarrier;
    private ReporterBarrier mBarrier;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mBarrier = new ReporterBarrier(pluginsBarrier);
    }

    @Test(expected = ValidationException.class)
    public void testEmptyName() {
        mBarrier.reportEvent("");
    }

    @Test(expected = ValidationException.class)
    public void testNullName() {
        mBarrier.reportEvent(null);
    }

    @Test
    public void testNameWithJson() {
        mBarrier.reportEvent("name", "json");
    }

    @Test(expected = ValidationException.class)
    public void testEmptyNameWithJson() {
        mBarrier.reportEvent("", "json");
    }

    @Test(expected = ValidationException.class)
    public void testNullNameWithJson() {
        mBarrier.reportEvent(null, "json");
    }

    @Test(expected = ValidationException.class)
    public void testReportEventWithMapShouldThrowExceptionIfEventNameIsNull() {
        mBarrier.reportEvent(null, new HashMap<String, Object>());
    }

    @Test
    public void testNameWithMap() {
        mBarrier.reportEvent("name", new HashMap<String, Object>());
    }

    @Test(expected = ValidationException.class)
    public void testEmptyNameWithMap() {
        mBarrier.reportEvent("", new HashMap<String, Object>());
    }

    @Test(expected = ValidationException.class)
    public void testNullNameWithMap() {
        mBarrier.reportEvent(null, new HashMap<String, Object>());
    }

    @Test
    public void testError() {
        mBarrier.reportError("native crash", (Throwable) null);
    }

    @Test(expected = ValidationException.class)
    public void testEmptyErrorName() {
        mBarrier.reportError("", (Throwable) null);
    }

    @Test(expected = ValidationException.class)
    public void testNullErrorName() {
        mBarrier.reportError(null, (Throwable) null);
    }

    @Test
    public void errorIdentifierWithoutThrowable() {
        mBarrier.reportError("id", "");
    }

    @Test(expected = ValidationException.class)
    public void emptyErrorIdentifierWithoutThrowable() {
        mBarrier.reportError("", "");
    }

    @Test(expected = ValidationException.class)
    public void nullErrorIdentifierWithoutThrowable() {
        mBarrier.reportError(null, "");
    }

    @Test
    public void errorIdentifier() {
        mBarrier.reportError("id", null, null);
    }

    @Test(expected = ValidationException.class)
    public void emptyErrorIdentifier() {
        mBarrier.reportError("", null, null);
    }

    @Test(expected = ValidationException.class)
    public void nullErrorIdentifier() {
        mBarrier.reportError(null, null, null);
    }

    @Test(expected = ValidationException.class)
    public void testNullUnhandledException() {
        mBarrier.reportUnhandledException(null);
    }

    @Test(expected = ValidationException.class)
    public void testNullUserProfile() {
        mBarrier.reportUserProfile(null);
    }

    @Test(expected = ValidationException.class)
    public void testNullRevenue() {
        mBarrier.reportRevenue(null);
    }

    @Test(expected = ValidationException.class)
    public void testNullAdRevenue() {
        mBarrier.reportAdRevenue(null);
    }
    
    @Test
    public void testAdRevenue() {
        mBarrier.reportAdRevenue(mock(AdRevenue.class));
    }

    @Test(expected = ValidationException.class)
    public void nullECommerce() {
        mBarrier.reportECommerce(null);
    }

    @Test
    public void eCommerce() {
        mBarrier.reportECommerce(mock(ECommerceEvent.class));
    }

    @Test
    public void testSendEventBuffer() {
        mBarrier.sendEventsBuffer();
    }

    @Test
    public void getPluginExtension() {
        assertThat(mBarrier.getPluginExtension()).isSameAs(pluginsBarrier);
    }
}
