package io.appmetrica.analytics.identifiers.impl;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import com.yandex.android.advid.service.YandexAdvIdInterface;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import java.util.Random;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
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
    public void constuctor() throws Exception {
        yandexAdvIdGetter = new YandexAdvIdGetter();
        ObjectPropertyAssertions(yandexAdvIdGetter)
                .withPrivateFields(true)
                .checkFieldMatchPredicate(
                        "connectionController",
                        new Predicate<AdvIdServiceConnectionController<YandexAdvIdInterface>>() {
                            @Override
                            public boolean test(AdvIdServiceConnectionController<YandexAdvIdInterface> controller) {
                                Intent intent = controller.getConnection().getIntent();
                                return "com.yandex.android.advid.IDENTIFIER_SERVICE".equals(intent.getAction())
                                        && "com.yandex.android.advid".equals(intent.getPackage());
                            }
                        }
                )
                .checkAll();
    }

    @Test
    public void connectThrowsNoProviderException() throws ConnectionException {
        when(connectionController.connect(context)).thenThrow(new NoProviderException("some error"));
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .isEqualToComparingFieldByField(new AdvIdResult(
                        IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                        null,
                        "some error"
                ));
    }

    @Test
    public void connectWithRetriesThrowsNoProviderExceptionAlways() throws ConnectionException {
        when(connectionController.connect(context))
                .thenThrow(new NoProviderException("some error"));
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .isEqualToComparingFieldByField(new AdvIdResult(
                        IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                        null,
                        "some error"
                ));
    }

    @Test
    public void connectWithRetriesThrowsNoProviderExceptionOnce() throws ConnectionException, RemoteException {
        when(connectionController.connect(context)).thenAnswer(answerWithExceptionOnce(
                new NoProviderException("some error"),
                service
        ));
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .usingRecursiveComparison().isEqualTo(new AdvIdResult(
                        IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                        null,
                        "some error"
                ));
        verify(connectionController).disconnect(context);
    }

    @Test
    public void connectThrowsConnectionException() throws ConnectionException {
        when(connectionController.connect(context))
                .thenThrow(new ConnectionException("some error"));
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .isEqualToComparingFieldByField(new AdvIdResult(
                        IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                        null,
                        "some error"
                ));
    }

    @Test
    public void connectWithRetriesThrowsConnectionExceptionAlways() throws ConnectionException {
        when(connectionController.connect(context))
                .thenThrow(new ConnectionException("some error"));
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .isEqualToComparingFieldByField(new AdvIdResult(
                        IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                        null,
                        "some error"
                ));
        verify(connectionController).disconnect(context);
    }

    @Test
    public void connectThrowConnectionExceptionWithoutMessage() throws Exception {
        when(connectionController.connect(context))
                .thenThrow(
                        new ConnectionException(null)
                );
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .isEqualToComparingFieldByField(new AdvIdResult(
                        IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
                        null,
                        "unknown exception while binding yandex adv_id service"
                ));
    }

    @Test
    public void connectNonConnectionException() throws Exception {
        when(connectionController.connect(context))
                .thenThrow(
                        new RuntimeException("exception details")
                );
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .isEqualToComparingFieldByField(new AdvIdResult(
                        IdentifierStatus.UNKNOWN,
                        null,
                        "exception while fetching yandex adv_id: exception details"
                ));
    }

    @Test
    public void connectWithRetriesNonConnectionExceptionAlways() throws Exception {
        when(connectionController.connect(context))
                .thenThrow(
                        new RuntimeException("exception details")
                );
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .isEqualToComparingFieldByField(new AdvIdResult(
                        IdentifierStatus.UNKNOWN,
                        null,
                        "exception while fetching yandex adv_id: exception details"
                ));
        verify(connectionController).disconnect(context);
    }

    @Test
    public void testHasServiceButGetAdvIdThrowsException() throws Throwable {
        when(connectionController.connect(context)).thenReturn(service);
        when(service.getAdvId()).thenThrow(new RuntimeException("some message"));
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .isEqualToComparingFieldByField(new AdvIdResult(
                        IdentifierStatus.UNKNOWN,
                        null,
                        "exception while fetching yandex adv_id: some message"
                ));
        verify(connectionController).disconnect(context);
    }

    @Test
    public void withRetriesHasServiceButGetAdvIdThrowsExceptionAlways() throws Throwable {
        when(connectionController.connect(context)).thenReturn(service);
        when(service.getAdvId()).thenThrow(new RuntimeException("some message"));
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .isEqualToComparingFieldByField(new AdvIdResult(
                        IdentifierStatus.UNKNOWN,
                        null,
                        "exception while fetching yandex adv_id: some message"
                ));
        verify(connectionController).disconnect(context);
    }

    @Test
    public void testHasServiceButGetTrackingStatusThrowsException() throws Throwable {
        when(connectionController.connect(context)).thenReturn(service);
        when(service.isAdvIdTrackingLimited()).thenThrow(new RuntimeException("some message"));
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .isEqualToComparingFieldByField(new AdvIdResult(
                        IdentifierStatus.UNKNOWN,
                        null,
                        "exception while fetching yandex adv_id: some message"
                ));
        verify(connectionController).disconnect(context);
    }

    @Test
    public void withRetriesHasServiceButGetTrackingStatusThrowsExceptionAlways() throws Throwable {
        when(connectionController.connect(context)).thenReturn(service);
        when(service.isAdvIdTrackingLimited()).thenThrow(new RuntimeException("some message"));
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .isEqualToComparingFieldByField(new AdvIdResult(
                        IdentifierStatus.UNKNOWN,
                        null,
                        "exception while fetching yandex adv_id: some message"
                ));
        verify(connectionController).disconnect(context);
    }

    @Test
    public void testHasServiceBindedOK() throws Throwable {
        when(connectionController.connect(context)).thenReturn(service);
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .usingRecursiveComparison().isEqualTo(new AdvIdResult(
                        IdentifierStatus.OK,
                        new AdvIdInfo(Constants.Providers.YANDEX, advId, limitAdTracking),
                        null
                ));
        verify(connectionController).disconnect(context);
    }

    @Test
    public void withRetriesHasServiceBindedOK() throws Throwable {
        when(connectionController.connect(context)).thenReturn(service);
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .usingRecursiveComparison().isEqualTo(new AdvIdResult(
                        IdentifierStatus.OK,
                        new AdvIdInfo(Constants.Providers.YANDEX, advId, limitAdTracking),
                        null
                ));
        verify(connectionController).disconnect(context);
    }

    @Test
    public void withRetriesHasServiceBindedOKOnce() throws Throwable {
        when(connectionController.connect(context)).thenAnswer(new Answer<Object>() {

            private int times = 0;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (times == 0) {
                    times++;
                    return service;
                } else {
                    throw new RuntimeException();
                }
            }
        });
        assertThat(yandexAdvIdGetter.getAdTrackingInfo(context))
                .usingRecursiveComparison().isEqualTo(new AdvIdResult(
                        IdentifierStatus.OK,
                        new AdvIdInfo(Constants.Providers.YANDEX, advId, limitAdTracking),
                        null
                ));
        verify(connectionController).disconnect(context);
    }

    private Answer<Object> answerWithExceptionOnce(@NonNull final Throwable exception, @NonNull final Object returnObject) {
        return new Answer<Object>() {

            private int times = 0;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (times == 0) {
                    times++;
                    throw exception;
                } else {
                    return returnObject;
                }
            }
        };
    }
}
