package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SelfDiagnosticReporterStorageTest extends CommonTest {

    @Mock
    private SelfProcessReporter mSelfProcessReporter;
    private Context mContext;
    private SelfDiagnosticReporterStorage mStorage;
    private final String mApiKey = UUID.randomUUID().toString();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        mStorage = new SelfDiagnosticReporterStorage(mContext, mSelfProcessReporter);
    }

    @Test
    public void testCreateReporter() {
        assertThat(mStorage.getOrCreateReporter(mApiKey, CounterConfigurationReporterType.MAIN)).isNotNull();
    }

    @Test
    public void testDoesNotCreateReporterTwice() {
        SelfDiagnosticReporter firstReporter = mStorage.getOrCreateReporter(mApiKey, CounterConfigurationReporterType.MAIN);
        assertThat(mStorage.getOrCreateReporter(mApiKey, CounterConfigurationReporterType.MANUAL)).isEqualTo(firstReporter);
    }

    @Test
    public void testReportersForDifferentApiKeys() {
        SelfDiagnosticReporter firstReporter = mStorage.getOrCreateReporter(mApiKey, CounterConfigurationReporterType.MAIN);
        assertThat(mStorage.getOrCreateReporter(UUID.randomUUID().toString(), CounterConfigurationReporterType.MANUAL)).isNotEqualTo(firstReporter);
    }
}
