package io.appmetrica.analytics.impl.referrer.service;

import android.content.Context;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReferrerHolderTest extends CommonTest {

    @Mock
    private ReferrerListenerNotifier mListener;
    @Mock
    private ReferrerListenerNotifier mAnotherListener;
    @Mock
    private VitalCommonDataProvider vitalCommonDataProvider;
    private ReferrerAggregator referrerAggregator;
    @Rule
    public final MockedConstructionRule<ReferrerAggregator> referrerAggregatorConstructionRule = new MockedConstructionRule<>(ReferrerAggregator.class, new MockedConstruction.MockInitializer<ReferrerAggregator>() {
        @Override
        public void prepare(ReferrerAggregator mock, MockedConstruction.Context context) throws Throwable {
            referrerAggregator = mock;
        }
    });

    private Context context;
    private final ReferrerInfo mReferrerFromServices = new ReferrerInfo("referrer from services", 100, 200, ReferrerInfo.Source.GP);
    private final ReferrerInfo mNewReferrerFromServices = new ReferrerInfo("new referrer from play services", 110, 220, ReferrerInfo.Source.HMS);

    private ReferrerHolder mHolder;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        when(vitalCommonDataProvider.getReferrer()).thenReturn(mReferrerFromServices);
    }

    @Test
    public void retrieveReferrerChecked() {
        when(vitalCommonDataProvider.getReferrerChecked()).thenReturn(true);
        mHolder = new ReferrerHolder(context, vitalCommonDataProvider);
        mHolder.retrieveReferrerIfNeeded();
        assertThat(referrerAggregatorConstructionRule.getConstructionMock().constructed()).isEmpty();
    }

    @Test
    public void retrieveReferrerNotChecked() {
        when(vitalCommonDataProvider.getReferrerChecked()).thenReturn(false);
        mHolder = new ReferrerHolder(context, vitalCommonDataProvider);
        mHolder.retrieveReferrerIfNeeded();
        verify(referrerAggregator).retrieveReferrer();
        assertThat(referrerAggregatorConstructionRule.getArgumentInterceptor().flatArguments()).containsExactly(context, mHolder);
    }

    @Test
    public void initialReferrerIsNull() {
        when(vitalCommonDataProvider.getReferrer()).thenReturn(null);
        mHolder = new ReferrerHolder(context, vitalCommonDataProvider);
        assertThat(mHolder.getReferrerInfo()).isNull();
    }

    @Test
    public void initialStateIsHasFromPlayServices() {
        mHolder = new ReferrerHolder(context, vitalCommonDataProvider);
        assertThat(mHolder.getReferrerInfo()).isEqualTo(mReferrerFromServices);
    }

    @Test
    public void storeReferrer() {
        mHolder = new ReferrerHolder(context, vitalCommonDataProvider);
        mHolder.subscribe(mListener);
        mHolder.storeReferrer(mNewReferrerFromServices);
        verify(vitalCommonDataProvider).setReferrerChecked(true);
        verify(mListener).notifyIfNeeded(mNewReferrerFromServices);
        assertThat(mHolder.getReferrerInfo()).isEqualTo(mNewReferrerFromServices);
    }

    @Test
    public void storeNullReferrer() {
        mHolder = new ReferrerHolder(context, vitalCommonDataProvider);
        mHolder.subscribe(mListener);
        clearInvocations(mListener);
        mHolder.storeReferrer(null);
        verify(vitalCommonDataProvider).setReferrer(null);
        verify(vitalCommonDataProvider).setReferrerChecked(true);
        verify(mListener).notifyIfNeeded(null);
        assertThat(mHolder.getReferrerInfo()).isNull();
    }

    @Test
    public void subscribeSeveralListenersBeforeReceivingReferrer() {
        when(vitalCommonDataProvider.getReferrerChecked()).thenReturn(false);
        mHolder = new ReferrerHolder(context, vitalCommonDataProvider);
        mHolder.subscribe(mListener);
        mHolder.subscribe(mAnotherListener);
        mHolder.storeReferrer(mNewReferrerFromServices);
        verify(mListener).notifyIfNeeded(mNewReferrerFromServices);
        verify(mAnotherListener).notifyIfNeeded(mNewReferrerFromServices);
    }

    @Test
    public void subscribeSeveralListenersAfterReceivingReferrer() {
        when(vitalCommonDataProvider.getReferrerChecked()).thenReturn(false);
        mHolder = new ReferrerHolder(context, vitalCommonDataProvider);
        mHolder.storeReferrer(mNewReferrerFromServices);
        mHolder.subscribe(mListener);
        mHolder.subscribe(mAnotherListener);
        verify(mListener).notifyIfNeeded(mNewReferrerFromServices);
        verify(mAnotherListener).notifyIfNeeded(mNewReferrerFromServices);
    }

    @Test
    public void notifyListenerReferrerInfoIsNullReferrerChecked() {
        when(vitalCommonDataProvider.getReferrerChecked()).thenReturn(true);
        when(vitalCommonDataProvider.getReferrer()).thenReturn(null);
        mHolder = new ReferrerHolder(context, vitalCommonDataProvider);
        assertThat(mHolder.getReferrerInfo()).isNull();
        mHolder.subscribe(mListener);
        verify(mListener).notifyIfNeeded(null);
    }

    @Test
    public void notifyListenerReferrerInfoIsNullReferrerNotChecked() {
        when(vitalCommonDataProvider.getReferrerChecked()).thenReturn(false);
        when(vitalCommonDataProvider.getReferrer()).thenReturn(null);
        mHolder = new ReferrerHolder(context, vitalCommonDataProvider);
        assertThat(mHolder.getReferrerInfo()).isNull();
        mHolder.subscribe(mListener);
        verify(mListener, never()).notifyIfNeeded(nullable(ReferrerInfo.class));
    }

    @Test
    public void notifyListenerReferrerInfoIsNotNullReferrerChecked() {
        when(vitalCommonDataProvider.getReferrerChecked()).thenReturn(true);
        mHolder = new ReferrerHolder(context, vitalCommonDataProvider);
        assertThat(mHolder.getReferrerInfo()).isEqualTo(mReferrerFromServices);
        mHolder.subscribe(mListener);
        verify(mListener).notifyIfNeeded(mReferrerFromServices);
    }

    @Test
    public void notifyListenerReferrerInfoIsNotNullReferrerNotChecked() {
        when(vitalCommonDataProvider.getReferrerChecked()).thenReturn(false);
        mHolder = new ReferrerHolder(context, vitalCommonDataProvider);
        assertThat(mHolder.getReferrerInfo()).isEqualTo(mReferrerFromServices);
        mHolder.subscribe(mListener);
        verify(mListener, never()).notifyIfNeeded(nullable(ReferrerInfo.class));
    }
}
