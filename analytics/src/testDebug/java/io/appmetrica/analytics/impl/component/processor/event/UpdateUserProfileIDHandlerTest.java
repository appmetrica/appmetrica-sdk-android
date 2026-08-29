package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.gradle.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdateUserProfileIDHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit mUnit;
    @Captor
    private ArgumentCaptor<CoreServiceEvent> mReportArgumentCaptor;
    private UpdateUserProfileIDHandler mHandler;
    private final CoreServiceEvent serviceEvent = new CoreServiceEvent();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mHandler = new UpdateUserProfileIDHandler(mUnit);
    }

    @Test
    public void testSetUserProfileIDIfProfileIDsAreNull() {
        when(mUnit.getProfileID()).thenReturn(null);
        serviceEvent.setProfileID(null);
        mHandler.process(serviceEvent);
        verify(mUnit).setProfileID(null);
    }

    @Test
    public void testSetUserProfileIDIfProfileIDsAreEquals() {
        String profileID = "User profile ID";
        when(mUnit.getProfileID()).thenReturn(profileID);
        serviceEvent.setProfileID(profileID);
        mHandler.process(serviceEvent);
        verify(mUnit).setProfileID(profileID);
    }

    @Test
    public void testSetUserProfileIDIfProfileIDsAreDifferent() {
        when(mUnit.getProfileID()).thenReturn("Old profile ID");
        String newProfileID = "New profile id";
        serviceEvent.setProfileID(newProfileID);
        mHandler.process(serviceEvent);
        verify(mUnit).setProfileID(newProfileID);
    }

    @Test
    public void testSetUserProfileIDIfNewProfileIDIsNull() {
        when(mUnit.getProfileID()).thenReturn("Old profile ID");
        serviceEvent.setProfileID(null);
        mHandler.process(serviceEvent);
        verify(mUnit).setProfileID(null);
    }

    @Test
    public void testSetUserProfileIDIfNewProfileIDIsEmpty() {
        when(mUnit.getProfileID()).thenReturn("Old profile ID");
        serviceEvent.setProfileID("");
        mHandler.process(serviceEvent);
        verify(mUnit).setProfileID("");
    }

    @Test
    public void testSetUserProfileIDIfOldProfileIDIsNull() {
        String newProfileID = "new profile ID";
        when(mUnit.getProfileID()).thenReturn(null);
        serviceEvent.setProfileID(newProfileID);
        mHandler.process(serviceEvent);
        verify(mUnit).setProfileID(newProfileID);
    }

    @Test
    public void testSetUserProfileIDIfOldProfileIDIsEmpty() {
        String newProfileID = "new profile ID";
        when(mUnit.getProfileID()).thenReturn("");
        serviceEvent.setProfileID(newProfileID);
        mHandler.process(serviceEvent);
        verify(mUnit).setProfileID(newProfileID);
    }

    @Test
    public void testDoesNotSendUserProfileIDProfileIdsAreNull() {
        when(mUnit.getProfileID()).thenReturn(null);
        serviceEvent.setProfileID(null);
        mHandler.process(serviceEvent);
        verify(mUnit, never()).handleReport(any(CoreServiceEvent.class));
    }

    @Test
    public void testDoesNotSendUserProfileIDProfileIdsAreEquals() {
        String profileId = "test profile id";
        when(mUnit.getProfileID()).thenReturn(profileId);
        serviceEvent.setProfileID(profileId);
        mHandler.process(serviceEvent);
        verify(mUnit, never()).handleReport(any(CoreServiceEvent.class));
    }

    @Test
    public void testSendUserProfileIfProfileIDsAreDifferent() {
        when(mUnit.getProfileID()).thenReturn("old profile id");
        serviceEvent.setProfileID("new profile id");
        mHandler.process(serviceEvent);
        verify(mUnit).handleReport(mReportArgumentCaptor.capture());
        assertThatProfileEventIsValid(mReportArgumentCaptor.getValue());
    }

    @Test
    public void testSendUserProfileIdIfNewProfileIDIsNull() {
        when(mUnit.getProfileID()).thenReturn("old profile id");
        serviceEvent.setProfileID(null);
        mHandler.process(serviceEvent);
        verify(mUnit).handleReport(mReportArgumentCaptor.capture());
        assertThatProfileEventIsValid(mReportArgumentCaptor.getValue());
    }

    @Test
    public void testSendUserProfileIdIfNewProfileIDIsEmpty() {
        when(mUnit.getProfileID()).thenReturn("old profile id");
        serviceEvent.setProfileID("");
        mHandler.process(serviceEvent);
        verify(mUnit).handleReport(mReportArgumentCaptor.capture());
        assertThatProfileEventIsValid(mReportArgumentCaptor.getValue());
    }

    @Test
    public void testSendUserProfileIDIfOldProfileIDIsNull() {
        when(mUnit.getProfileID()).thenReturn(null);
        serviceEvent.setProfileID("New profile id");
        mHandler.process(serviceEvent);
        verify(mUnit).handleReport(mReportArgumentCaptor.capture());
        assertThatProfileEventIsValid(mReportArgumentCaptor.getValue());
    }

    @Test
    public void testSendUserProfileIDIfOldProfileIDIsEmpty() {
        when(mUnit.getProfileID()).thenReturn("");
        serviceEvent.setProfileID("New profile id");
        mHandler.process(serviceEvent);
        verify(mUnit).handleReport(mReportArgumentCaptor.capture());
        assertThatProfileEventIsValid(mReportArgumentCaptor.getValue());
    }

    private void assertThatProfileEventIsValid(CoreServiceEvent serviceEvent) {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(serviceEvent.getType())
            .isEqualTo(InternalEvents.EVENT_TYPE_SEND_USER_PROFILE.getTypeId());
        softAssertions.assertThat(serviceEvent.getValue()).isNullOrEmpty();
        softAssertions.assertAll();
    }
}
