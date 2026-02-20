package io.appmetrica.analytics.impl.preparer;

import android.content.ContentValues;
import android.util.Base64;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.ProtobufUtils;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.protobuf.backend.Referrer;
import io.appmetrica.analytics.impl.protobuf.client.DbProto;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockProvider;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class EventToPreparerReferrerSourceTest extends CommonTest {

    @Parameters(name = "{0} to {1}")
    public static Collection<Object[]> data() {
        List<Object[]> data = Arrays.asList(
            new Object[]{ReferrerInfo.Source.UNKNOWN, Referrer.UNKNOWN},
            new Object[]{ReferrerInfo.Source.GP, Referrer.GP},
            new Object[]{ReferrerInfo.Source.HMS, Referrer.HMS}
        );
        assert data.size() == ReferrerInfo.Source.values().length;
        return data;
    }

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Rule
    public MockedStaticRule<Base64> base64MockedStaticRule = new MockedStaticRule<>(Base64.class);

    @Mock
    private ReportRequestConfig config;
    @NonNull
    private final ReferrerInfo.Source modelSource;
    private final int protoSource;

    public EventToPreparerReferrerSourceTest(@NonNull ReferrerInfo.Source modelSource, int protoSource) {
        this.modelSource = modelSource;
        this.protoSource = protoSource;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void referrerSerialization() throws Exception {
        EventPreparer eventPreparer = ProtobufUtils.getEventPreparer(InternalEvents.EVENT_TYPE_SEND_REFERRER);
        ReferrerInfo referrerInfo = new ReferrerInfo("referrer", 10, 20, modelSource);
        String encodedReferrerInfo = "Encoded referrer info";
        when(Base64.decode(encodedReferrerInfo, Base64.DEFAULT)).thenReturn(referrerInfo.toProto());
        ContentValues cv = MockProvider.mockedContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = encodedReferrerInfo;
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        EventFromDbModel event = new EventFromDbModel(cv);
        Referrer referrer = Referrer.parseFrom(eventPreparer.getValueComposer().getValue(event, config));
        new ProtoObjectPropertyAssertions<Referrer>(referrer)
            .checkField("referrer", "referrer".getBytes())
            .checkField("clickTimestamp", 10L)
            .checkField("installBeginTimestamp", 20L)
            .checkField("source", protoSource)
            .checkAll();
    }
}
