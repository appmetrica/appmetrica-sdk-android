package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.internal.CounterConfigurationReporterType;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentUnitTest extends ComponentUnitBaseTest {

    @Mock
    private ComponentUnitFieldsFactory mFieldsFactory;
    @Rule
    public final GlobalServiceLocatorRule mGlobalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        super.init();
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
        return new ComponentUnit(
            mContext,
            mComponentId,
            mAppEnvironmentProvider,
            mTimePassedChecker,
            mFieldsFactory,
            mReporterArguments
        );
    }

    @Override
    @Test
    public void testGetReporterType() {
        assertThat(mComponentUnit.getReporterType()).isEqualTo(CounterConfigurationReporterType.MANUAL);
    }
}
