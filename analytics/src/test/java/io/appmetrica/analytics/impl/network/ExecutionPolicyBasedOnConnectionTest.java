package io.appmetrica.analytics.impl.network;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.PhoneUtils;
import io.appmetrica.analytics.impl.utils.IConnectionTypeProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ExecutionPolicyBasedOnConnectionTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "Do not should start task in network {0}? - {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {PhoneUtils.NetworkType.OFFLINE, false},
                {PhoneUtils.NetworkType.UNDEFINED, true},
                {PhoneUtils.NetworkType.CELL, true},
                {PhoneUtils.NetworkType.WIFI, true},
        });
    }

    @Mock
    private IConnectionTypeProvider mConnectionTypeProvider;
    @InjectMocks
    private ExecutionPolicyBasedOnConnection mPolicy = new ExecutionPolicyBasedOnConnection(RuntimeEnvironment.getApplication());

    @NonNull
    private final PhoneUtils.NetworkType mNetworkType;
    private final boolean mCanBeExecuted;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doReturn(mNetworkType).when(mConnectionTypeProvider).getConnectionType(any(Context.class));
    }

    public ExecutionPolicyBasedOnConnectionTest(@NonNull PhoneUtils.NetworkType networkType, boolean canBeExecuted) {
        mNetworkType = networkType;
        mCanBeExecuted = canBeExecuted;
    }

    @Test
    public void test() {
        assertThat(mPolicy.canBeExecuted()).isEqualTo(mCanBeExecuted);
    }

}
