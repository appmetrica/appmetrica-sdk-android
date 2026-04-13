package io.appmetrica.analytics.impl.db.state.converter;

import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.CacheControl;
import io.appmetrica.gradle.testutils.CommonTest;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

import io.appmetrica.gradle.testutils.assertions.Assertions;
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions;

public class CacheControlConverterTest extends CommonTest {

    private CacheControlConverter mCacheControlConverter;

    private final long lastKnownLocationTtl = 13000L;

    @Before
    public void setUp() throws Exception {
        mCacheControlConverter = new CacheControlConverter();
    }

    @Test
    public void testConvertToProto() throws Exception {
        CacheControl cacheControl = new CacheControl(lastKnownLocationTtl);
        StartupStateProtobuf.StartupState.CacheControl nano = mCacheControlConverter.fromModel(cacheControl);

        ObjectPropertyAssertions<StartupStateProtobuf.StartupState.CacheControl> assertions =
            Assertions.INSTANCE.ObjectPropertyAssertions(nano)
                .withFinalFieldOnly(false);

        assertions.checkField("lastKnownLocationTtl", lastKnownLocationTtl);

        assertions.checkAll();
    }

    @Test
    public void testToModel() throws Exception {
        StartupStateProtobuf.StartupState.CacheControl nano = new StartupStateProtobuf.StartupState.CacheControl();
        nano.lastKnownLocationTtl = lastKnownLocationTtl;

        ObjectPropertyAssertions<CacheControl> assertions = Assertions.INSTANCE.ObjectPropertyAssertions(
            mCacheControlConverter.toModel(nano)
        );

        assertions.checkField("lastKnownLocationTtl", lastKnownLocationTtl);

        assertions.checkAll();
    }

    @Test
    public void testDefaultToModel() throws Exception {
        ObjectPropertyAssertions<CacheControl> assertions = Assertions.INSTANCE.ObjectPropertyAssertions(
            mCacheControlConverter.toModel(new StartupStateProtobuf.StartupState.CacheControl())
        );

        assertions.checkField("lastKnownLocationTtl", TimeUnit.SECONDS.toMillis(10));

        assertions.checkAll();
    }
}
