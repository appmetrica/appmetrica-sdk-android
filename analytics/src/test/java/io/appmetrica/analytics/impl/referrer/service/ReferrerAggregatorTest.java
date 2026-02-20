package io.appmetrica.analytics.impl.referrer.service;

import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReferrerAggregatorTest extends CommonTest {

    @Mock
    private ReferrerHolder referrerHolder;
    @Mock
    private ReferrerRetrieverWrapper googleReferrerRetriever;
    @Mock
    private HuaweiReferrerRetriever huaweiReferrerRetriever;
    @Mock
    private ReferrerValidityChecker referrerValidityChecker;
    @Captor
    private ArgumentCaptor<ReferrerReceivedListener> googleListenerCaptor;
    @Captor
    private ArgumentCaptor<ReferrerReceivedListener> huaweiListenerCaptor;
    private final ReferrerInfo googleReferrer = new ReferrerInfo("google referrer", 10, 20, ReferrerInfo.Source.GP);
    private final ReferrerInfo huaweiReferrer = new ReferrerInfo("huawei referrer", 15, 25, ReferrerInfo.Source.HMS);

    private ReferrerAggregator aggregator;

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        aggregator = new ReferrerAggregator(
            referrerHolder,
            googleReferrerRetriever,
            huaweiReferrerRetriever,
            referrerValidityChecker
        );
    }

    @Test
    public void googleReferrerResultMatchesInstaller() {
        when(referrerValidityChecker.doesInstallerMatchReferrer(googleReferrer)).thenReturn(true);
        aggregator.retrieveReferrer();
        verify(googleReferrerRetriever).retrieveReferrer(googleListenerCaptor.capture());
        googleListenerCaptor.getValue().onReferrerReceived(googleReferrer);
        verify(referrerHolder).storeReferrer(googleReferrer);
    }

    @Test
    public void googleReferrerErrorMatchesInstaller() {
        when(referrerValidityChecker.doesInstallerMatchReferrer(null)).thenReturn(true);
        aggregator.retrieveReferrer();
        verify(googleReferrerRetriever).retrieveReferrer(googleListenerCaptor.capture());
        googleListenerCaptor.getValue().onReferrerRetrieveError(new RuntimeException());
        verify(referrerHolder).storeReferrer(null);
    }

    @Test
    public void googleReferrerDoesNotMatchInstallerAndHuaweiDoesResult() {
        when(referrerValidityChecker.doesInstallerMatchReferrer(huaweiReferrer)).thenReturn(true);
        aggregator.retrieveReferrer();
        verify(googleReferrerRetriever).retrieveReferrer(googleListenerCaptor.capture());
        googleListenerCaptor.getValue().onReferrerReceived(googleReferrer);
        verify(huaweiReferrerRetriever).retrieveReferrer(huaweiListenerCaptor.capture());
        huaweiListenerCaptor.getValue().onReferrerReceived(huaweiReferrer);
        verify(referrerHolder).storeReferrer(huaweiReferrer);
    }

    @Test
    public void googleReferrerDoesNotMatchInstallerAndHuaweiDoesError() {
        when(referrerValidityChecker.doesInstallerMatchReferrer(null)).thenReturn(true);
        aggregator.retrieveReferrer();
        verify(googleReferrerRetriever).retrieveReferrer(googleListenerCaptor.capture());
        googleListenerCaptor.getValue().onReferrerReceived(googleReferrer);
        verify(huaweiReferrerRetriever).retrieveReferrer(huaweiListenerCaptor.capture());
        huaweiListenerCaptor.getValue().onReferrerRetrieveError(new RuntimeException());
        verify(referrerHolder).storeReferrer(null);
    }

    @Test
    public void installerIsEmptyOnlyGoogleIsFilled() {
        when(referrerValidityChecker.hasReferrer(googleReferrer)).thenReturn(true);
        when(referrerValidityChecker.hasReferrer(huaweiReferrer)).thenReturn(false);
        when(referrerValidityChecker.chooseReferrerFromValid(Collections.singletonList(googleReferrer))).thenReturn(googleReferrer);
        aggregator.retrieveReferrer();
        verify(googleReferrerRetriever).retrieveReferrer(googleListenerCaptor.capture());
        googleListenerCaptor.getValue().onReferrerReceived(googleReferrer);
        verify(huaweiReferrerRetriever).retrieveReferrer(huaweiListenerCaptor.capture());
        huaweiListenerCaptor.getValue().onReferrerReceived(huaweiReferrer);
        verify(referrerValidityChecker).chooseReferrerFromValid(Collections.singletonList(googleReferrer));
        verify(referrerHolder).storeReferrer(googleReferrer);
    }

    @Test
    public void installerIsEmptyOnlyHmsIsFilled() {
        when(referrerValidityChecker.hasReferrer(googleReferrer)).thenReturn(false);
        when(referrerValidityChecker.hasReferrer(huaweiReferrer)).thenReturn(true);
        when(referrerValidityChecker.chooseReferrerFromValid(Collections.singletonList(huaweiReferrer))).thenReturn(huaweiReferrer);
        aggregator.retrieveReferrer();
        verify(googleReferrerRetriever).retrieveReferrer(googleListenerCaptor.capture());
        googleListenerCaptor.getValue().onReferrerReceived(googleReferrer);
        verify(huaweiReferrerRetriever).retrieveReferrer(huaweiListenerCaptor.capture());
        huaweiListenerCaptor.getValue().onReferrerReceived(huaweiReferrer);
        verify(referrerValidityChecker).chooseReferrerFromValid(Collections.singletonList(huaweiReferrer));
        verify(referrerHolder).storeReferrer(huaweiReferrer);
    }

    @Test
    public void installerIsEmptyHasBothReferrersGoogleIsBetterByTime() {
        when(referrerValidityChecker.hasReferrer(googleReferrer)).thenReturn(true);
        when(referrerValidityChecker.hasReferrer(huaweiReferrer)).thenReturn(true);
        when(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(googleReferrer, huaweiReferrer)))
            .thenReturn(googleReferrer);
        aggregator.retrieveReferrer();
        verify(googleReferrerRetriever).retrieveReferrer(googleListenerCaptor.capture());
        googleListenerCaptor.getValue().onReferrerReceived(googleReferrer);
        verify(huaweiReferrerRetriever).retrieveReferrer(huaweiListenerCaptor.capture());
        huaweiListenerCaptor.getValue().onReferrerReceived(huaweiReferrer);
        verify(referrerHolder).storeReferrer(googleReferrer);
    }

    @Test
    public void installerIsEmptyHasBothReferrersHmsIsBetterByTime() {
        when(referrerValidityChecker.hasReferrer(googleReferrer)).thenReturn(true);
        when(referrerValidityChecker.hasReferrer(huaweiReferrer)).thenReturn(true);
        when(referrerValidityChecker.chooseReferrerFromValid(Arrays.asList(googleReferrer, huaweiReferrer)))
            .thenReturn(huaweiReferrer);
        aggregator.retrieveReferrer();
        verify(googleReferrerRetriever).retrieveReferrer(googleListenerCaptor.capture());
        googleListenerCaptor.getValue().onReferrerReceived(googleReferrer);
        verify(huaweiReferrerRetriever).retrieveReferrer(huaweiListenerCaptor.capture());
        huaweiListenerCaptor.getValue().onReferrerReceived(huaweiReferrer);
        verify(referrerHolder).storeReferrer(huaweiReferrer);
    }

    @Test
    public void noReferrers() {
        when(referrerValidityChecker.hasReferrer(googleReferrer)).thenReturn(false);
        when(referrerValidityChecker.hasReferrer(huaweiReferrer)).thenReturn(false);
        when(referrerValidityChecker.chooseReferrerFromValid(new ArrayList<ReferrerInfo>())).thenReturn(null);
        aggregator.retrieveReferrer();
        verify(googleReferrerRetriever).retrieveReferrer(googleListenerCaptor.capture());
        googleListenerCaptor.getValue().onReferrerReceived(googleReferrer);
        verify(huaweiReferrerRetriever).retrieveReferrer(huaweiListenerCaptor.capture());
        huaweiListenerCaptor.getValue().onReferrerReceived(huaweiReferrer);
        verify(referrerValidityChecker).chooseReferrerFromValid(new ArrayList<ReferrerInfo>());
        verify(referrerHolder).storeReferrer(null);
    }
}
