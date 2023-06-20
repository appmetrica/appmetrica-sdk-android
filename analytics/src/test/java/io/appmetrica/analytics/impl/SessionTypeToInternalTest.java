package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.impl.component.session.SessionType;
import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class SessionTypeToInternalTest extends CommonTest {

    @Rule
    public final GlobalServiceLocatorRule rule  = new GlobalServiceLocatorRule();

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0} to {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {EventProto.ReportMessage.Session.SessionDesc.SESSION_FOREGROUND, SessionType.FOREGROUND},
                {EventProto.ReportMessage.Session.SessionDesc.SESSION_BACKGROUND, SessionType.BACKGROUND},
                {42, SessionType.FOREGROUND},
        });
    }

    private final int mProtoType;
    private final SessionType mExpected;

    public SessionTypeToInternalTest(int protoType, SessionType expected) {
        mProtoType = protoType;
        mExpected = expected;
    }

    @Test
    public void test() {
        assertThat(ProtobufUtils.sessionTypeToInternal(mProtoType)).isEqualTo(mExpected);
    }
}
