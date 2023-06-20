package io.appmetrica.analytics.impl.component.remarketing;

import io.appmetrica.analytics.testutils.CommonTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class EventHashSerializerHistoryTest extends CommonTest {

    private byte[] mInput;
    private EventHashes mExpectedValue;
    private String mDescription;

    @ParameterizedRobolectricTestRunner.Parameters(name = "#{index}. Input: {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {
                        new byte[]{8, 0, 16, 0, 24, 0},
                        new EventHashes(),
                        "Empty object v1"
                },
                {
                        new byte[]{8, 1, 16, 100, 24, -56, 1, 32, -112, 78, 32, 1, 32, -96, -115, 6, 32, 100, 32, -24,
                                7, 32, 10, 32, -64, -124, 61},
                        new EventHashes(
                                true,
                                100,
                                200,
                                new int[]{1, 10, 100, 1000, 10000, 100000, 1000000}),
                        "Filled object v1"
                }
        });
    }

    private EventHashesSerializer mEventHashesSerializer;
    private EventHashesConverter mConverter;

    public EventHashSerializerHistoryTest(final byte[] input,
                                          final EventHashes expectedValue,
                                          final String description) {
        mInput = input;
        mExpectedValue = expectedValue;
        mDescription = description;
    }

    @Before
    public void setUp() throws Exception {
        mEventHashesSerializer = new EventHashesSerializer();
        mConverter = new EventHashesConverter();
    }

    @Test
    public void testDeserialization() throws IOException {
        EventHashes actual = mConverter.toModel(mEventHashesSerializer.toState(mInput));
        assertThat(actual).isEqualToComparingFieldByField(mExpectedValue);
    }
}
