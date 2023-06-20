package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.CounterConfiguration;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.StatisticsRestrictionControllerImpl;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ReporterComponentUnitTest extends ComponentUnitBaseTest {

    @Mock
    private StatisticsRestrictionControllerImpl mStatisticsRestrictionController;
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

    @After
    public void tearDown() throws Exception {
        Field instance = DatabaseStorageFactory.class.getDeclaredField("sStorageFactory");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    public void testUpdateConfigNonNullStatisticsSending() {
        doReturn(true).when(mCounterConfiguration).getStatisticsSending();
        mReporterComponentUnit.updateSdkConfig(new CommonArguments.ReporterArguments(mCounterConfiguration, null));
        verify(mStatisticsRestrictionController).setEnabledFromSharedReporter(mApiKey, true);
    }

    @Test
    public void testUpdateConfigNullStatisticsSending() {
        doReturn(null).when(mCounterConfiguration).getStatisticsSending();
        mReporterComponentUnit.updateSdkConfig(new CommonArguments.ReporterArguments(mCounterConfiguration, null));
        verify(mStatisticsRestrictionController).setEnabledFromSharedReporter(mApiKey, null);
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
                mStatisticsRestrictionController
        );
    }

    @Override
    protected ComponentUnitFieldsFactory createFieldsFactory() {
        return mFieldsFactory;
    }
}
