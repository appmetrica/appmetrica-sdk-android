package io.appmetrica.analytics.impl.utils;

import android.util.Log;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class PublicLoggerTests extends CommonTest {

    @NonNull
    private PublicLogger mPublicLogger;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mPublicLogger = new PublicLogger("123");
        LoggerWithApiKey.reset();
    }

    @Test
    public void setEnabled() {
        mPublicLogger.setEnabled();
        assertThat(mPublicLogger.isEnabled()).isTrue();
    }

    @Test
    public void setDisabled() {
        mPublicLogger.setDisabled();
        assertThat(mPublicLogger.isEnabled()).isFalse();
    }

    @Test
    public void logSessionEvents() {
        try (MockedStatic<Log> sLog = Mockito.mockStatic(Log.class)) {
            mPublicLogger.setEnabled();
            final String message = "some message";
            EventProto.ReportMessage.Session.Event firstEvent = new EventProto.ReportMessage.Session.Event();
            firstEvent.type = EventProto.ReportMessage.Session.Event.EVENT_CLIENT;
            firstEvent.name = "first name";
            firstEvent.value = "first value".getBytes();
            EventProto.ReportMessage.Session.Event thirdEvent = new EventProto.ReportMessage.Session.Event();
            thirdEvent.type = EventProto.ReportMessage.Session.Event.EVENT_CLIENT;
            thirdEvent.name = "third name";
            thirdEvent.value = "third value".getBytes();
            EventProto.ReportMessage.Session session = new EventProto.ReportMessage.Session();
            session.events = new EventProto.ReportMessage.Session.Event[3];
            session.events[0] = firstEvent;
            session.events[2] = thirdEvent;
            mPublicLogger.logSessionEvents(session, message);
            sLog.verify(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    Log.println(Log.INFO, "AppMetrica", "[] " + message + ": first name with value first value");
                }
            });
            sLog.verify(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    Log.println(Log.INFO, "AppMetrica", "[] " + message + ": third name with value third value");
                }
            });
        }
    }

}
