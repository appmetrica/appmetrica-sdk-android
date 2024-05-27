package io.appmetrica.analytics.logger.common;

import android.content.Context;
import io.appmetrica.analytics.logger.common.impl.MultilineLogger;
import io.appmetrica.analytics.logger.common.internal.BaseReleaseLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class BaseReleaseLoggerTest extends CommonTest {

    private final String packageName = "some_package_name";
    private final String tag = "some_tag";
    private final String logPrefix = "some_prefix";
    private final String message = "some_message";
    private final String arg = "some_arg";
    private final Throwable throwable = new IllegalStateException();

    private MultilineLogger multilineLogger;

    @Rule
    public MockedConstructionRule<MultilineLogger> multilineLoggerRule =
        new MockedConstructionRule<>(
            MultilineLogger.class,
            new MockedConstruction.MockInitializer<MultilineLogger>() {
                @Override
                public void prepare(MultilineLogger mock, MockedConstruction.Context context) {
                    multilineLogger = mock;
                }
            });

    @Mock
    private Context context;

    private BaseReleaseLogger logger;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(context.getPackageName()).thenReturn(packageName);

        logger = new BaseReleaseLogger(tag, logPrefix) {};
        BaseReleaseLogger.init(context);
    }

    @Test
    public void loggerIsDisabledByDefault() {
        logger.info(message, arg);
        logger.warning(message, arg);
        logger.error(message, arg);
        logger.error(throwable, message, arg);

        verifyNoInteractions(multilineLogger);
    }

    @Test
    public void disablingLogger() {
        logger.setEnabled(true);
        logger.setEnabled(false);

        logger.info(message, arg);
        logger.warning(message, arg);
        logger.error(message, arg);
        logger.error(throwable, message, arg);

        verifyNoInteractions(multilineLogger);
    }

    @Test
    public void info() {
        logger.setEnabled(true);
        logger.info(message, arg);

        verify(multilineLogger).info(getLogPrefix(), message, arg);
    }

    @Test
    public void warning() {
        logger.setEnabled(true);
        logger.warning(message, arg);

        verify(multilineLogger).warning(getLogPrefix(), message, arg);
    }

    @Test
    public void error() {
        logger.setEnabled(true);
        logger.error(message, arg);

        verify(multilineLogger).error(getLogPrefix(), message, arg);
    }

    @Test
    public void errorWithThrowable() {
        logger.setEnabled(true);
        logger.error(throwable, message, arg);

        verify(multilineLogger).error(getLogPrefix(), throwable, message, arg);
    }

    @Test
    public void logIfLoggerIsDisabled() {
        logger.setEnabled(false);

        logger.info(message, arg);
        logger.warning(message, arg);
        logger.error(message, arg);
        logger.error(throwable, message, arg);

        verifyNoInteractions(multilineLogger);
    }

    private String getLogPrefix() {
        return "[" + packageName + "] : " + logPrefix;
    }
}
