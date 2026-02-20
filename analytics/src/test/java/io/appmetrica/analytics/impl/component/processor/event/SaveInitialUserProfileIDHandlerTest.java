package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SaveInitialUserProfileIDHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit componentUnit;

    private final CounterReport report = new CounterReport();

    private SaveInitialUserProfileIDHandler handler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        handler = new SaveInitialUserProfileIDHandler(componentUnit);
    }

    @Test
    public void process() {
        String userProfileID = "user_profile_id";
        report.setProfileID(userProfileID);
        handler.process(report);
        verify(componentUnit).setProfileID(userProfileID);
    }

    @Test
    public void processForNullUserProfileID() {
        report.setProfileID(null);
        handler.process(report);
        verify(componentUnit, never()).setProfileID(nullable(String.class));
    }

    @Test
    public void processForEmptyUserProfileID() {
        report.setProfileID("");
        handler.process(report);
        verify(componentUnit, never()).setProfileID(nullable(String.class));
    }
}
