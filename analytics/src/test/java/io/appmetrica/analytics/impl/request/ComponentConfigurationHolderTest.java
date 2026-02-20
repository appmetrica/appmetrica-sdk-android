package io.appmetrica.analytics.impl.request;

import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommonArgumentsTestUtils;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.networktasks.internal.BaseRequestConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ComponentConfigurationHolderTest extends CommonTest {

    private ComponentConfigurationHolder<
        CoreRequestConfig,
        ReportRequestConfig.Arguments,
        BaseRequestConfig.ComponentLoader<
            CoreRequestConfig,
            ReportRequestConfig.Arguments,
            CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments>
            >
        > mHolder;

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        mHolder = new ComponentConfigurationHolder<
            CoreRequestConfig,
            ReportRequestConfig.Arguments,
            BaseRequestConfig.ComponentLoader<
                CoreRequestConfig,
                ReportRequestConfig.Arguments,
                CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments>
                >
            >(
            mock(CoreRequestConfig.CoreLoader.class),
            mock(StartupState.class),
            ReportRequestConfig.Arguments.empty()) {
        };
    }

    @Test
    public void testUpdateClientConfiguration() {
        CommonArguments.ReporterArguments configuration = CommonArgumentsTestUtils.emptyReporterArguments();
        ReportRequestConfig.Arguments oldArguments = mHolder.getArguments();
        assertThat(oldArguments).isNotSameAs(configuration);
        mHolder.updateArguments(configuration);
        assertThat(mHolder.getArguments()).isSameAs(oldArguments);
    }

}
