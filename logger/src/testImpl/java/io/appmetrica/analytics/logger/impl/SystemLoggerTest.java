package io.appmetrica.analytics.logger.impl;

import android.util.Log;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;

public class SystemLoggerTest extends CommonTest {

    @Rule
    public final MockedStaticRule<Log> logRule = new MockedStaticRule<>(Log.class);

    @NonNull
    private final String tag = "some_tag";
    @NonNull
    private final String message = "some_message";
    @NonNull
    private final SystemLogger logger = new SystemLogger(tag);

    @Test
    public void info() {
        logger.info(message);
        logRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                Log.println(Log.INFO, tag, message);
            }
        });
    }

    @Test
    public void infoIfMessageIsNull() {
        logger.info(null);
        logRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                Log.println(Log.INFO, tag, "");
            }
        });
    }

    @Test
    public void warning() {
        logger.warning(message);
        logRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                Log.println(Log.WARN, tag, message);
            }
        });
    }

    @Test
    public void warningIfMessageIsNull() {
        logger.warning(null);
        logRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                Log.println(Log.WARN, tag, "");
            }
        });
    }

    @Test
    public void error() {
        logger.error(message);
        logRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                Log.println(Log.ERROR, tag, message);
            }
        });
    }

    @Test
    public void errorIfMessageIsNull() {
        logger.error(null);
        logRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                Log.println(Log.ERROR, tag, "");
            }
        });
    }
}
