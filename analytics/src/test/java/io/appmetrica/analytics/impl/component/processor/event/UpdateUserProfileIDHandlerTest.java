package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class UpdateUserProfileIDHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit mUnit;
    @Captor
    private ArgumentCaptor<CounterReport> mReportArgumentCaptor;
    private UpdateUserProfileIDHandler mHandler;
    private final CounterReport mReport = new CounterReport();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mHandler = new UpdateUserProfileIDHandler(mUnit);
    }

    @Test
    public void testSetUserProfileIDIfProfileIDsAreNull() {
        when(mUnit.getProfileID()).thenReturn(null);
        mReport.setProfileID(null);
        mHandler.process(mReport);
        verify(mUnit).setProfileID(null);
    }

    @Test
    public void testSetUserProfileIDIfProfileIDsAreEquals() {
        String profileID = "User profile ID";
        when(mUnit.getProfileID()).thenReturn(profileID);
        mReport.setProfileID(profileID);
        mHandler.process(mReport);
        verify(mUnit).setProfileID(profileID);
    }

    @Test
    public void testSetUserProfileIDIfProfileIDsAreDifferent() {
        when(mUnit.getProfileID()).thenReturn("Old profile ID");
        String newProfileID = "New profile id";
        mReport.setProfileID(newProfileID);
        mHandler.process(mReport);
        verify(mUnit).setProfileID(newProfileID);
    }

    @Test
    public void testSetUserProfileIDIfNewProfileIDIsNull() {
        when(mUnit.getProfileID()).thenReturn("Old profile ID");
        mReport.setProfileID(null);
        mHandler.process(mReport);
        verify(mUnit).setProfileID(null);
    }

    @Test
    public void testSetUserProfileIDIfNewProfileIDIsEmpty() {
        when(mUnit.getProfileID()).thenReturn("Old profile ID");
        mReport.setProfileID("");
        mHandler.process(mReport);
        verify(mUnit).setProfileID("");
    }

    @Test
    public void testSetUserProfileIDIfOldProfileIDIsNull() {
        String newProfileID = "new profile ID";
        when(mUnit.getProfileID()).thenReturn(null);
        mReport.setProfileID(newProfileID);
        mHandler.process(mReport);
        verify(mUnit).setProfileID(newProfileID);
    }

    @Test
    public void testSetUserProfileIDIfOldProfileIDIsEmpty() {
        String newProfileID = "new profile ID";
        when(mUnit.getProfileID()).thenReturn("");
        mReport.setProfileID(newProfileID);
        mHandler.process(mReport);
        verify(mUnit).setProfileID(newProfileID);
    }

    @Test
    public void testDoesNotSendUserProfileIDProfileIdsAreNull() {
        when(mUnit.getProfileID()).thenReturn(null);
        mReport.setProfileID(null);
        mHandler.process(mReport);
        verify(mUnit, never()).handleReport(any(CounterReport.class));
    }

    @Test
    public void testDoesNotSendUserProfileIDProfileIdsAreEquals() {
        String profileId = "test profile id";
        when(mUnit.getProfileID()).thenReturn(profileId);
        mReport.setProfileID(profileId);
        mHandler.process(mReport);
        verify(mUnit, never()).handleReport(any(CounterReport.class));
    }

    @Test
    public void testSendUserProfileIfProfileIDsAreDifferent() {
        when(mUnit.getProfileID()).thenReturn("old profile id");
        mReport.setProfileID("new profile id");
        mHandler.process(mReport);
        verify(mUnit).handleReport(mReportArgumentCaptor.capture());
        assertThatProfileEventIsValid(mReportArgumentCaptor.getValue());
    }

    @Test
    public void testSendUserProfileIdIfNewProfileIDIsNull() {
        when(mUnit.getProfileID()).thenReturn("old profile id");
        mReport.setProfileID(null);
        mHandler.process(mReport);
        verify(mUnit).handleReport(mReportArgumentCaptor.capture());
        assertThatProfileEventIsValid(mReportArgumentCaptor.getValue());
    }

    @Test
    public void testSendUserProfileIdIfNewProfileIDIsEmpty() {
        when(mUnit.getProfileID()).thenReturn("old profile id");
        mReport.setProfileID("");
        mHandler.process(mReport);
        verify(mUnit).handleReport(mReportArgumentCaptor.capture());
        assertThatProfileEventIsValid(mReportArgumentCaptor.getValue());
    }

    @Test
    public void testSendUserProfileIDIfOldProfileIDIsNull() {
        when(mUnit.getProfileID()).thenReturn(null);
        mReport.setProfileID("New profile id");
        mHandler.process(mReport);
        verify(mUnit).handleReport(mReportArgumentCaptor.capture());
        assertThatProfileEventIsValid(mReportArgumentCaptor.getValue());
    }

    @Test
    public void testSendUserProfileIDIfOldProfileIDIsEmpty() {
        when(mUnit.getProfileID()).thenReturn("");
        mReport.setProfileID("New profile id");
        mHandler.process(mReport);
        verify(mUnit).handleReport(mReportArgumentCaptor.capture());
        assertThatProfileEventIsValid(mReportArgumentCaptor.getValue());
    }

    private void assertThatProfileEventIsValid(CounterReport counterReport) {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(counterReport.getType())
            .isEqualTo(InternalEvents.EVENT_TYPE_SEND_USER_PROFILE.getTypeId());
        softAssertions.assertThat(counterReport.getValue()).isEmpty();
        softAssertions.assertAll();
    }
}
