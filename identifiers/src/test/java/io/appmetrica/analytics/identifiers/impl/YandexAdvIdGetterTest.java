package io.appmetrica.analytics.identifiers.impl;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import com.yandex.android.advid.service.YandexAdvIdInterface;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceCommunicationException;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceConnectionTimeoutException;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceNotFoundException;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
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
public class YandexAdvIdGetterTest {

    @Mock
    private AdvIdServiceConnectionController<YandexAdvIdInterface> connectionController;
    @Mock
    private YandexAdvIdInterface service;
    private Context context;
    private YandexAdvIdGetter yandexAdvIdGetter;
    private final String advId = "yandex adv_id";
    private final boolean limitAdTracking = new Random().nextBoolean();

    @Before
    public void setUp() throws RemoteException {
        MockitoAnnotations.openMocks(this);
        context = mock(Context.class);
        yandexAdvIdGetter = new YandexAdvIdGetter(connectionController);
        when(service.isAdvIdTrackingLimited()).thenReturn(limitAdTracking);
        when(service.getAdvId()).thenReturn(advId);
    }

    @Test
    public void constructor() throws Exception {
        yandexAdvIdGetter = new YandexAdvIdGetter();
        Assertions.INSTANCE.ObjectPropertyAssertions(yandexAdvIdGetter)
            .withPrivateFields(true)
            .withIgnoredFields("tag")
            .checkFieldMatchPredicate(
                "connectionController",
                (Predicate<AdvIdServiceConnectionController<YandexAdvIdInterface>>) controller -> {
                    Intent intent = controller.getConnection().getIntent();
                    return "com.yandex.android.advid.IDENTIFIER_SERVICE".equals(intent.getAction())
                        && "com.yandex.android.advid".equals(intent.getPackage());
                }
            )
            .checkAll();
    }

    @Test
    public void propagatesProviderException() {
        AdvIdServiceNotFoundException exception = new AdvIdServiceNotFoundException("service is absent");
        when(connectionController.connect(context)).thenThrow(exception);

        assertThatThrownBy(() -> yandexAdvIdGetter.getAdTrackingInfo(context))
            .isSameAs(exception);

        verify(connectionController).disconnect(context);
    }

    @Test
    public void propagatesRetryableProviderException() {
        AdvIdServiceConnectionTimeoutException exception = new AdvIdServiceConnectionTimeoutException("timed out");
        when(connectionController.connect(context)).thenThrow(exception);

        assertThatThrownBy(() -> yandexAdvIdGetter.getAdTrackingInfo(context))
            .isSameAs(exception);

        verify(connectionController).disconnect(context);
    }

    @Test
    public void mapsRemoteExceptionToCommunicationException() throws RemoteException {
        RemoteException remoteException = new RemoteException("connection lost");
        when(connectionController.connect(context)).thenReturn(service);
        when(service.getAdvId()).thenThrow(remoteException);

        assertThatThrownBy(() -> yandexAdvIdGetter.getAdTrackingInfo(context))
            .isInstanceOf(AdvIdServiceCommunicationException.class)
            .hasCause(remoteException);

        verify(connectionController).disconnect(context);
    }

    @Test
    public void propagatesUnexpectedException() {
        RuntimeException exception = new RuntimeException("unexpected");
        when(connectionController.connect(context)).thenThrow(exception);

        assertThatThrownBy(() -> yandexAdvIdGetter.getAdTrackingInfo(context))
            .isSameAs(exception);

        verify(connectionController).disconnect(context);
    }

    @Test
    public void returnsFetchedIdentifier() throws Exception {
        when(connectionController.connect(context)).thenReturn(service);

        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
            .usingRecursiveComparison()
            .isEqualTo(new AdvIdResult(
                IdentifierStatus.OK,
                new AdvIdInfo(Constants.Providers.YANDEX, advId, limitAdTracking),
                null
            ));

        verify(connectionController).disconnect(context);
    }
}
