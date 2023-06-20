package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class LoggerStorageTest extends CommonTest {

    private final String mApiKey = "mApiKey";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        LoggerStorage.unsetPublicLoggers();
    }

    @After
    public void tearDown() {
        LoggerStorage.unsetPublicLoggers();
    }

    @Test
    public void testGetPublicLogger() {
        PublicLogger logger = LoggerStorage.getOrCreatePublicLogger(mApiKey);
        assertThat(logger).isNotNull();
        assertThat(LoggerStorage.getOrCreatePublicLogger(mApiKey)).isSameAs(logger);
    }

    @Test
    public void testGetPublicLoggerNullApiKey() {
        assertThat(LoggerStorage.getOrCreatePublicLogger(null)).isEqualTo(PublicLogger.getAnonymousInstance());
    }

    @Test
    public void testGetPublicLoggerEmptyApiKey() {
        assertThat(LoggerStorage.getOrCreatePublicLogger("")).isEqualTo(PublicLogger.getAnonymousInstance());
    }

    @Test
    public void testGetAnonymousPublicLogger() {
        assertThat(LoggerStorage.getAnonymousPublicLogger()).isEqualTo(PublicLogger.getAnonymousInstance());
    }
}
