package io.appmetrica.analytics.coreutils.internal.io;

import android.database.Cursor;
import java.io.Closeable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class CloseableUtilsTest {

    @Mock
    private Closeable closeable;
    @Mock
    private Cursor cursor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void closeSafelyForNullCloseable() {
        CloseableUtils.closeSafely((Closeable) null);
    }

    @Test
    public void closeSafelyForNullCursor() {
        CloseableUtils.closeSafely((Cursor) null);
    }

    @Test
    public void closeSafelyCloseable() throws Exception {
        CloseableUtils.closeSafely(closeable);
        verify(closeable).close();
    }

    @Test
    public void closeSafelyCursor() throws Exception {
        CloseableUtils.closeSafely(cursor);
        verify(cursor).close();
    }

    @Test
    public void closeSafelyCloseableIfThrow() throws Exception {
        doThrow(new RuntimeException()).when(closeable).close();
        CloseableUtils.closeSafely(closeable);
    }

    @Test
    public void closeSafelyCursorIfThrow() throws Exception {
        doThrow(new RuntimeException()).when(cursor).close();
        CloseableUtils.closeSafely(cursor);
    }
}
