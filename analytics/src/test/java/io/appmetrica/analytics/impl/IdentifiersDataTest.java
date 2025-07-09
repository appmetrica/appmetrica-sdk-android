package io.appmetrica.analytics.impl;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import io.appmetrica.analytics.impl.startup.Constants;
import io.appmetrica.analytics.impl.startup.StartupRequiredUtils;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kotlin.jvm.functions.Function0;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class IdentifiersDataTest extends CommonTest {

    @Rule
    public final MockedStaticRule<StartupRequiredUtils> sStartupRequiredUtils = new MockedStaticRule<>(StartupRequiredUtils.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFilled() {
        DataResultReceiver receiver = new DataResultReceiver(new Handler(Looper.getMainLooper()), mock(DataResultReceiver.Receiver.class));
        ArrayList<String> identifiers = new ArrayList<String>(Arrays.asList("uuid", "deviceid"));
        Map<String, String> clientClids = new HashMap<String, String>();
        clientClids.put("clid0", "0");
        boolean forseRefreshConfiguration = true;
        IdentifiersData identifiersData = new IdentifiersData(identifiers, clientClids, receiver, forseRefreshConfiguration);
        Parcel parcel = Parcel.obtain();
        identifiersData.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        IdentifiersData fromParcel = IdentifiersData.CREATOR.createFromParcel(parcel);
        assertThat(fromParcel.getIdentifiersList()).containsExactlyElementsOf(identifiers);
        assertThat(fromParcel.getResultReceiver()).isNotNull();
        assertThat(fromParcel.getClidsFromClientForVerification()).isEqualTo(clientClids);
        assertThat(fromParcel.isForceRefreshConfiguration()).isTrue();
    }

    @Test
    public void testParcelNulls() {
        IdentifiersData identifiersData = new IdentifiersData(null, new HashMap<String, String>(), null, false);
        Parcel parcel = Parcel.obtain();
        identifiersData.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        IdentifiersData fromParcel = IdentifiersData.CREATOR.createFromParcel(parcel);
        assertThat(fromParcel.getIdentifiersList()).isNull();
        assertThat(fromParcel.getResultReceiver()).isNull();
        assertThat(fromParcel.getClidsFromClientForVerification()).isNotNull().isEmpty();
        assertThat(fromParcel.isForceRefreshConfiguration()).isFalse();
    }

    @Test
    public void isStartupConsistentConsistent() {
        final List<String> identifiers = Arrays.asList(Constants.StartupParamsCallbackKeys.DEVICE_ID, Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH);
        final Map<String, String> clids = new HashMap<>();
        clids.put("clid0", "0");
        IdentifiersData data = new IdentifiersData(identifiers, clids, null, true);
        final StartupState startupState = mock(StartupState.class);
        when(StartupRequiredUtils.containsIdentifiers(eq(startupState), eq(identifiers), eq(clids), any(Function0.class))).thenReturn(true);
        assertThat(data.isStartupConsistent(startupState)).isTrue();
    }

    @Test
    public void isStartupConsistentNotConsistent() {
        final List<String> identifiers = Arrays.asList(Constants.StartupParamsCallbackKeys.DEVICE_ID, Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH);
        final Map<String, String> clids = new HashMap<>();
        clids.put("clid0", "0");
        IdentifiersData data = new IdentifiersData(identifiers, clids, null, true);
        final StartupState startupState = mock(StartupState.class);
        when(StartupRequiredUtils.containsIdentifiers(eq(startupState), eq(identifiers), eq(clids), any(Function0.class))).thenReturn(false);
        assertThat(data.isStartupConsistent(startupState)).isFalse();
    }
}
