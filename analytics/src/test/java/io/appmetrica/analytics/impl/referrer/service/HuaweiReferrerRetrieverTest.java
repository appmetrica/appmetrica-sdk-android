package io.appmetrica.analytics.impl.referrer.service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class HuaweiReferrerRetrieverTest extends CommonTest {

    @Mock
    private ReferrerReceivedListener listener;
    @Mock
    private ContentResolver contentResolver;
    @Mock
    private Cursor cursor;
    @Mock
    private InterruptionSafeThread hmsReferrerThread;
    private final String packageName = "ru.yandex.test";
    private final Uri validUri = Uri.parse("content://com.huawei.appmarket.commondata/item/5");
    private HuaweiReferrerRetriever referrerRetriever;
    private Context context;
    private Runnable threadRunnable;

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        when(context.getContentResolver()).thenReturn(contentResolver);
        when(context.getPackageName()).thenReturn(packageName);
        when(contentResolver.query(validUri, null, null, new String[]{ packageName }, null)).thenReturn(cursor);
        when(GlobalServiceLocator.getInstance().getServiceExecutorProvider().getHmsReferrerThread(any(Runnable.class)))
                .thenAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        threadRunnable = invocation.getArgument(0);
                        return hmsReferrerThread;
                    }
                });
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                threadRunnable.run();
                return null;
            }
        }).when(hmsReferrerThread).start();
        referrerRetriever = new HuaweiReferrerRetriever(context);
    }

    @Test
    public void noCursor() {
        when(contentResolver.query(validUri, null, null, new String[]{ packageName }, null)).thenReturn(null);
        referrerRetriever.retrieveReferrer(listener);
        verify(listener).onReferrerReceived(null);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void getCursorThrowsException() {
        final Throwable exception = new RuntimeException();
        when(contentResolver.query(validUri, null, null, new String[]{ packageName }, null)).thenThrow(exception);
        referrerRetriever.retrieveReferrer(listener);
        verify(listener).onReferrerRetrieveError(any(ExecutionException.class));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void emptyCursor() {
        when(cursor.moveToFirst()).thenReturn(false);
        referrerRetriever.retrieveReferrer(listener);
        verify(listener).onReferrerReceived(null);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void emptyReferrer() {
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getLong(1)).thenReturn(40L);
        when(cursor.getLong(2)).thenReturn(50L);
        referrerRetriever.retrieveReferrer(listener);
        verify(listener).onReferrerReceived(null);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void hasReferrer() {
        final String referrer = "test referrer";
        final long clickTimestamp = 12;
        final long installTimestamp = 13;
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getString(0)).thenReturn(referrer);
        when(cursor.getLong(1)).thenReturn(clickTimestamp);
        when(cursor.getLong(2)).thenReturn(installTimestamp);
        referrerRetriever.retrieveReferrer(listener);
        verify(listener).onReferrerReceived(new ReferrerInfo(referrer, clickTimestamp, installTimestamp, ReferrerInfo.Source.HMS));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void getCursorHangs() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(10000);
                return cursor;
            }
        }).when(contentResolver).query(validUri, null, null, new String[]{ packageName }, null);
        when(GlobalServiceLocator.getInstance().getServiceExecutorProvider().getHmsReferrerThread(any(Runnable.class)))
                .thenAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return new InterruptionSafeThread((Runnable) invocation.getArgument(0));
                    }
                });
        long start = System.currentTimeMillis();
        referrerRetriever.retrieveReferrer(listener);
        long end = System.currentTimeMillis();
        verify(listener).onReferrerRetrieveError(any(TimeoutException.class));
        verifyNoMoreInteractions(listener);
        assertThat(end - start).isGreaterThanOrEqualTo(5000).isLessThan(10000);
    }

    @Test
    public void cursorIsClosed() {
        referrerRetriever.retrieveReferrer(listener);
        verify(cursor).close();
    }
}
