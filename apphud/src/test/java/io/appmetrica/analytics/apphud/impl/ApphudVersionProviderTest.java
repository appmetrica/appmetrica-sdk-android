package io.appmetrica.analytics.apphud.impl;

import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ApphudVersionProviderTest extends CommonTest {

    private static final String BUILD_CONFIG_CLASS = "com.apphud.sdk.BuildConfig";

    private final Class buildConfigClass = MockBuildConfig.class;

    @Rule
    public MockedStaticRule<ReflectionUtils> reflectionUtilsRule =
        new MockedStaticRule<>(ReflectionUtils.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() {
        MockBuildConfig.VERSION_NAME = null;
    }

    @Test
    public void getApphudVersionReturnsV3WhenVersionIs3() {
        setupVersionString("3.0.0");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V3);
    }

    @Test
    public void getApphudVersionReturnsV3WhenVersionIs3_1_0() {
        setupVersionString("3.1.0");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V3);
    }

    @Test
    public void getApphudVersionReturnsV3WhenVersionIs4() {
        setupVersionString("4.0.0");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V3);
    }

    @Test
    public void getApphudVersionReturnsV2WhenVersionIs2() {
        setupVersionString("2.0.0");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V2);
    }

    @Test
    public void getApphudVersionReturnsV2WhenVersionIs2_5_3() {
        setupVersionString("2.5.3");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V2);
    }

    @Test
    public void getApphudVersionReturnsV2WhenVersionIs1() {
        setupVersionString("1.0.0");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V2);
    }

    @Test
    public void getApphudVersionReturnsV3WhenVersionIsNull() {
        when(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(null);

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V3);
    }

    @Test
    public void getApphudVersionReturnsV3WhenBuildConfigClassNotFound() {
        when(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(null);

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V3);
    }

    @Test
    public void getApphudVersionReturnsV3WhenVersionStringIsInvalid() {
        setupVersionString("invalid");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V3);
    }

    @Test
    public void getApphudVersionReturnsV3WhenVersionStringIsEmpty() {
        setupVersionString("");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V3);
    }

    @Test
    public void getApphudVersionHandlesVersionWithBetaSuffix() {
        setupVersionString("3.0.0-beta");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V3);
    }

    @Test
    public void getApphudVersionHandlesVersionWithAlphaSuffix() {
        setupVersionString("2.5.0-alpha");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V2);
    }

    @Test
    public void getApphudVersionHandlesMajorVersionWithDash() {
        setupVersionString("3-beta");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V3);
    }

    @Test
    public void getApphudVersionHandlesVersionWithoutDots() {
        setupVersionString("3");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V3);
    }

    @Test
    public void getApphudVersionReturnsV3WhenVersionHasNonNumericMajor() {
        setupVersionString("abc.0.0");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V3);
    }

    @Test
    public void getApphudVersionHandlesEdgeCaseVersion2_9_9() {
        setupVersionString("2.9.9");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V2);
    }

    @Test
    public void getApphudVersionHandlesEdgeCaseVersion3_0_0_RC1() {
        setupVersionString("3.0.0-RC1");

        ApphudVersion result = ApphudVersionProvider.getApphudVersion();

        assertThat(result).isEqualTo(ApphudVersion.APPHUD_V3);
    }

    private void setupVersionString(String version) {
        when(ReflectionUtils.findClass(BUILD_CONFIG_CLASS)).thenReturn(buildConfigClass);
        MockBuildConfig.VERSION_NAME = version;
    }
}
