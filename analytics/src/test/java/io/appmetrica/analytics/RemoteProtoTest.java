package io.appmetrica.analytics;

import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.InputStream;
import kotlin.io.ByteStreamsKt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test example for discovering client-server proto parsing issues. It is the "Hello world" for
 * fast start of investigation.
 * Just replace test/resources/proto.bin with remote proto message and check it.
 * Usually back-end can provide proto.bin file with some proto message.
 * CrashAndroid.Anr is just an example.
 */
@RunWith(RobolectricTestRunner.class)
public class RemoteProtoTest extends CommonTest {

    @Test
    public void test() throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("proto.bin");
        byte[] data = ByteStreamsKt.readBytes(stream);
        CrashAndroid.Anr message = CrashAndroid.Anr.parseFrom(data);
        assertThat(message.buildId).isNotEmpty();
    }
}
