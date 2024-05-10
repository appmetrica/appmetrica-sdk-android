package io.appmetrica.analytics.impl.component.clients;

import android.os.Bundle;
import android.os.ResultReceiver;
import io.appmetrica.analytics.impl.ClientIdentifiersHolder;
import io.appmetrica.analytics.impl.ClientIdentifiersProvider;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.DataResultReceiver;
import io.appmetrica.analytics.impl.client.ClientConfigurationTestUtils;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommonArgumentsTestUtils;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CommutationClientUnitTest extends CommonTest {

    @Mock
    CommutationDispatcherComponent mCommutationDispatcherComponent;
    @Mock
    DataResultReceiver mResultReceiver;
    @Mock
    ClientIdentifiersProvider mClientIdentifiersProvider;
    @Mock
    ClientIdentifiersHolder mClientIdentifiersHolder;

    CommonArguments mClientConfiguration;
    CommutationClientUnit mClientUnit;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mCommutationDispatcherComponent.getClientIdentifiersProvider()).thenReturn(mClientIdentifiersProvider);
        when(mClientIdentifiersProvider.createClientIdentifiersHolder(nullable(Map.class))).thenReturn(mClientIdentifiersHolder);
        mClientConfiguration = CommonArgumentsTestUtils.createMockedArguments(mResultReceiver);
        mClientUnit = new CommutationClientUnit(RuntimeEnvironment.getApplication(), mCommutationDispatcherComponent, mClientConfiguration);
    }

    @Test
    public void testConstructor() {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(mClientUnit.getContext()).isEqualTo(RuntimeEnvironment.getApplication());
        softAssertions.assertThat(mClientUnit.getComponent()).isEqualTo(mCommutationDispatcherComponent);
        softAssertions.assertThat(mClientUnit.getResultReceiver()).isEqualTo(mResultReceiver);
        softAssertions.assertAll();
    }

    @Test
    public void testHandle() {
        CounterReport report = new CounterReport();
        mClientUnit.handle(report, mClientConfiguration);
        verify(mCommutationDispatcherComponent, times(1)).handleReport(report, mClientUnit);
    }

    @Test
    public void testStartupChanged() {
        mClientUnit.onClientIdentifiersChanged(mClientIdentifiersHolder);
        ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(mClientIdentifiersHolder).toBundle(captor.capture());
        verify(mResultReceiver, timeout(1)).send(1, captor.getValue());
    }

    @Test
    public void testResultReceiverNotUpdated() {
        ResultReceiver newReceiver = mock(ResultReceiver.class);
        CommonArguments clientConfiguration = new CommonArguments(ClientConfigurationTestUtils.createStubbedConfiguration(newReceiver));
        mClientUnit.handle(new CounterReport(), clientConfiguration);
        mClientUnit.onClientIdentifiersChanged(mClientIdentifiersHolder);
        verify(mClientIdentifiersHolder).toBundle(any(Bundle.class));
        verify(newReceiver, never()).send(anyInt(), any(Bundle.class));
        verify(mResultReceiver, times(1)).send(eq(1), any(Bundle.class));
    }
}
