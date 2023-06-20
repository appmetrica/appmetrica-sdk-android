package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kotlin.jvm.functions.Function0;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupUnitIsStartupRequiredTest extends StartupUnitBaseTest {

    @Rule
    public final MockedStaticRule<StartupRequiredUtils> sStartupRequiredUtils = new MockedStaticRule<>(StartupRequiredUtils.class);
    private final Map<String, String> clientClids = new HashMap<String, String>();

    @Before
    public void setUp() {
        super.setup();
        when(mStartupRequestConfig.getClidsFromClient()).thenReturn(clientClids);
    }

    @Test
    public void startupIsNotRequired() {
        StartupState startupState = mock(StartupState.class);
        when(StartupRequiredUtils.isOutdated(startupState)).thenReturn(false);
        when(mConfigurationHolder.getStartupState()).thenReturn(startupState);
        when(StartupRequiredUtils.areMainIdentifiersValid(startupState)).thenReturn(true);
        when(clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(clientClids, startupState, clidsStorage)).thenReturn(true);
        assertThat(mStartupUnit.isStartupRequired()).isFalse();
    }

    @Test
    public void startupIsRequiredBecauseClidsDoNotMatch() {
        StartupState startupState = mock(StartupState.class);
        when(mConfigurationHolder.getStartupState()).thenReturn(startupState);
        when(StartupRequiredUtils.isOutdated(startupState)).thenReturn(false);
        when(StartupRequiredUtils.areMainIdentifiersValid(startupState)).thenReturn(true);
        when(clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(clientClids, startupState, clidsStorage)).thenReturn(false);
        assertThat(mStartupUnit.isStartupRequired()).isTrue();
    }

    @Test
    public void startupIsRequiredBecauseMainIdentifiersArNotValid() {
        StartupState startupState = mock(StartupState.class);
        when(mConfigurationHolder.getStartupState()).thenReturn(startupState);
        when(StartupRequiredUtils.isOutdated(startupState)).thenReturn(false);
        when(StartupRequiredUtils.areMainIdentifiersValid(startupState)).thenReturn(false);
        when(clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(clientClids, startupState, clidsStorage)).thenReturn(true);
        assertThat(mStartupUnit.isStartupRequired()).isTrue();
    }

    @Test
    public void startupIsRequiredBecauseItIsOutdated() {
        StartupState startupState = mock(StartupState.class);
        when(mConfigurationHolder.getStartupState()).thenReturn(startupState);
        when(StartupRequiredUtils.isOutdated(startupState)).thenReturn(true);
        when(StartupRequiredUtils.areMainIdentifiersValid(startupState)).thenReturn(true);
        when(clidsStateChecker.doChosenClidsForRequestMatchLastRequestClids(clientClids, startupState, clidsStorage)).thenReturn(true);
        assertThat(mStartupUnit.isStartupRequired()).isTrue();
    }

    @Test
    public void isStartupRequiredForIdentifiersRequired() {
        StartupState startupState = mock(StartupState.class);
        List<String> identifiers = Arrays.asList("uuid", "device_id");
        Map<String, String> clids = new HashMap<>();
        clids.put("clid0", "0");
        when(mConfigurationHolder.getStartupState()).thenReturn(startupState);
        when(StartupRequiredUtils.containsIdentifiers(eq(startupState), eq(identifiers), eq(clids), any(Function0.class))).thenReturn(false);
        assertThat(mStartupUnit.isStartupRequired(identifiers, clids)).isTrue();
    }

    @Test
    public void isStartupRequiredForIdentifiersNotRequired() {
        StartupState startupState = mock(StartupState.class);
        List<String> identifiers = Arrays.asList("uuid", "device_id");
        Map<String, String> clids = new HashMap<>();
        clids.put("clid0", "0");
        when(mConfigurationHolder.getStartupState()).thenReturn(startupState);
        when(StartupRequiredUtils.containsIdentifiers(eq(startupState), eq(identifiers), eq(clids), any(Function0.class))).thenReturn(true);
        assertThat(mStartupUnit.isStartupRequired(identifiers, clids)).isFalse();
    }
}
