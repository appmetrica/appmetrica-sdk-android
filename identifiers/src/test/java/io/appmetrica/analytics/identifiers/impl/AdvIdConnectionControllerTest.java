package io.appmetrica.analytics.identifiers.impl;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import kotlin.jvm.functions.Function1;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AdvIdConnectionControllerTest {

    private Context context;
    @Mock
    private SafePackageManager safePackageManager;
    @Mock
    private AdvIdServiceConnection connection;
    @Mock
    private Function1<IBinder, Object> converter;
    @Mock
    private IBinder binder;
    @Mock
    private Object service;
    @Mock
    private Intent intent;
    @Mock
    private ResolveInfo resolveInfo;

    private final String serviceShortTag = "st";

    private AdvIdServiceConnectionController<Object> controller;

    private final long timeout = 3000L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = mock(Context.class);

        when(connection.getIntent()).thenReturn(intent);
        when(converter.invoke(binder)).thenReturn(service);

        when(safePackageManager.resolveService(context, intent, 0)).thenReturn(resolveInfo);

        final String tag = "Tag";
        controller = new AdvIdServiceConnectionController<Object>(
                connection,
                converter,
                tag,
                serviceShortTag,
                safePackageManager
        );
    }

    @Test
    public void defaultConstructor() throws Exception {
        controller = new AdvIdServiceConnectionController<>(intent, converter, serviceShortTag);

        ObjectPropertyAssertions(controller)
                .withPrivateFields(true)
                .withIgnoredFields("connection")
                .checkFieldNonNull("safePackageManager")
                .checkField("tag", String.format("[AdvIdServiceConnectionController-%s]", serviceShortTag))
                .checkField("serviceShortTag", serviceShortTag)
                .checkField("converter", converter)
                .checkAll();

        assertThat(controller.getConnection())
                .isNotNull()
                .extracting("intent")
                .isEqualTo(intent);
    }

    @Test
    public void connectIfIntentWasNotResolved() {
        when(safePackageManager.resolveService(context, intent, 0)).thenReturn(null);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                controller.connect(context);
            }
        })
                .isExactlyInstanceOf(NoProviderException.class)
                .hasMessage(String.format("could not resolve %s services", serviceShortTag));

        verify(connection, never()).bindService(context);
        verify(connection, never()).awaitBinding(anyLong());
    }

    @Test
    public void connectIfServiceExists() throws Exception {
        when(connection.bindService(context)).thenReturn(true);
        when(connection.getBinder()).thenReturn(binder);
        when(connection.awaitBinding(timeout)).thenReturn(binder);
        assertThat(controller.connect(context)).isSameAs(service);

        verify(connection).bindService(context);
        verify(connection).awaitBinding(timeout);
    }

    @Test
    public void connectIdCouldNotBindToService() {
        when(connection.bindService(context)).thenReturn(false);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                controller.connect(context);
            }
        }).isExactlyInstanceOf(ConnectionException.class)
                .hasMessage(String.format("could not bind to %s services", serviceShortTag));

        verify(connection, never()).awaitBinding(anyLong());
    }

    @Test
    public void connectWithSuccessfulAwait() throws Exception {
        when(connection.bindService(context)).thenReturn(true);
        when(connection.awaitBinding(timeout)).thenReturn(binder);

        assertThat(controller.connect(context)).isEqualTo(service);
        verify(connection).awaitBinding(timeout);
    }

    @Test
    public void connectWithoutSuccessfulAwait() {
        when(connection.bindService(context)).thenReturn(true);
        when(connection.awaitBinding(timeout)).thenReturn(null);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                controller.connect(context);
            }
        })
                .isExactlyInstanceOf(ConnectionException.class)
                .hasMessage(String.format("could not bind to %s services", serviceShortTag));

        verify(connection).awaitBinding(timeout);
    }

    @Test
    public void connectIfBindThrowException() {
        when(connection.bindService(context)).thenThrow(new RuntimeException());

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                controller.connect(context);
            }
        })
                .isExactlyInstanceOf(ConnectionException.class)
                .hasMessage(String.format("could not bind to %s services", serviceShortTag));

        verify(connection, never()).awaitBinding(anyLong());
    }

    @Test
    public void connectIfAwaitThrowException() {
        when(connection.bindService(context)).thenReturn(true);
        when(connection.awaitBinding(timeout)).thenThrow(new RuntimeException());

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                controller.connect(context);
            }
        })
                .isExactlyInstanceOf(ConnectionException.class)
                .hasMessage(String.format("could not bind to %s services", serviceShortTag));

        verify(connection, times(1)).awaitBinding(timeout);
    }

    @Test
    public void disconnect() {
        controller.disconnect(context);
        verify(connection).unbindService(context);
    }

    @Test
    public void disconnectIfConnectionThrowException() {
        doThrow(new RuntimeException()).when(connection).unbindService(context);
        controller.disconnect(context);
        verify(connection).unbindService(context);
    }

}
