package io.appmetrica.analytics.impl.stub;

import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.profile.UserProfile;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.HashMap;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(RobolectricTestRunner.class)
public class ReporterExtendedStubTest extends CommonTest {

    @Mock
    private UserProfile userProfile;
    @Mock
    private Revenue revenue;
    @Mock
    private ECommerceEvent eCommerceEvent;
    @Mock
    private UnhandledException unhandledException;
    @Mock
    private AllThreads allThreads;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void putAppEnvironmentValue() {
        getStub().putAppEnvironmentValue("key", "value");
    }

    @Test
    public void clearAppEnvironment() {
        getStub().clearAppEnvironment();
    }

    @Test
    public void sendEventsBuffer() {
        getStub().sendEventsBuffer();
    }

    @Test
    public void reportEvent() {
        getStub().reportEvent("name");
    }

    @Test
    public void reportEventWithJson() {
        getStub().reportEvent("name", new JSONObject().toString());
    }

    @Test
    public void reportEventWithMap() {
        getStub().reportEvent("name", new HashMap<String, Object>());
    }

    @Test
    public void reportErrorWithThrowable() {
        getStub().reportError("name", new RuntimeException());
    }

    @Test
    public void reportErrorWithIdentifier() {
        getStub().reportError("Identifier", "Error message");
    }

    @Test
    public void reportErrorWithIdentifierAndThrowable() {
        getStub().reportError("identifier", "Error message", new RuntimeException());
    }

    @Test
    public void reportUnhandledException() {
        getStub().reportUnhandledException(new RuntimeException());
    }

    @Test
    public void resumeSession() {
        getStub().resumeSession();
    }

    @Test
    public void pauseSession() {
        getStub().pauseSession();
    }

    @Test
    public void setUserProfileID() {
        getStub().setUserProfileID("profileID");
    }

    @Test
    public void reportUserProfile() {
        getStub().reportUserProfile(userProfile);
        verifyNoInteractions(userProfile);
    }

    @Test
    public void reportRevenue() {
        getStub().reportRevenue(revenue);
        verifyNoInteractions(revenue);
    }

    @Test
    public void reportECommerce() {
        getStub().reportECommerce(eCommerceEvent);
        verifyNoInteractions(eCommerceEvent);
    }

    @Test
    public void setDataSendingEnabled() {
        getStub().setDataSendingEnabled(true);
    }

    @Test
    public void reportUnhandledExceptionWithWrapper() {
        getStub().reportUnhandledException(unhandledException);
        verifyNoInteractions(unhandledException);
    }

    @Test
    public void reportAnr() {
        getStub().reportAnr(allThreads);
        verifyNoInteractions(allThreads);
    }

    @Test
    public void getPluginExtension() {
        assertThat(getStub().getPluginExtension()).isInstanceOf(PluginReporterStub.class);
    }

    @Test
    public void setSessionExtra() {
        getStub().setSessionExtra("Key", new byte[]{2, 6, 8});
    }

    public ReporterExtendedStub getStub() {
        return new ReporterExtendedStub();
    }

}
