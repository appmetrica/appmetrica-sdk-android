package io.appmetrica.analytics.adrevenue.fyber.v3.internal;

import io.appmetrica.analytics.adrevenue.fyber.v3.impl.FyberAdRevenueAdapter;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;

import static io.appmetrica.analytics.adrevenue.fyber.v3.impl.Constants.LIBRARY_MAIN_CLASS;
import static io.appmetrica.analytics.adrevenue.fyber.v3.impl.Constants.MODULE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FyberClientModuleEntryPointTest extends CommonTest {

    private final ClientContext clientContext = mock(ClientContext.class);

    @Rule
    public final MockedStaticRule<ReflectionUtils> reflectionUtilsRule = new MockedStaticRule<>(ReflectionUtils.class);
    @Rule
    public final MockedStaticRule<FyberAdRevenueAdapter> adapterRule =
        new MockedStaticRule<>(FyberAdRevenueAdapter.class);

    private final FyberClientModuleEntryPoint entryPoint = new FyberClientModuleEntryPoint();

    @Test
    public void getIdentifier() {
        assertThat(entryPoint.getIdentifier()).isEqualTo(MODULE_ID);
    }

    @Test
    public void onActivated() {
        when(ReflectionUtils.detectClassExists(LIBRARY_MAIN_CLASS)).thenReturn(true);

        entryPoint.initClientSide(clientContext);
        entryPoint.onActivated();

        adapterRule.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                FyberAdRevenueAdapter.registerListener(clientContext);
            }
        });
    }

    @Test
    public void onActivatedIfClientContextIsNull() {
        when(ReflectionUtils.detectClassExists(LIBRARY_MAIN_CLASS)).thenReturn(true);

        entryPoint.initClientSide(null);
        entryPoint.onActivated();

        adapterRule.getStaticMock().verifyNoInteractions();
    }

    @Test
    public void onActivatedIfNoLibrary() {
        when(ReflectionUtils.detectClassExists(LIBRARY_MAIN_CLASS)).thenReturn(false);

        entryPoint.initClientSide(clientContext);
        entryPoint.onActivated();

        adapterRule.getStaticMock().verifyNoInteractions();
    }
}
