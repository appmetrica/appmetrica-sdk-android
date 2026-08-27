package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class ReporterComponentUnitTest extends ComponentUnitBaseTest {

    @Mock
    private DataSendingRestrictionControllerImpl dataSendingRestrictionController;
    @Mock
    private CounterConfiguration mCounterConfiguration;
    @Mock
    private ComponentUnitFieldsFactory mFieldsFactory;
    private ReporterComponentUnit mReporterComponentUnit;

    @Rule
    public final GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        super.init();
        mReporterComponentUnit = (ReporterComponentUnit) mComponentUnit;
    }

    @Test
    public void testUpdateConfigNonNullDataSendingEnabled() {
        doReturn(true).when(mCounterConfiguration).getDataSendingEnabled();
        mReporterComponentUnit.updateSdkConfig(new CommonArguments.ReporterArguments(mCounterConfiguration, null));
        verify(dataSendingRestrictionController).setEnabledFromSharedReporter(mApiKey, true);
    }

    @Test
    public void testUpdateConfigNullDataSendingEnabled() {
        doReturn(null).when(mCounterConfiguration).getDataSendingEnabled();
        mReporterComponentUnit.updateSdkConfig(new CommonArguments.ReporterArguments(mCounterConfiguration, null));
        verify(dataSendingRestrictionController).setEnabledFromSharedReporter(mApiKey, null);
    }

    @Override
    public void testGetReporterType() {
        assertThat(mReporterComponentUnit.getReporterType()).isEqualTo(CounterConfigurationReporterType.MANUAL);
    }

    @Override
    protected void initCustomFields() {
        // do nothing
    }

    @Override
    protected ComponentUnit createComponentUnit() {
        return new ReporterComponentUnit(
            mContext,
            mComponentId,
            mAppEnvironmentProvider,
            mTimePassedChecker,
            mFieldsFactory,
            dataSendingRestrictionController,
            mReporterArguments
        );
    }

    @Override
    protected ComponentUnitFieldsFactory createFieldsFactory() {
        return mFieldsFactory;
    }
}
