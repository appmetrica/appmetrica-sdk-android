package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void getOrCreateMainPublicLogger() {
        PublicLogger logger = LoggerStorage.getOrCreateMainPublicLogger(mApiKey);
        assertThat(logger).isNotNull();
        assertThat(LoggerStorage.getOrCreateMainPublicLogger(mApiKey)).isSameAs(logger);
        assertThat(LoggerStorage.getOrCreatePublicLogger(mApiKey)).isSameAs(logger);
    }

    @Test
    public void getMainPublicOrAnonymousLogger() {
        PublicLogger logger = LoggerStorage.getOrCreateMainPublicLogger(mApiKey);
        assertThat(LoggerStorage.getMainPublicOrAnonymousLogger()).isSameAs(logger);
    }

    @Test
    public void getMainPublicOrAnonymousLoggerIfNotCreated() {
        assertThat(LoggerStorage.getMainPublicOrAnonymousLogger()).isSameAs(PublicLogger.getAnonymousInstance());
    }
}
