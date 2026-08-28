package io.appmetrica.analytics.impl.preparer;

import io.appmetrica.analytics.impl.protobuf.backend.EventProto;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NoneEncodingTypeProviderTest extends CommonTest {

    private final NoneEncodingTypeProvider mProvider = new NoneEncodingTypeProvider();

    @Test
    public void testGetEncodingType() {
        assertThat(mProvider.getEncodingType()).isEqualTo(EventProto.ReportMessage.Session.Event.NONE);
    }
}
