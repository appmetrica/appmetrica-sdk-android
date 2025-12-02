package io.appmetrica.analytics.apphud.impl;

import io.appmetrica.analytics.apphud.internal.ApphudWrapper;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ApphudWrapperProviderTest extends CommonTest {

    private static final String APPHUD_V2_WRAPPER_CLASS = "io.appmetrica.analytics.apphudv2.internal.ApphudV2Wrapper";
    private static final String APPHUD_V3_WRAPPER_CLASS = "io.appmetrica.analytics.apphudv3.internal.ApphudV3Wrapper";

    @Mock
    private ApphudWrapper apphudV2Wrapper;
    @Mock
    private ApphudWrapper apphudV3Wrapper;

    @Rule
    public MockedStaticRule<ApphudVersionProvider> apphudVersionProviderRule =
        new MockedStaticRule<>(ApphudVersionProvider.class);

    @Rule
    public MockedStaticRule<ReflectionUtils> reflectionUtilsRule =
        new MockedStaticRule<>(ReflectionUtils.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getApphudWrapperReturnsV3WrapperWhenVersionIsV3() {
        when(ApphudVersionProvider.getApphudVersion()).thenReturn(ApphudVersion.APPHUD_V3);
        when(ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
            APPHUD_V3_WRAPPER_CLASS,
            ApphudWrapper.class
        )).thenReturn(apphudV3Wrapper);

        ApphudWrapper result = ApphudWrapperProvider.getApphudWrapper();

        assertThat(result).isEqualTo(apphudV3Wrapper);
    }

    @Test
    public void getApphudWrapperReturnsV2WrapperWhenVersionIsV2() {
        when(ApphudVersionProvider.getApphudVersion()).thenReturn(ApphudVersion.APPHUD_V2);
        when(ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
            APPHUD_V2_WRAPPER_CLASS,
            ApphudWrapper.class
        )).thenReturn(apphudV2Wrapper);

        ApphudWrapper result = ApphudWrapperProvider.getApphudWrapper();

        assertThat(result).isEqualTo(apphudV2Wrapper);
    }

    @Test
    public void getApphudWrapperReturnsDummyWrapperWhenV3WrapperLoadingFails() {
        when(ApphudVersionProvider.getApphudVersion()).thenReturn(ApphudVersion.APPHUD_V3);
        when(ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
            APPHUD_V3_WRAPPER_CLASS,
            ApphudWrapper.class
        )).thenReturn(null);

        ApphudWrapper result = ApphudWrapperProvider.getApphudWrapper();

        assertThat(result).isInstanceOf(DummyApphudWrapper.class);
    }

    @Test
    public void getApphudWrapperReturnsDummyWrapperWhenV2WrapperLoadingFails() {
        when(ApphudVersionProvider.getApphudVersion()).thenReturn(ApphudVersion.APPHUD_V2);
        when(ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
            APPHUD_V2_WRAPPER_CLASS,
            ApphudWrapper.class
        )).thenReturn(null);

        ApphudWrapper result = ApphudWrapperProvider.getApphudWrapper();

        assertThat(result).isInstanceOf(DummyApphudWrapper.class);
    }
}
