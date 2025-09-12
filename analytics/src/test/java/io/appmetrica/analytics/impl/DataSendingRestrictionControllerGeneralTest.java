package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class DataSendingRestrictionControllerGeneralTest extends CommonTest {

    private final DataSendingRestrictionControllerImpl.Storage mStorage = mock(DataSendingRestrictionControllerImpl.Storage.class);
    private DataSendingRestrictionControllerImpl mController;

    @Before
    public void setUp() {
        doReturn(null).when(mStorage).readRestrictionFromMainReporter();
        mController = new DataSendingRestrictionControllerImpl(mStorage);
    }

    @Test
    public void testSaveRestrictedInMainReporter() {
        mController.setEnabledFromMainReporter(null);
        verify(mStorage).storeRestrictionFromMainReporter(anyBoolean());
    }

    @Test
    public void testReadRestrictedInMainReporter() {
        verify(mStorage).readRestrictionFromMainReporter();
    }

    @Test
    public void testEnabledFromMainReporterNull() {
        mController.setEnabledFromMainReporter(null);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void testEnabledFromMainReporterWasFalseThenNull() {
        mController.setEnabledFromMainReporter(false);
        assertThat(mController.isRestrictedForSdk()).isTrue();
        mController.setEnabledFromMainReporter(null);
        assertThat(mController.isRestrictedForSdk()).isTrue();
    }

    @Test
    public void testEnabledFromMainReporterWasFalseThenTrue() {
        mController.setEnabledFromMainReporter(false);
        assertThat(mController.isRestrictedForSdk()).isTrue();
        mController.setEnabledFromMainReporter(true);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void testEnabledFromMainReporterWasTrueThenFalse() {
        mController.setEnabledFromMainReporter(true);
        assertThat(mController.isRestrictedForSdk()).isFalse();
        mController.setEnabledFromMainReporter(false);
        assertThat(mController.isRestrictedForSdk()).isTrue();
    }

    @Test
    public void setEnabledFromMainReporterIfNotYetForTrue() {
        mController.setEnabledFromMainReporterIfNotYet(true);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterIsNotYetForFalse() {
        mController.setEnabledFromMainReporterIfNotYet(false);
        assertThat(mController.isRestrictedForSdk()).isTrue();
    }

    @Test
    public void setEnabledFromMainReporterIsNotYetForNull() {
        mController.setEnabledFromMainReporterIfNotYet(null);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterIfNotYetIfTrueAfterNull() {
        mController.setEnabledFromMainReporterIfNotYet(null);
        mController.setEnabledFromMainReporterIfNotYet(true);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterIfNotYestIfFalseAfterNull() {
        mController.setEnabledFromMainReporterIfNotYet(null);
        mController.setEnabledFromMainReporterIfNotYet(false);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterIfNullAfterNull() {
        mController.setEnabledFromMainReporterIfNotYet(null);
        mController.setEnabledFromMainReporterIfNotYet(null);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterIfNotYetTrueAfterTrue() {
        mController.setEnabledFromMainReporterIfNotYet(true);
        mController.setEnabledFromMainReporterIfNotYet(true);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterIfNotYetIfFalseAfterTrue() {
        mController.setEnabledFromMainReporterIfNotYet(true);
        mController.setEnabledFromMainReporterIfNotYet(false);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterIfNotYetIfNullAfterTrue() {
        mController.setEnabledFromMainReporterIfNotYet(true);
        mController.setEnabledFromMainReporterIfNotYet(null);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterIfNotYetIfTrueAfterFalse() {
        mController.setEnabledFromMainReporterIfNotYet(false);
        mController.setEnabledFromMainReporterIfNotYet(true);
        assertThat(mController.isRestrictedForSdk()).isTrue();
    }

    @Test
    public void setEnabledFromMainReporterIfNotYetIfFalseAfterFalse() {
        mController.setEnabledFromMainReporterIfNotYet(false);
        mController.setEnabledFromMainReporterIfNotYet(false);
        assertThat(mController.isRestrictedForSdk()).isTrue();
    }

    @Test
    public void setEnabledFromMainReporterIfNotYetIfNullAfterFalse() {
        mController.setEnabledFromMainReporterIfNotYet(false);
        mController.setEnabledFromMainReporterIfNotYet(null);
        assertThat(mController.isRestrictedForSdk()).isTrue();
    }

    @Test
    public void setEnabledFromMainReporterIfNotYetWithTrueAfterEnabledWithNull() {
        mController.setEnabledFromMainReporter(null);
        mController.setEnabledFromMainReporterIfNotYet(true);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterIfNotYetWithFalseAfterEnabledWithNull() {
        mController.setEnabledFromMainReporter(null);
        mController.setEnabledFromMainReporterIfNotYet(false);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterWithNullAfterEnabledIfNotYetWithNull() {
        mController.setEnabledFromMainReporterIfNotYet(null);
        mController.setEnabledFromMainReporter(null);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterWithTrueAfterEnabledIfNotYetWithNull() {
        mController.setEnabledFromMainReporterIfNotYet(null);
        mController.setEnabledFromMainReporter(true);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterWithFalseAfterEnabledIfNotYetWithNull() {
        mController.setEnabledFromMainReporterIfNotYet(null);
        mController.setEnabledFromMainReporter(false);
        assertThat(mController.isRestrictedForSdk()).isTrue();
    }

    @Test
    public void setEnabledFromMainReporterWithNullAfterEnabledIfNotYetWithTrue() {
        mController.setEnabledFromMainReporterIfNotYet(true);
        mController.setEnabledFromMainReporter(null);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterWithTrueAfterEnabledIfNotYetWithTrue() {
        mController.setEnabledFromMainReporterIfNotYet(true);
        mController.setEnabledFromMainReporter(true);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterWithFalseAfterEnabledIfNotYetWithTrue() {
        mController.setEnabledFromMainReporterIfNotYet(true);
        mController.setEnabledFromMainReporter(false);
        assertThat(mController.isRestrictedForSdk()).isTrue();
    }

    @Test
    public void setEnabledFromMainReporterWithNullAfterEnabledIfNotYetWithFalse() {
        mController.setEnabledFromMainReporterIfNotYet(false);
        mController.setEnabledFromMainReporter(null);
        assertThat(mController.isRestrictedForSdk()).isTrue();
    }

    @Test
    public void setEnabledFromMainReporterWithTrueAfterEnabledIfNotYetWithFalse() {
        mController.setEnabledFromMainReporterIfNotYet(false);
        mController.setEnabledFromMainReporter(true);
        assertThat(mController.isRestrictedForSdk()).isFalse();
    }

    @Test
    public void setEnabledFromMainReporterWithFalseAfterEnabledIfNotYetWithFalse() {
        mController.setEnabledFromMainReporterIfNotYet(false);
        mController.setEnabledFromMainReporter(false);
        assertThat(mController.isRestrictedForSdk()).isTrue();
    }

}
