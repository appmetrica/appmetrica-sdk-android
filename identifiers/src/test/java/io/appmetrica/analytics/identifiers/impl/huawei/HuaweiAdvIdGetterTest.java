package io.appmetrica.analytics.identifiers.impl.huawei;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceCommunicationException;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceNotFoundException;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.identifiers.impl.AdvIdInfo;
import io.appmetrica.analytics.identifiers.impl.AdvIdResult;
import io.appmetrica.analytics.identifiers.impl.AdvIdServiceConnectionController;
import io.appmetrica.analytics.identifiers.impl.Constants;
import io.appmetrica.gradle.testutils.assertions.Assertions;
import java.util.Random;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        Assertions.INSTANCE.ObjectPropertyAssertions(mHuaweiAdvIdGetter)
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
    public void propagatesProviderException() {
        AdvIdServiceNotFoundException exception = new AdvIdServiceNotFoundException("service is absent");
        when(connectionController.connect(mContext)).thenThrow(exception);

        assertThatThrownBy(() -> mHuaweiAdvIdGetter.getAdTrackingInfo(mContext))
                .isSameAs(exception);
        verify(connectionController).disconnect(mContext);
    }

    @Test
    public void mapsRemoteExceptionToCommunicationException() throws Throwable {
        RemoteException exception = new RemoteException("connection lost");
        when(connectionController.connect(mContext)).thenReturn(service);
        when(service.getOaid()).thenThrow(exception);

        assertThatThrownBy(() -> mHuaweiAdvIdGetter.getAdTrackingInfo(mContext))
                .isInstanceOf(AdvIdServiceCommunicationException.class)
                .hasCause(exception);
        verify(connectionController).disconnect(mContext);
    }

    @Test
    public void propagatesUnexpectedException() {
        RuntimeException exception = new RuntimeException("unexpected");
        when(connectionController.connect(mContext)).thenThrow(exception);

        assertThatThrownBy(() -> mHuaweiAdvIdGetter.getAdTrackingInfo(mContext))
                .isSameAs(exception);
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
