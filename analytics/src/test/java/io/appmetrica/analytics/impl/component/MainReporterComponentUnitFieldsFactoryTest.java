package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.referrer.service.OnlyOnceReferrerNotificationFilter;
import io.appmetrica.analytics.impl.referrer.service.ReferrerListenerNotifier;
import io.appmetrica.analytics.impl.utils.executors.ServiceExecutorProvider;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MainReporterComponentUnitFieldsFactoryTest extends ComponentUnitFieldsFactoryTest {

    @Mock
    private MainReporterComponentUnit mMainReporterComponentUnit;
    @Mock
    private ServiceExecutorProvider serviceExecutorProvider;
    @Mock
    private IHandlerExecutor defaultExecutor;
    @Mock
    private ICommonExecutor uiExecutor;
    @Mock
    private EventTriggerProviderCreator eventTriggerProviderCreator;
    private MainReporterComponentUnitFieldsFactory mainReporterComponentUnitFieldsFactory;

    @Before
    public void setUp() {
        super.setUp();
        when(serviceExecutorProvider.getDefaultExecutor()).thenReturn(defaultExecutor);
        when(serviceExecutorProvider.getUiExecutor()).thenReturn(uiExecutor);

        mainReporterComponentUnitFieldsFactory = new MainReporterComponentUnitFieldsFactory(
            mContext,
            mComponentId,
            mSdkConfig,
            mStartupExecutorFactory,
            mStartupState,
            dataSendingStrategy,
            mExecutor,
            mCurrentAppVersion,
            serviceExecutorProvider,
            lifecycleDependentComponentManager,
            eventTriggerProviderCreator
        );
    }

    @Test
    public void testCreateListener() {
        ReferrerListenerNotifier listener = mainReporterComponentUnitFieldsFactory.createReferrerListener(mMainReporterComponentUnit);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(listener.getFilter()).isExactlyInstanceOf(OnlyOnceReferrerNotificationFilter.class);
        softly.assertThat(listener.getReferrerHandledNotifier()).isSameAs(mMainReporterComponentUnit);
        softly.assertThat(listener.getListener()).isExactlyInstanceOf(MainReporterComponentUnit.MainReporterListener.class);
        softly.assertAll();
    }

    @Test
    public void testCreateBillingMonitorWrapper() {
        assertThat(mainReporterComponentUnitFieldsFactory.createBillingMonitorWrapper(mMainReporterComponentUnit)).isNotNull();
    }
}
