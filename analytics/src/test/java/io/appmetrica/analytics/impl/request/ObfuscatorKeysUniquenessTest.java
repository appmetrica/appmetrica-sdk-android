package io.appmetrica.analytics.impl.request;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObfuscatorKeysUniquenessTest extends CommonTest {

    @Test
    public void testKeysUniqueness() {
        Collection<String> keys = new Obfuscator().getObfuscationKeys();
        Set<String> hashSet = new HashSet<String>();
        Set<String> duplicatedKeys = new HashSet<String>();
        for (String key : keys) {
            if (!hashSet.add(key)) {
                duplicatedKeys.add(key);
            }
        }
        assertThat(duplicatedKeys)
            .as("Detected obfuscated keys duplication")
            .isEmpty();
    }

}
