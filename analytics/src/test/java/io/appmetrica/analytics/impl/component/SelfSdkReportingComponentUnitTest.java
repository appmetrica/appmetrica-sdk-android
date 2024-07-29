package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.internal.CounterConfigurationReporterType;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SelfSdkReportingComponentUnitTest extends ComponentUnitBaseTest {

    private SelfSdkReportingComponentUnit mSelfSdkReportingComponentUnit;

    @Mock
    private ComponentUnitFieldsFactory mFieldsFactory;
    @Rule
    public final GlobalServiceLocatorRule mGlobalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        super.init();
        mSelfSdkReportingComponentUnit = (SelfSdkReportingComponentUnit) mComponentUnit;
    }

    @Override
    protected void initCustomFields() {
        // do nothing
    }

    @Override
    public ComponentUnitFieldsFactory createFieldsFactory() {
        return mFieldsFactory;
    }

    @Override
    protected ComponentUnit createComponentUnit() {
        return new SelfSdkReportingComponentUnit(
                mContext,
                mComponentId,
                mAppEnvironmentProvider,
                mTimePassedChecker,
                mFieldsFactory
        );
    }

    @Override
    @Test
    public void testGetReporterType() {
        assertThat(mSelfSdkReportingComponentUnit.getReporterType()).isEqualTo(CounterConfigurationReporterType.SELF_SDK);
    }
}
