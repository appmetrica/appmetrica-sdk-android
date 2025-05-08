package io.appmetrica.analytics.impl.preparer;

import android.content.ContentValues;
import android.util.Base64;
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
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class EventToPreparerSpecialCasesTest extends CommonTest {

    @Rule
    public final GlobalServiceLocatorRule rule = new GlobalServiceLocatorRule();

    @Mock
    private ReportRequestConfig mConfig;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCustomEvent() {
        EventPreparer eventPreparer = ProtobufUtils.getEventPreparer(InternalEvents.EVENT_TYPE_CUSTOM_EVENT);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(eventPreparer.getNameComposer()).isExactlyInstanceOf(SameNameComposer.class);
        softly.assertThat(eventPreparer.getValueComposer()).isExactlyInstanceOf(StringValueComposer.class);
        softly.assertThat(eventPreparer.getEncodingTypeProvider()).isExactlyInstanceOf(NoneEncodingTypeProvider.class);

        final int type = InternalEvents.EVENT_TYPE_REGULAR.getTypeId();
        final int customType = 444;
        ContentValues cv = new ContentValues();
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE, type);
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.customType = customType;
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        EventFromDbModel event = new EventFromDbModel(cv);
        softly.assertThat(eventPreparer.getEventTypeComposer().getEventType(event)).isEqualTo(customType);

        softly.assertAll();
    }

    @Test
    public void testSendReferrer() throws Exception {
        EventPreparer eventPreparer = ProtobufUtils.getEventPreparer(InternalEvents.EVENT_TYPE_SEND_REFERRER);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(eventPreparer.getNameComposer()).isExactlyInstanceOf(SameNameComposer.class);
        softly.assertThat(eventPreparer.getEventTypeComposer()).isExactlyInstanceOf(SameEventTypeComposer.class);
        softly.assertThat(eventPreparer.getEncodingTypeProvider()).isExactlyInstanceOf(NoneEncodingTypeProvider.class);

        final String referrerValue = "referrer value";
        final long clickTimestamp = 653724876;
        final long installBeginTimestamp = 9837598;
        ReferrerInfo referrerInfo = new ReferrerInfo(referrerValue, clickTimestamp, installBeginTimestamp, ReferrerInfo.Source.GP);
        ContentValues cv = new ContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = new String(Base64.encode(referrerInfo.toProto(), Base64.DEFAULT));
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        EventFromDbModel event = new EventFromDbModel(cv);
        Referrer referrer = Referrer.parseFrom(eventPreparer.getValueComposer().getValue(event, mConfig));
        softly.assertThat(referrer.referrer).isEqualTo(referrerValue.getBytes());
        softly.assertThat(referrer.installBeginTimestamp).isEqualTo(installBeginTimestamp);
        softly.assertThat(referrer.clickTimestamp).isEqualTo(clickTimestamp);
        softly.assertThat(referrer.source).isEqualTo(Referrer.GP);
        softly.assertAll();
    }

    @Test
    public void testSendReferrerException() {
        EventPreparer eventPreparer = ProtobufUtils.getEventPreparer(InternalEvents.EVENT_TYPE_SEND_REFERRER);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(eventPreparer.getNameComposer()).isExactlyInstanceOf(SameNameComposer.class);
        softly.assertThat(eventPreparer.getEventTypeComposer()).isExactlyInstanceOf(SameEventTypeComposer.class);
        softly.assertThat(eventPreparer.getEncodingTypeProvider()).isExactlyInstanceOf(NoneEncodingTypeProvider.class);

        ContentValues cv = new ContentValues();
        DbProto.EventDescription eventDescription = new DbProto.EventDescription();
        eventDescription.value = "some value";
        cv.put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, MessageNano.toByteArray(eventDescription));
        EventFromDbModel event = new EventFromDbModel(cv);
        softly.assertThat(eventPreparer.getValueComposer().getValue(event, mConfig)).isEmpty();
        softly.assertAll();
    }

    @Test
    public void testSendReferrerEmpty() {
        EventPreparer eventPreparer = ProtobufUtils.getEventPreparer(InternalEvents.EVENT_TYPE_SEND_REFERRER);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(eventPreparer.getNameComposer()).isExactlyInstanceOf(SameNameComposer.class);
        softly.assertThat(eventPreparer.getEventTypeComposer()).isExactlyInstanceOf(SameEventTypeComposer.class);
        softly.assertThat(eventPreparer.getEncodingTypeProvider()).isExactlyInstanceOf(NoneEncodingTypeProvider.class);
        EventFromDbModel event = new EventFromDbModel(new ContentValues());
        softly.assertThat(eventPreparer.getValueComposer().getValue(event, mConfig)).isEmpty();
        softly.assertAll();
    }
}
