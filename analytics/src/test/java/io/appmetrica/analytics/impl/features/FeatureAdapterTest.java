package io.appmetrica.analytics.impl.features;

import android.content.pm.FeatureInfo;
import android.os.Build;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class FeatureAdapterTest extends CommonTest {

    private int mSdkIntBackup;

    @Before
    public void setUp() {
        mSdkIntBackup = Build.VERSION.SDK_INT;
    }

    @After
    public void tearDown() {
        TestUtils.setSdkInt(mSdkIntBackup);
    }

    @Test
    public void testRequiredFeature() {
        FeatureAdapter adapter = new FeatureAdapter() {
            @Override
            public FeatureDescription adoptFeature(FeatureInfo feature) {
                return null;
            }
        };
        FeatureInfo info = new FeatureInfo();
        info.flags |= FeatureInfo.FLAG_REQUIRED;
        assertThat(adapter.isRequired(info)).isTrue();
    }

    @Test
    public void testNotRequiredFeature() {
        FeatureAdapter adapter = new FeatureAdapter() {
            @Override
            public FeatureDescription adoptFeature(FeatureInfo feature) {
                return null;
            }
        };
        FeatureInfo info = new FeatureInfo();
        info.flags &= ~FeatureInfo.FLAG_REQUIRED;
        assertThat(adapter.isRequired(info)).isFalse();
    }

    @Test
    public void testOpenGL() {
        final int glVersion = 100;
        FeatureInfo info = new FeatureInfo();
        info.reqGlEsVersion = glVersion;
        FeatureAdapter adapter = spy(new FeatureAdapter() {

            protected FeatureDescription adoptFeature(FeatureInfo feature) {
                return null;
            }
        });
        FeatureDescription description = adapter.adapt(info);
        assertThat(description.getVersion()).isEqualTo(glVersion);
        assertThat(description.getName()).isEqualTo(FeatureDescription.OPEN_GL_FEATURE);
        assertThat(description.isRequired()).isFalse();
        verify(adapter, never()).adoptFeature(any(FeatureInfo.class));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void testStrangeOpenGL() {
        TestUtils.setSdkInt(24);
        final int version = 100;
        FeatureInfo info = new FeatureInfo();
        info.reqGlEsVersion = FeatureInfo.GL_ES_VERSION_UNDEFINED;
        info.version = version;
        FeatureAdapter adapter = spy(FeatureAdapter.Factory.create());
        FeatureDescription description = adapter.adapt(info);
        assertThat(description.getVersion()).isEqualTo(version);
        assertThat(description.getName()).isNull();
        assertThat(description.isRequired()).isFalse();
        verify(adapter, times(1)).adoptFeature(any(FeatureInfo.class));
    }

    @Config(sdk = Build.VERSION_CODES.N)
    @Test
    public void testOnAndroidN() {
        TestUtils.setSdkInt(24);
        assertThat(FeatureAdapter.Factory.create()).isExactlyInstanceOf(FeatureAdapter.FeatureAdapterWithVersion.class);
    }

    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    @Test
    public void testOnPreN() {
        TestUtils.setSdkInt(21);
        assertThat(FeatureAdapter.Factory.create()).isExactlyInstanceOf(FeatureAdapter.FeatureAdapterWithoutVersion.class);
    }

    @Config(sdk = Build.VERSION_CODES.N)
    @Test
    public void testFeatureWithVersion() {
        TestUtils.setSdkInt(24);
        FeatureInfo info = new FeatureInfo();
        info.name = UUID.randomUUID().toString();
        info.flags |= FeatureInfo.FLAG_REQUIRED;
        info.version = 25;
        FeatureDescription description = FeatureAdapter.Factory.create().adapt(info);
        assertThat(description.getName()).isEqualTo(info.name);
        assertThat(description.isRequired()).isTrue();
        assertThat(description.getVersion()).isEqualTo(info.version);
    }

    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    @Test
    public void testFeatureWithoutVersion() {
        TestUtils.setSdkInt(21);
        FeatureInfo info = new FeatureInfo();
        info.name = UUID.randomUUID().toString();
        info.flags |= FeatureInfo.FLAG_REQUIRED;
        FeatureDescription description = FeatureAdapter.Factory.create().adapt(info);
        assertThat(description.getName()).isEqualTo(info.name);
        assertThat(description.isRequired()).isTrue();
        assertThat(description.getVersion()).isEqualTo(-1);
    }
}
