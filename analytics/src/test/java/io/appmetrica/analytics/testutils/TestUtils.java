package io.appmetrica.analytics.testutils;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Pair;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {

    public static Collection<Integer> generateSequence(int from, int to) {
        List<Integer> list = new ArrayList<Integer>(to - from);
        for (int i = from; i < to; list.add(i), i++) ;
        return list;
    }

    public static <K, V> Map.Entry<K, V> mapEntry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<K, V>(key, value);
    }

    public static void setSdkInt(int fieldNewValue) {
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", fieldNewValue);
    }

    public static Handler createBlockingExecutionHandlerStub() {
        Handler handler = mock(Handler.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }
        }).when(handler).post(any(Runnable.class));
        return handler;
    }

    public static Context createMockedContext() {
        Context context = mock(Context.class);
        when(context.getApplicationContext()).thenReturn(context);
        when(context.getPackageName()).thenReturn("test_package_name");
        final Application application = RuntimeEnvironment.getApplication();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return application.getDatabasePath(invocation.getArgument(0, String.class));
            }
        }).when(context).getDatabasePath((String) any());
        Resources resources = mock(Resources.class);
        when(resources.getDisplayMetrics()).thenReturn(new DisplayMetrics());
        Configuration configuration = new Configuration();
        configuration.locale = new Locale("en", "BY");
        when(resources.getConfiguration()).thenReturn(configuration);
        when(context.getResources()).thenReturn(resources);
        when(context.getFilesDir()).thenReturn(application.getFilesDir());
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            when(context.getNoBackupFilesDir()).thenReturn(application.getFilesDir());
        }
        return context;
    }

    public static Throwable createThrowableMock(final String stacktrace) {
        Throwable throwable = mock(Throwable.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                PrintWriter writer = invocation.getArgument(0);
                writer.write(stacktrace);
                return null;
            }
        }).when(throwable).printStackTrace(any(PrintWriter.class));
        return throwable;
    }

    public static Pair<String, String> pair(String first, String second) {
        return new Pair<String, String>(first, second);
    }

    public static Map<String, String> mapOf(Pair<String, String>... entries) {
        Map<String, String> map = new HashMap<String, String>();
        for (Pair<String, String> entry : entries) {
            map.put(entry.first, entry.second);
        }
        return map;
    }

    public static StartupState.Builder createDefaultStartupStateBuilder() {
        return new StartupState.Builder(
                new CollectingFlags.CollectingFlagsBuilder().build()
        );
    }

    public static StartupState createDefaultStartupState() {
        return createDefaultStartupStateBuilder().build();
    }
}
