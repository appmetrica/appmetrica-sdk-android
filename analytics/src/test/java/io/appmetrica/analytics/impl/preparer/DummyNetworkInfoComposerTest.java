package io.appmetrica.analytics.impl.preparer;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class DummyNetworkInfoComposerTest extends CommonTest {

    private final Integer inputConnectionType;
    private final String inputCellularConnectionType;
    private final String inputWifiData;
    private final String inputCellData;

    public DummyNetworkInfoComposerTest(Integer inputConnectionType,
                                        String inputCellularConnectionType,
                                        String inputWifiData,
                                        String inputCellData) {
        this.inputConnectionType = inputConnectionType;
        this.inputCellularConnectionType = inputCellularConnectionType;
        this.inputWifiData = inputWifiData;
        this.inputCellData = inputCellData;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {4769, "some type", "[]", "{}"},
            {0, "", "", ""},
            {null, null, null, null}
        });
    }

    private DummyNetworkInfoComposer composer;

    @Before
    public void setUp() throws Exception {
        composer = new DummyNetworkInfoComposer();
    }

    @Test
    public void getNetworkInfo() {
        assertThat(composer.getNetworkInfo(
            inputConnectionType,
            inputCellularConnectionType
        )).isNull();
    }
}
