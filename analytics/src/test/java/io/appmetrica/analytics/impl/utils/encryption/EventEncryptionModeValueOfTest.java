package io.appmetrica.analytics.impl.utils.encryption;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class EventEncryptionModeValueOfTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "{1} for {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {0, EventEncryptionMode.NONE},
                {1, EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER},
                {2, EventEncryptionMode.AES_VALUE_ENCRYPTION},
                {3, EventEncryptionMode.NONE}
        });
    }

    private final int mModeId;
    private final EventEncryptionMode mExpected;

    public EventEncryptionModeValueOfTest(int modeId, EventEncryptionMode expected) {
        mModeId = modeId;
        mExpected = expected;
    }

    @Test
    public void testValueOf() {
        assertThat(EventEncryptionMode.valueOf(mModeId)).isEqualTo(mExpected);
    }
}
