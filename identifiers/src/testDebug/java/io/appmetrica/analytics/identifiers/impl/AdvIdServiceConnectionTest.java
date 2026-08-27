package io.appmetrica.analytics.identifiers.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class AdvIdServiceConnectionTest {

    private Context context;
    @Mock
    private Intent intent;
    @Mock
    private ComponentName componentName;
    @Mock
    private IBinder binder;

    private final String serviceTag = "Service tag";

    private AdvIdServiceConnection connection;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = mock(Context.class);

        connection = new AdvIdServiceConnection(intent, serviceTag);
    }

    @Test
    public void constructor() {
        assertThat(connection.getIntent()).isEqualTo(intent);
    }

    @Test
    public void bindService() {
        connection.bindService(context);
        verify(context).bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Test
    public void unbindService() {
        connection.unbindService(context);
        verify(context).unbindService(connection);
    }

    @Test
    public void awaitForBindingWithoutNotifying() {
        long before = System.currentTimeMillis();
        long timeout = 500L;
        connection.bindService(context);
        assertThat(connection.awaitBinding(timeout)).isNull();
        assertThat(System.currentTimeMillis() - before).isCloseTo(timeout, withPercentage(10d));
    }

    @Test
    public void awaitForBindingWithNotificationBeforeAwaiting() {
        long before = System.currentTimeMillis();
        long timeout = 500L;
        connection.bindService(context);
        connection.onServiceConnected(componentName, binder);
        assertThat(connection.awaitBinding(timeout)).isEqualTo(binder);
        assertThat(System.currentTimeMillis() - before).isNotCloseTo(timeout, withPercentage(10d));
    }

    @Test
    public void awaitForBindingWithNotificationDuringAwaiting() {
        long timeout = 5000L;
        final long notificationTimeout = 1000L;
        connection.bindService(context);

        long before = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(notificationTimeout);
                    connection.onServiceConnected(componentName, binder);
                } catch (InterruptedException e) {
                }
            }
        }).start();

        assertThat(connection.awaitBinding(timeout)).isEqualTo(binder);
        assertThat(System.currentTimeMillis() - before).isCloseTo(notificationTimeout, withPercentage(100d));
    }

    @Test
    public void awaitForBindingWithNotificationAfterServiceDisconnected() {
        connection.bindService(context);
        connection.onServiceConnected(componentName, binder);
        connection.onServiceDisconnected(componentName);
        assertThat(connection.awaitBinding(500L)).isNull();
    }

    @Test
    public void awaitForBindingWithNotificationAfterUnbind() {
        connection.bindService(context);
        connection.onServiceConnected(componentName, binder);
        connection.unbindService(context);
        assertThat(connection.awaitBinding(500L)).isNull();
    }

    @Test
    public void awaitForBindingWithUbbind() {
        long before = System.currentTimeMillis();
        connection.bindService(context);
        final long notificationDelay = 1000L;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(notificationDelay);
                    connection.unbindService(context);
                } catch (InterruptedException e) {
                }
            }
        }).start();
        assertThat(connection.awaitBinding(5000L)).isNull();
        assertThat(System.currentTimeMillis() - before).isCloseTo(notificationDelay, withPercentage(100d));
    }

    @Test
    public void awaitForBindingWithOnBindingDiedNotification() {
        long before = System.currentTimeMillis();
        connection.bindService(context);
        final long notificationDelay = 1000L;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(notificationDelay);
                    connection.onBindingDied(componentName);
                } catch (InterruptedException e) {
                }
            }
        }).start();
        assertThat(connection.awaitBinding(5000L)).isNull();
        assertThat(System.currentTimeMillis() - before).isCloseTo(notificationDelay, withPercentage(100d));
    }

    @Test
    public void awaitForBindingWithOnNullBindingNotification() {
        long before = System.currentTimeMillis();
        connection.bindService(context);
        final long notificationDelay = 1000L;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(notificationDelay);
                    connection.onNullBinding(componentName);
                } catch (InterruptedException e) {
                }
            }
        }).start();
        assertThat(connection.awaitBinding(5000L)).isNull();
        assertThat(System.currentTimeMillis() - before).isCloseTo(notificationDelay, withPercentage(100d));
    }

    @Test
    public void getServiceBeforeConnection() {
        assertThat(connection.getBinder()).isNull();
    }

    @Test
    public void getServiceAfterBindingBeginning() {
        connection.bindService(context);
        assertThat(connection.getBinder()).isNull();
    }

    @Test
    public void getServiceAfterOnServiceConnectedNotification() {
        connection.bindService(context);
        connection.onServiceConnected(componentName, binder);
        assertThat(connection.getBinder()).isEqualTo(binder);
    }

    @Test
    public void getServiceAfterOnServiceDisconnected() {
        connection.bindService(context);
        connection.onServiceConnected(componentName, binder);
        connection.onServiceDisconnected(componentName);
        assertThat(connection.getBinder()).isNull();
    }

    @Test
    public void getServiceAfterUnbind() {
        connection.bindService(context);
        connection.onServiceConnected(componentName, binder);
        connection.unbindService(context);
        assertThat(connection.getBinder()).isNull();
    }

    @Test
    public void getServiceAfterOnBindingDiedNotification() {
        connection.bindService(context);
        connection.onBindingDied(componentName);
        assertThat(connection.getBinder()).isNull();
    }

    @Test
    public void getServiceAfterOnBindingDiedNotificationDuringExistingConnection() {
        connection.bindService(context);
        connection.onServiceConnected(componentName, binder);
        connection.onBindingDied(componentName);
        assertThat(connection.getBinder()).isNull();
    }

    @Test
    public void getServiceAfterOnNullBindingNotification() {
        connection.bindService(context);
        connection.onBindingDied(componentName);
        assertThat(connection.getBinder()).isNull();
    }
}
