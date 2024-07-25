package io.appmetrica.analytics.identifiers.impl.huawei;

import android.content.Context;
import android.content.Intent;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.identifiers.impl.AdvIdInfo;
import io.appmetrica.analytics.identifiers.impl.AdvIdResult;
import io.appmetrica.analytics.identifiers.impl.AdvIdServiceConnectionController;
import io.appmetrica.analytics.identifiers.impl.ConnectionException;
import io.appmetrica.analytics.identifiers.impl.Constants;
import java.util.Random;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class HuaweiAdvIdGetterTest {

    @Mock
    private AdvIdServiceConnectionController<OpenDeviceIdentifierService> connectionController;
    @Mock
    private OpenDeviceIdentifierService service;
    private Context mContext;
    private HuaweiAdvIdGetter mHuaweiAdvIdGetter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = mock(Context.class);
        mHuaweiAdvIdGetter = new HuaweiAdvIdGetter(connectionController);
    }

    @Test
    public void constructor() throws Exception {
        mHuaweiAdvIdGetter = new HuaweiAdvIdGetter();
        ObjectPropertyAssertions(mHuaweiAdvIdGetter)
            .withPrivateFields(true)
            .withIgnoredFields("tag")
            .checkFieldMatchPredicate(
                "connectionController",
                (Predicate<AdvIdServiceConnectionController<OpenDeviceIdentifierService>>) controller -> {
                   Intent intent = controller.getConnection().getIntent();
                    return "com.uodis.opendevice.OPENIDS_SERVICE".equals(intent.getAction())
                            && "com.huawei.hwid".equals(intent.getPackage());
                }
            )
            .checkAll();
    }

    @Test
    public void connectThrowConnectionException() throws Exception {
        when(connectionController.connect(mContext))
                .thenThrow(new ConnectionException("could not resolve huawei services"));
        AdvIdResult expected = new AdvIdResult(
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                null,
                "could not resolve huawei services"
        );
        assertThat(mHuaweiAdvIdGetter.getAdTrackingInfo(mContext)).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void connectThrowConnectionExceptionWithoutMessage() throws Exception {
        when(connectionController.connect(mContext)).thenThrow(new ConnectionException(null));

        AdvIdResult expected = new AdvIdResult(
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                null,
                "unknown exception during binding huawei services"
        );
        assertThat(mHuaweiAdvIdGetter.getAdTrackingInfo(mContext)).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void connectNonConnectionException() throws Exception {
        when(connectionController.connect(mContext)).thenThrow(new RuntimeException("exception details"));
        AdvIdResult expected = new AdvIdResult(
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                null,
                "exception while fetching hoaid: exception details"
        );
        assertThat(mHuaweiAdvIdGetter.getAdTrackingInfo(mContext)).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void testHasServiceButGetOaidThrowException() throws Throwable {
        when(connectionController.connect(mContext)).thenReturn(service);
        when(service.getOaid()).thenThrow(new RuntimeException("some message"));
        AdvIdResult expected = new AdvIdResult(
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                null,
                "exception while fetching hoaid: some message"
        );
        assertThat(mHuaweiAdvIdGetter.getAdTrackingInfo(mContext)).isEqualToComparingFieldByField(expected);
        verify(connectionController).disconnect(mContext);
    }

    @Test
    public void testHasServiceBugGetTrackingStatusThrowException() throws Throwable {
        when(connectionController.connect(mContext)).thenReturn(service);
        when(service.isOaidTrackLimited()).thenThrow(new RuntimeException("some message"));
        AdvIdResult expected = new AdvIdResult(
                IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                null,
                "exception while fetching hoaid: some message"
        );
        assertThat(mHuaweiAdvIdGetter.getAdTrackingInfo(mContext)).isEqualToComparingFieldByField(expected);
        verify(connectionController).disconnect(mContext);
    }

    @Test
    public void testHasServiceBindedButOK() throws Throwable {
        final String oaid = "huawei id";
        final boolean limitAdTracking = new Random().nextBoolean();
        when(connectionController.connect(mContext)).thenReturn(service);
        when(service.getOaid()).thenReturn(oaid);
        when(service.isOaidTrackLimited()).thenReturn(limitAdTracking);
        AdvIdResult expected = new AdvIdResult(
                IdentifierStatus.OK,
                new AdvIdInfo(Constants.Providers.HUAWEI, oaid, limitAdTracking),
                null
        );
        assertThat(mHuaweiAdvIdGetter.getAdTrackingInfo(mContext)).usingRecursiveComparison().isEqualTo(expected);
        verify(connectionController).disconnect(mContext);
    }
}
