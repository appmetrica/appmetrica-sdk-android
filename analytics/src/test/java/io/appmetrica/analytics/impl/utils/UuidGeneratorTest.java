package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class UuidGeneratorTest extends CommonTest {

    private final UuidGenerator uuidGenerator = new UuidGenerator();

    @Test
    public void testGenerateUuid() {
        String uuid = uuidGenerator.generateUuid();
        assertThat(uuid).isNotEmpty();
        assertThat(uuid).matches("[0-9a-f]{32}");
    }
}
